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
package org.exoplatform.ide.extension.cloudfoundry.client.rename;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link RenameApplicationEvent} event.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: RenameApplicationHandler.java Jul 15, 2011 11:23:26 AM vereshchaka $
 */
public interface RenameApplicationHandler extends EventHandler {
    /**
     * Perform actions, when user tries to rename application.
     *
     * @param event
     */
    void onRenameApplication(RenameApplicationEvent event);

}
