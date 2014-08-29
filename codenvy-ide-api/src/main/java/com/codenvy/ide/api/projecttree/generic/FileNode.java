/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.api.projecttree.generic;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * A node that represents a file.
 *
 * @author Artem Zatsarynnyy
 */
public class FileNode extends AbstractTreeNode<ItemReference> implements StorableNode {
    protected EventBus             eventBus;
    protected ProjectServiceClient projectServiceClient;

    public FileNode(AbstractTreeNode parent, ItemReference data, EventBus eventBus, ProjectServiceClient projectServiceClient) {
        super(parent, data, data.getName());
        this.eventBus = eventBus;
        this.projectServiceClient = projectServiceClient;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return data.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getPath() {
        return data.getPath();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(AsyncCallback<AbstractTreeNode<?>> callback) {
    }

    /** {@inheritDoc} */
    @Override
    public void processNodeAction() {
        eventBus.fireEvent(new FileEvent(this, FileEvent.FileOperation.OPEN));
    }

    public void getContent(final AsyncCallback<String> callback) {
        projectServiceClient.getFileContent(data.getPath(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
            @Override
            protected void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    public void updateContent(String content, final AsyncCallback<Void> callback) {
        projectServiceClient.updateFile(data.getPath(), content, null, new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }
}