/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.core.editor;

import com.codenvy.ide.CoreLocalizationConstant;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.editor.EditorInitException;
import com.codenvy.ide.api.editor.EditorInput;
import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.editor.EditorPartPresenter.EditorPartCloseHandler;
import com.codenvy.ide.api.editor.EditorProvider;
import com.codenvy.ide.api.editor.EditorRegistry;
import com.codenvy.ide.api.event.ActivePartChangedEvent;
import com.codenvy.ide.api.event.ActivePartChangedHandler;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEvent.FileOperation;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.ItemEvent;
import com.codenvy.ide.api.event.ItemHandler;
import com.codenvy.ide.api.event.WindowActionEvent;
import com.codenvy.ide.api.event.WindowActionHandler;
import com.codenvy.ide.api.filetypes.FileType;
import com.codenvy.ide.api.filetypes.FileTypeRegistry;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.parts.PartPresenter;
import com.codenvy.ide.api.parts.PartStackType;
import com.codenvy.ide.api.parts.PropertyListener;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.projecttree.VirtualFile;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.api.projecttree.generic.ItemNode;
import com.codenvy.ide.api.texteditor.HasReadOnlyProperty;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.collections.StringMap;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

import static com.codenvy.ide.api.event.FileEvent.FileOperation.CLOSE;
import static com.codenvy.ide.api.event.ItemEvent.ItemOperation.DELETE;
import static com.codenvy.ide.api.notification.Notification.Type.ERROR;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;

/** @author Evgen Vidolob */
@Singleton
public class EditorAgentImpl implements EditorAgent {

    private final StringMap<EditorPartPresenter> openedEditors;
    /** Used to notify {@link EditorAgentImpl} that editor has closed */
    private final EditorPartCloseHandler editorClosed     = new EditorPartCloseHandler() {
        @Override
        public void onClose(EditorPartPresenter editor) {
            editorClosed(editor);
        }
    };
    private final FileEventHandler       fileEventHandler = new FileEventHandler() {
        @Override
        public void onFileOperation(final FileEvent event) {
            if (event.getOperationType() == FileOperation.OPEN) {
                openEditor(event.getFile());
            } else if (event.getOperationType() == CLOSE) {
                // close associated editor. OR it can be closed itself TODO
            }
        }
    };
    private final EventBus                   eventBus;
    private final WorkspaceAgent             workspace;
    private       Array<EditorPartPresenter> dirtyEditors;
    private       FileTypeRegistry           fileTypeRegistry;
    private       EditorRegistry             editorRegistry;
    private       EditorPartPresenter        activeEditor;
    private final ActivePartChangedHandler activePartChangedHandler = new ActivePartChangedHandler() {
        @Override
        public void onActivePartChanged(ActivePartChangedEvent event) {
            if (event.getActivePart() instanceof EditorPartPresenter) {
                activeEditor = (EditorPartPresenter)event.getActivePart();
                activeEditor.activate();
            }
        }
    };
    private NotificationManager      notificationManager;
    private CoreLocalizationConstant coreLocalizationConstant;
    private final WindowActionHandler windowActionHandler = new WindowActionHandler() {
        @Override
        public void onWindowClosing(final WindowActionEvent event) {
            openedEditors.iterate(new StringMap.IterationCallback<EditorPartPresenter>() {
                @Override
                public void onIteration(String s, EditorPartPresenter editorPartPresenter) {
                    if (editorPartPresenter.isDirty()) {
                        event.setMessage(coreLocalizationConstant.changesMayBeLost());
                    }
                }
            });
        }

        @Override
        public void onWindowClosed(WindowActionEvent event) {
        }
    };

    @Inject
    public EditorAgentImpl(EventBus eventBus,
                           FileTypeRegistry fileTypeRegistry,
                           EditorRegistry editorRegistry,
                           final WorkspaceAgent workspace,
                           final NotificationManager notificationManager,
                           CoreLocalizationConstant coreLocalizationConstant) {
        super();
        this.eventBus = eventBus;
        this.fileTypeRegistry = fileTypeRegistry;
        this.editorRegistry = editorRegistry;
        this.workspace = workspace;
        this.notificationManager = notificationManager;
        this.coreLocalizationConstant = coreLocalizationConstant;
        openedEditors = Collections.createStringMap();

        bind();
    }

