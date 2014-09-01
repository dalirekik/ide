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
package com.codenvy.ide.api.projecttree;

import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An <code>AbstractTreeNode</code> is a general-purpose node in a project tree.
 * An <code>AbstractTreeNode</code> may also hold a reference to an associated object,
 * the use of which is left to the user.
 *
 * @param <T>
 *         the type of the associated data
 * @author Artem Zatsarynnyy
 */
public abstract class AbstractTreeNode<T> {
    protected T                          data;
    protected AbstractTreeNode<?>        parent;
    protected Array<AbstractTreeNode<?>> children;
    private   Presentation               presentation;

    /**
     * Creates new node with the specified parent, associated data and display name.
     *
     * @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param displayName
     *         node's display name
     */
    public AbstractTreeNode(@Nullable AbstractTreeNode<?> parent, T data, @Nonnull String displayName) {
        this.data = data;
        this.parent = parent;
        children = Collections.createArray();
        presentation = getTemplatePresentation();
        presentation.setDisplayName(displayName);
    }

    /**
     * Returns this node's parent node.
     *
     * @return this node's parent node
     */
    public AbstractTreeNode<?> getParent() {
        return parent;
    }

    /**
     * Sets the new parent node for this node.
     *
     * @param parent
     *         the new parent node
     */
    public void setParent(AbstractTreeNode<?> parent) {
        this.parent = parent;
    }

    /**
     * Returns the object represented by this node                                   .
     *
     * @return the associated data
     */
    public T getData() {
        return data;
    }

    /**
     * Determines may the node be expanded.
     *
     * @return <code>true</code> - if node shouldn't never be expanded in the tree,
     * <code>false</code> - if node may be expanded
     */
    public abstract boolean isLeaf();

    /**
     * Returns an array of all this node's child nodes. The array will always
     * exist (i.e. never <code>null</code>) and be of length zero if this is
     * a leaf node.
     *
     * @return an array of all this node's child nodes
     */
    @Nonnull
    public Array<AbstractTreeNode<?>> getChildren() {
        return children;
    }

    /**
     * Set node's children.
     *
     * @param children
     *         array of new children for this node
     */
    public void setChildren(Array<AbstractTreeNode<?>> children) {
        this.children = children;
    }

    /**
     * Populate node by children.
     *
     * @param callback
     *         callback to return node with refreshed children
     */
    public abstract void refreshChildren(AsyncCallback<AbstractTreeNode<?>> callback);

    /** Process an action on node in the view (e.g. double-click on rendered node in the view). */
    public void processNodeAction() {
    }

    /** Defines whether the node may be renamed. */
    public boolean isRenemable() {
        return false;
    }

    /**
     * Override this method to provide a way to rename this node.
     *
     * @param newName
     *         new name
     * @param callback
     *         callback to return result
     */
    public void rename(String newName, AsyncCallback<Void> callback) {
    }

    /** Defines whether the node may be deleted. */
    public boolean isDeletable() {
        return false;
    }

    /**
     * Override this method to provide a way to delete this node.
     *
     * @param callback
     *         callback to return result
     */
    public void delete(AsyncCallback<Void> callback) {
    }

    /**
     * Returns a template presentation that will be used
     * as a template for created presentation.
     */
    protected Presentation getTemplatePresentation() {
        return new Presentation();
    }

    /** Returns a node's presentation. */
    public Presentation getPresentation() {
        return presentation;
    }
}
