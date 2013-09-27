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
package org.exoplatform.ide.vfs.server.exceptions;

/**
 * Thrown if requested item does not exist.
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: ItemNotFoundException.java 68071 2011-04-07 13:11:47Z vitalka $
 */
@SuppressWarnings("serial")
public class ItemNotFoundException extends VirtualFileSystemException {
    /**
     * @param message
     *         the message
     */
    public ItemNotFoundException(String message) {
        super(message);
    }
}
