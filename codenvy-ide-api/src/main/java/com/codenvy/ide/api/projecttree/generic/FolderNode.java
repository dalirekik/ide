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
package com.codenvy.ide.api.projecttree.generic;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A node that represents a folder (an {@link ItemReference} with type - folder).
 *
 * @author Artem Zatsarynnyy
 */
public class FolderNode extends ItemNode {

    @AssistedInject
    public FolderNode(@Assisted TreeNode<?> parent, @Assisted ItemReference data, @Assisted GenericTreeStructure treeStructure,
                      EventBus eventBus, ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        getChildren(getData().getPath(), new AsyncCallback<Array<ItemReference>>() {
            @Override
            public void onSuccess(Array<ItemReference> childItems) {
                setChildren(getChildNodesForItems(childItems));
                callback.onSuccess(FolderNode.this);
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private Array<TreeNode<?>> getChildNodesForItems(Array<ItemReference> childItems) {
        final boolean isShowHiddenItems = getTreeStructure().getSettings().isShowHiddenItems();
        Array<TreeNode<?>> oldChildren = Collections.createArray(getChildren().asIterable());
        Array<TreeNode<?>> newChildren = Collections.createArray();
        for (ItemReference item : childItems.asIterable()) {
            if (!isShowHiddenItems && item.getName().startsWith(".")) {
                continue;
            }
            AbstractTreeNode node = createChildNode(item);
            if (node != null) {
                if (oldChildren.contains(node)) {
                    final int i = oldChildren.indexOf(node);
                    newChildren.add(oldChildren.get(i));
                } else {
                    newChildren.add(node);
                }
            }
        }
        return newChildren;
    }

    /**
     * Method helps to retrieve children by the specified path using Codenvy Project API.
     *
     * @param path
     *         path to retrieve cachedChildren
     * @param callback
     *         callback to return retrieved cachedChildren
     */
    protected void getChildren(String path, final AsyncCallback<Array<ItemReference>> callback) {
        final Unmarshallable<Array<ItemReference>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ItemReference.class);
        projectServiceClient.getChildren(path, new AsyncRequestCallback<Array<ItemReference>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<ItemReference> result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /**
     * Creates node for the specified item. Method called for every child item in {@link #refreshChildren(AsyncCallback)} method.
     * <p/>
     * May be overridden in order to provide a way to create a node for the specified by.
     *
     * @param item
     *         {@link ItemReference} for which need to create node
     * @return new node instance or {@code null} if the specified item is not supported
     */
    @Nullable
    protected AbstractTreeNode<?> createChildNode(ItemReference item) {
        if ("file".equals(item.getType())) {
            return getTreeStructure().newFileNode(this, item);
        } else if ("folder".equals(item.getType())) {
            return getTreeStructure().newFolderNode(this, item);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public GenericTreeStructure getTreeStructure() {
        return (GenericTreeStructure)super.getTreeStructure();
    }
}