    protected void bind() {
        eventBus.addHandler(ActivePartChangedEvent.TYPE, activePartChangedHandler);
        eventBus.addHandler(FileEvent.TYPE, fileEventHandler);
        eventBus.addHandler(WindowActionEvent.TYPE, windowActionHandler);
        eventBus.addHandler(ItemEvent.TYPE, new ItemHandler() {
            @Override
            public void onItem(ItemEvent event) {
                final ItemNode item = event.getItem();
                if (event.getOperation() == DELETE && item instanceof FolderNode) {
                    closeAllFilesByPath(item.getPath());
                }
            }
        });
    }

    private void closeAllFilesByPath(String path) {
        for (EditorPartPresenter editor : getOpenedEditors().getValues().asIterable()) {
            if (editor.getEditorInput().getFile().getPath().startsWith(path)) {
                eventBus.fireEvent(new FileEvent(editor.getEditorInput().getFile(), CLOSE));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void openEditor(@Nonnull final VirtualFile file) {
        doOpen(file, null);
    }

    @Override
    public void openEditor(@Nonnull VirtualFile file, @Nonnull OpenEditorCallback callback) {
        doOpen(file, callback);
    }

    private void doOpen(final VirtualFile file, final OpenEditorCallback callback) {
        if (openedEditors.containsKey(file.getPath())) {
            workspace.setActivePart(openedEditors.get(file.getPath()));
        } else {
            FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
            EditorProvider editorProvider = editorRegistry.getEditor(fileType);
            final EditorPartPresenter editor = editorProvider.getEditor();
            try {
                editor.init(new EditorInputImpl(fileType, file));
                editor.addCloseHandler(editorClosed);
            } catch (EditorInitException e) {
                Log.error(getClass(), e);
            }
            workspace.openPart(editor, PartStackType.EDITING);
            openedEditors.put(file.getPath(), editor);

            workspace.setActivePart(editor);
            editor.addPropertyListener(new PropertyListener() {
                @Override
                public void propertyChanged(PartPresenter source, int propId) {
                    if (propId == EditorPartPresenter.PROP_INPUT) {
                        if(editor instanceof HasReadOnlyProperty) {
                            ((HasReadOnlyProperty)editor).setReadOnly(file.isReadOnly());
                        }
                        if(callback != null) {
                            callback.onEditorOpened(editor);
                        }
                    }

                }
            });

        }
    }

    @Override
    public void activateEditor(@Nonnull EditorPartPresenter editor) {
        workspace.setActivePart(editor);
    }

    /** {@inheritDoc} */
    @Override
    public Array<EditorPartPresenter> getDirtyEditors() {
        Array<EditorPartPresenter> dirtyEditors = Collections.createArray();
        for (EditorPartPresenter partPresenter : getOpenedEditors().getValues().asIterable()) {
            if (partPresenter.isDirty()) {
                dirtyEditors.add(partPresenter);
            }
        }
        return dirtyEditors;
    }

    /** @param editor */
    protected void editorClosed(EditorPartPresenter editor) {
        String closedFilePath = editor.getEditorInput().getFile().getPath();
        openedEditors.remove(closedFilePath);

        //call close() method
        editor.close(false);

        String activeFilePath = activeEditor.getEditorInput().getFile().getPath();
        if (activeFilePath == closedFilePath) {
            activeEditor = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public StringMap<EditorPartPresenter> getOpenedEditors() {
        return openedEditors;
    }

    /** {@inheritDoc} */
    @Override
    public void saveAll(final AsyncCallback callback) {
        dirtyEditors = getDirtyEditors();
        if (dirtyEditors.isEmpty()) {
            Notification notification = new Notification(coreLocalizationConstant.allFilesSaved(), INFO);
            notificationManager.showNotification(notification);
            callback.onSuccess("Success");
        } else {
            doSave(callback);
        }
    }

    private void doSave(final AsyncCallback callback) {
        final EditorPartPresenter partPresenter = dirtyEditors.get(0);
        partPresenter.doSave(new AsyncCallback<EditorInput>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
                Notification notification = new Notification(coreLocalizationConstant.someFilesCanNotBeSaved(), ERROR);
                notificationManager.showNotification(notification);
            }

            @Override
            public void onSuccess(EditorInput result) {
                dirtyEditors.remove(partPresenter);
                if (dirtyEditors.isEmpty()) {
                    Notification notification = new Notification(coreLocalizationConstant.allFilesSaved(), INFO);
                    notificationManager.showNotification(notification);
                    callback.onSuccess("Success");
                } else {
                    doSave(callback);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public EditorPartPresenter getActiveEditor() {
        return activeEditor;
    }

}
