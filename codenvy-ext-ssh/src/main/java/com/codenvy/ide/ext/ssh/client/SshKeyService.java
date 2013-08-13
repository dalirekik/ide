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
package com.codenvy.ide.ext.ssh.client;

import com.codenvy.ide.annotations.NotNull;
import com.codenvy.ide.ext.ssh.shared.GenKeyRequest;
import com.codenvy.ide.ext.ssh.shared.KeyItem;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestException;

/**
 * The client service for working with ssh key.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: SshService May 18, 2011 4:49:49 PM evgen $
 */
public interface SshKeyService {
    /**
     * Receive all ssh key, stored on server
     *
     * @param callback
     */
    void getAllKeys(@NotNull JsonpAsyncCallback<JavaScriptObject> callback);

    /**
     * Generate new ssh key pare
     *
     * @param host
     *         for ssh key
     * @param callback
     * @throws RequestException
     */
    void generateKey(@NotNull String host, @NotNull AsyncRequestCallback<GenKeyRequest> callback) throws RequestException;

    /**
     * Get public ssh key
     *
     * @param keyItem
     *         to get public key
     * @param callback
     */
    void getPublicKey(@NotNull KeyItem keyItem, @NotNull JsonpAsyncCallback<JavaScriptObject> callback);

    /**
     * Delete ssh key
     *
     * @param keyItem
     *         to delete
     * @param callback
     */
    void deleteKey(@NotNull KeyItem keyItem, @NotNull JsonpAsyncCallback<Void> callback);
}