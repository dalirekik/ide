/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.api.ui.workspace;


import com.codenvy.ide.api.extension.SDK;


/**
 * Handles IDE Perspective, allows to open/close/switch Parts,
 * manages opened Parts.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
@SDK(title = "ide.api.ui.workspace")
public interface WorkspaceAgent {

    /**
     * Activate given part
     *
     * @param part
     */
    public void setActivePart(PartPresenter part);

    /**
     * Opens given Part
     *
     * @param part
     * @param type
     */
    public void openPart(PartPresenter part, PartStackType type);

    /**
     * Hides given Part
     *
     * @param part
     */
    public void hidePart(PartPresenter part);

    /**
     * Remove given Part
     *
     * @param part
     */
    public void removePart(PartPresenter part);
}