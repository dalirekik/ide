/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.client.application;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Timer;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.ExceptionThrownHandler;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorChangeActiveFileEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorOpenFileEvent;
import org.exoplatform.ide.client.framework.event.IDELoadCompleteEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.project.OpenProjectEvent;
import org.exoplatform.ide.client.framework.project.ProjectOpenedEvent;
import org.exoplatform.ide.client.framework.project.ProjectOpenedHandler;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.model.Settings;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.FileContentUnmarshaller;
import org.exoplatform.ide.vfs.client.marshal.ItemUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.ItemWrapper;
import org.exoplatform.ide.vfs.client.model.ProjectModel;

import java.util.*;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class RestoreOpenedFilesPhase implements ExceptionThrownHandler, EditorActiveFileChangedHandler,
                                                ProjectOpenedHandler {

    public static final int SCHEDULE_START = 100;

    private ApplicationSettings applicationSettings;

    private List<String> filesToLoad;

    private Map<String, FileModel> openedFiles = new LinkedHashMap<String, FileModel>();

    private FileModel fileToLoad;

    private List<String> filesToOpen;

    private String activeFileURL;

    private boolean isLoadingOpenedFiles = false;

    private boolean isRestoringOpenedFiles = false;

    private ProjectModel restoredProject;


    private String initialOpenedProject;

    private List<String> initialOpenedFiles;

    private String initialActiveFile;


    public RestoreOpenedFilesPhase(ApplicationSettings applicationSettings, String initialOpenedProject, List<String> initialOpenedFiles,
                                   String initialActiveFile) {
        this.applicationSettings = applicationSettings;

        this.initialOpenedProject = initialOpenedProject;
        this.initialOpenedFiles = initialOpenedFiles;
        this.initialActiveFile = initialActiveFile;

        // TODO eventBus.fireEvent(new EnableStandartErrorsHandlingEvent(false));

        IDE.addHandler(ExceptionThrownEvent.TYPE, this);
        IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);

        rememberFilesToBeRestored();
        isLoadingOpenedFiles = false;
        isRestoringOpenedFiles = false;

        new Timer() {
            @Override
            public void run() {
                execute();
            }
        }.schedule(SCHEDULE_START);
    }

    protected void execute() {
        //String openedProjectId = applicationSettings.getValueAsString(Settings.OPENED_PROJECT);
        String projectId = initialOpenedProject;

        if (projectId == null || projectId.isEmpty()) {
            //lazyRestoreOpenedFiles();
            loadComplete();
            return;
        }

        findProjectById(projectId);
    }

    private void findProjectById(String projectId) {
        try {
            final ProjectModel project = new ProjectModel();
            project.setId(projectId);

            VirtualFileSystem.getInstance().getItemById(projectId,
                                                        new AsyncRequestCallback<ItemWrapper>(
                                                                new ItemUnmarshaller(new ItemWrapper(project))) {
                                                            @Override
                                                            protected void onSuccess(ItemWrapper result) {
                                                                final ProjectModel project = (ProjectModel)result.getItem();

                                                                if (ProjectType.MultiModule.value().equals(project.getProjectType())) {
                                                                    loadComplete();
                                                                    return;
                                                                }

                                                                IDE.addHandler(ProjectOpenedEvent.TYPE, RestoreOpenedFilesPhase.this);
                                                                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                                                                    @Override
                                                                    public void execute() {
                                                                        IDE.fireEvent(new OpenProjectEvent(project));
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            protected void onFailure(Throwable exception) {
                                                                IDE.fireEvent(new ExceptionThrownEvent(exception));
                                                                loadComplete();
                                                            }
                                                        });

        } catch (Exception e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
            loadComplete();
        }
    }

    @Override
    public void onProjectOpened(ProjectOpenedEvent event) {
        IDE.removeHandler(ProjectOpenedEvent.TYPE, RestoreOpenedFilesPhase.this);

        restoredProject = event.getProject();

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                lazyRestoreOpenedFiles();
            }
        });
    }

    private void rememberFilesToBeRestored() {
        if (!applicationSettings.containsKey(Settings.OPENED_FILES)) {
            applicationSettings.setValue(Settings.OPENED_FILES, new ArrayList<String>(), Store.COOKIES);
        }

        filesToLoad = new ArrayList<String>();
        filesToLoad.addAll(initialOpenedFiles);
//      filesToLoad.addAll(applicationSettings.getValueAsList(Settings.OPENED_FILES));

        activeFileURL = applicationSettings.getValueAsString(Settings.ACTIVE_FILE);
        activeFileURL = initialActiveFile;
    }

    protected void lazyRestoreOpenedFiles() {
        isLoadingOpenedFiles = true;
        isRestoringOpenedFiles = false;

        preloadNextFile();
    }

    private void preloadNextFile() {
        if (filesToLoad.size() == 0) {
            isLoadingOpenedFiles = false;
            openFilesInEditor();
            return;
        }

        String fileId = filesToLoad.get(0);
        fileToLoad = new FileModel();
        fileToLoad.setId(fileId);
        filesToLoad.remove(0);
        try {
            VirtualFileSystem.getInstance().getItemById(fileId,
                                                        new AsyncRequestCallback<ItemWrapper>(
                                                                new ItemUnmarshaller(new ItemWrapper(fileToLoad))) {
                                                            @Override
                                                            protected void onFailure(Throwable exception) {
                                                                preloadNextFile();
                                                            }

                                                            @Override
                                                            protected void onSuccess(ItemWrapper result) {
                                                                FileModel file = (FileModel)result.getItem();
                                                                file.setContentChanged(false);
                                                                try {
                                                                    VirtualFileSystem.getInstance().getContent(
                                                                            new AsyncRequestCallback<FileModel>(
                                                                                    new FileContentUnmarshaller(file)) {
                                                                                @Override
                                                                                protected void onSuccess(FileModel result) {
                                                                                    openedFiles.put(result.getId(), result);
                                                                                    preloadNextFile();
                                                                                }

                                                                                @Override
                                                                                protected void onFailure(Throwable exception) {
                                                                                    IDE.fireEvent(new ExceptionThrownEvent(exception));
                                                                                    preloadNextFile();
                                                                                }
                                                                            });
                                                                } catch (RequestException e) {
                                                                    IDE.fireEvent(new ExceptionThrownEvent(e));
                                                                }
                                                            }

                                                        });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    private void openFilesInEditor() {
        isRestoringOpenedFiles = true;
        filesToOpen = new ArrayList<String>(openedFiles.keySet());
        openNextFileInEditor();
    }

    private void openNextFileInEditor() {
        if (filesToOpen.size() == 0) {
            isRestoringOpenedFiles = false;

            new Timer() {
                @Override
                public void run() {
                    changeActiveFile();
                }
            }.schedule(100);

            return;
        }

        String fileURL = filesToOpen.get(0);
        filesToOpen.remove(0);

        FileModel file = openedFiles.get(fileURL);
        file.setProject(restoredProject);
        IDE.fireEvent(new EditorOpenFileEvent(file));
    }

    private FileModel getFileByPath(String path) {
        Iterator<FileModel> fileIter = openedFiles.values().iterator();
        while (fileIter.hasNext()) {
            FileModel file = fileIter.next();
            if (file.getPath().equals(path)) {
                return file;
            }
        }

        return null;
    }

    public void onError(ExceptionThrownEvent event) {
        if (isLoadingOpenedFiles) {
            preloadNextFile();
            return;
        }
    }

    @Override
    public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event) {
        if (isLoadingOpenedFiles) {
            preloadNextFile();
        }

        if (isRestoringOpenedFiles) {
            openNextFileInEditor();
            return;
        }
    }

    /** Final step - Changing the active file. */
    private void changeActiveFile() {
        if (activeFileURL != null) {
            FileModel file = getFileByPath(initialActiveFile);
            if (file != null) {
                IDE.fireEvent(new EditorChangeActiveFileEvent(file));
            }
        }

        loadComplete();
    }

    private void loadComplete() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                IDE.fireEvent(new IDELoadCompleteEvent());
            }
        });
    }

}
