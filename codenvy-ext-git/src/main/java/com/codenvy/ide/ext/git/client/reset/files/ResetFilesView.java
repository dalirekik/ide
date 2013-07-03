/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.ide.ext.git.client.reset.files;

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.ext.git.shared.IndexFile;
import com.codenvy.ide.json.JsonArray;

/**
 * The view of {@link com.codenvy.ide.ext.git.client.reset.commit.ResetToCommitPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface ResetFilesView extends View<ResetFilesView.ActionDelegate> {
    String FILES = "Files for commit";

    /** Needs for delegate some function into ResetFiles view. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Reset button. */
        void onResetClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();
    }

    /**
     * Set indexed files into table on view.
     *
     * @param indexedFiles
     *         indexed files
     */
    void setIndexedFiles(JsonArray<IndexFile> indexedFiles);

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}