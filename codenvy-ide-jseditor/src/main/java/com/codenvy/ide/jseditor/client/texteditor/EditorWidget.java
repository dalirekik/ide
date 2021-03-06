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
package com.codenvy.ide.jseditor.client.texteditor;


import java.util.List;

import javax.annotation.Nonnull;

import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.api.texteditor.UndoableEditor;
import com.codenvy.ide.jseditor.client.codeassist.AdditionalInfoCallback;
import com.codenvy.ide.jseditor.client.codeassist.CompletionProposal;
import com.codenvy.ide.jseditor.client.codeassist.CompletionsSource;
import com.codenvy.ide.jseditor.client.document.EmbeddedDocument;
import com.codenvy.ide.jseditor.client.editortype.EditorType;
import com.codenvy.ide.jseditor.client.events.DocumentChangeEvent;
import com.codenvy.ide.jseditor.client.events.HasCursorActivityHandlers;
import com.codenvy.ide.jseditor.client.events.HasGutterClickHandlers;
import com.codenvy.ide.jseditor.client.keymap.Keymap;
import com.codenvy.ide.jseditor.client.position.PositionConverter;
import com.codenvy.ide.jseditor.client.texteditor.LineStyler.HasLineStyler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;

/** An interface for editor widget implementations. */
public interface EditorWidget extends IsWidget, HasChangeHandlers, HasFocusHandlers, HasBlurHandlers,
                                      HasCursorActivityHandlers, HasGutter, HasKeybindings, HasTextMarkers,
                                      HasLineStyler,
                                      HasGutterClickHandlers,
                                      RequiresResize, UndoableEditor {

    /**
     * Returns the contents of the editor.
     *
     * @return
     */
    String getValue();

    /**
     * Sets the content of the editor.<br>
     * The operation <em>must</em> send a {@link DocumentChangeEvent} on the document private event bus.
     *
     * @param newValue
     *         the new contents
     */
    void setValue(String newValue);

    /**
     * Returns the current language mode for highlighting.
     *
     * @return the mode
     */
    String getMode();

    /**
     * Change readonly state of the editor.
     *
     * @param isReadOnly
     *         true to set the editor in readonly mode, false to allow edit
     */
    void setReadOnly(boolean isReadOnly);

    /**
     * Returns the readonly state of the editor.
     *
     * @return the readonly state, true iff the editor is readonly
     */
    boolean isReadOnly();

    /**
     * Returns the dirty state of the editor.
     *
     * @return true iff the editor is dirty (i.e. unsaved change were made)
     */
    boolean isDirty();

    /** Marks the editor as clean i.e change the dirty state to false. */
    void markClean();

    /**
     * Returns the tab size (equivalent number of spaces).
     *
     * @return the tab size
     */
    int getTabSize();

    /**
     * Sets the tab size.
     *
     * @param tabSize
     *         the new value
     */
    void setTabSize(int tabSize);

    /**
     * The instance of {@link EmbeddedDocument}.
     *
     * @return the embedded document
     */
    EmbeddedDocument getDocument();

    /**
     * Returns the selected range in the editor. In case of multiple selection support, returns the primary selection. When no actual
     * selection is done, a selection with a zero length is given
     *
     * @return the selected range
     */
    Region getSelectedRange();

    /**
     * Returns the editor type for this editor.
     *
     * @return the editor type
     */
    EditorType getEditorType();

    /**
     * Returns the current keymap in the editor.
     *
     * @return the current keymap
     */
    @Nonnull
    Keymap getKeymap();

    /** Give the focus to the editor. */
    void setFocus();

    /**
     * Show a message to the user.
     * 
     * @param message the message
     */
    void showMessage(String message);

    /**
     * Selects the given range in the editor.
     * 
     * @param selection the new selection
     * @param show whether the editor should be scrolled to show the range
     */
    void setSelectedRange(Region selection, boolean show);

    /**
     * Scroll the editor to show the given range.
     * 
     * @param range the range to show
     */
    void setDisplayRange(Region range);

    /**
     * Returns a position converter relative to this editor (pixel coordinates <-> line char positions).
     * 
     * @return a position converter
     */
    PositionConverter getPositionConverter();

    /**
     * Display the completion proposals.
     * 
     * @param proposals the proposals
     */
    void showCompletionsProposals(List<CompletionProposal> proposals);

    /**
     * Display the completion proposals.
     * 
     * @param completionsSource the completion source
     */
    void showCompletionProposals(CompletionsSource completionsSource);
    
    /**
     * Display the default completion proposals.
     */
    void showCompletionProposals();

    void showCompletionProposals(CompletionsSource completionsSource, AdditionalInfoCallback additionalInfoCallback);

    /**
     * Refresh the editor widget.
     */
    void refresh();
}
