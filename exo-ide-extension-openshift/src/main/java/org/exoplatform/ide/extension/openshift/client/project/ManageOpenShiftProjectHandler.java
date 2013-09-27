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
package org.exoplatform.ide.extension.openshift.client.project;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link ManageOpenShiftProjectEvent} event.
 *
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Dec 5, 2011 12:34:43 PM anya $
 */
public interface ManageOpenShiftProjectHandler extends EventHandler {
    /**
     * Perform actions, when user tries to manage project deployed to OpenShift.
     *
     * @param event
     */
    void onManageOpenShiftProject(ManageOpenShiftProjectEvent event);
}
