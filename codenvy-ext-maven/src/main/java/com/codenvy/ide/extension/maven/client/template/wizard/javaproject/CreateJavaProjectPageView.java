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
package com.codenvy.ide.extension.maven.client.template.wizard.javaproject;

import com.codenvy.ide.api.mvp.View;

/**
 * The view of {@link CreateJavaProjectPagePresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface CreateJavaProjectPageView extends View<CreateJavaProjectPageView.ActionDelegate> {
    /** Action delegate for new Java project wizard */
    public interface ActionDelegate {
        /** Checks whether source folder is complete or not and updates navigation buttons. */
        void checkSourceFolederInput();
    }

    /**
     * Get Source folder name
     *
     * @return the source folder name
     */
    String getSourceFolder();
}