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
package com.codenvy.ide.ext.java.jdi.client.debug;

import com.codenvy.ide.ext.java.jdi.shared.Variable;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.json.JsonCollections;
import com.codenvy.ide.ui.tree.NodeDataAdapter;
import com.codenvy.ide.ui.tree.TreeNodeElement;

import java.util.HashMap;

/**
 * The adapter for debug variable node.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class VariableTreeNodeDataAdapter implements NodeDataAdapter<Variable> {
    private HashMap<Variable, TreeNodeElement<Variable>> treeNodeElements = new HashMap<Variable, TreeNodeElement<Variable>>();

    /** {@inheritDoc} */
    @Override
    public int compare(Variable a, Variable b) {
        JsonArray<String> pathA = a.getVariablePath().getPath();
        JsonArray<String> pathB = b.getVariablePath().getPath();

        for (int i = 0; i < pathA.size(); i++) {
            String elementA = pathA.get(i);
            String elementB = pathB.get(i);

            int compare = elementA.compareTo(elementB);
            if (compare != 0) {
                return compare;
            }
        }

        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(Variable data) {
        return !data.primitive();
    }

    /** {@inheritDoc} */
    @Override
    public JsonArray<Variable> getChildren(Variable data) {
        JsonArray<Variable> variables = data.getVariables();
        return variables != null ? variables : JsonCollections.<Variable>createArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeId(Variable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeName(Variable data) {
        return data.getName() + ": " + data.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public Variable getParent(Variable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TreeNodeElement<Variable> getRenderedTreeNode(Variable data) {
        return treeNodeElements.get(data);
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeName(Variable data, String name) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderedTreeNode(Variable data, TreeNodeElement<Variable> renderedNode) {
        treeNodeElements.put(data, renderedNode);
    }

    /** {@inheritDoc} */
    @Override
    public Variable getDragDropTarget(Variable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public JsonArray<String> getNodePath(Variable data) {
        return data.getVariablePath().getPath();
    }

    /** {@inheritDoc} */
    @Override
    public Variable getNodeByPath(Variable root, JsonArray<String> relativeNodePath) {
        Variable localRoot = root;
        for (int i = 0; i < relativeNodePath.size(); i++) {
            String path = relativeNodePath.get(i);
            if (localRoot != null) {
                JsonArray<Variable> variables = localRoot.getVariables();
                localRoot = null;
                for (int j = 0; j < variables.size(); j++) {
                    Variable variable = variables.get(i);
                    if (variable.getName().equals(path)) {
                        localRoot = variable;
                        break;
                    }
                }

                if (i == (relativeNodePath.size() - 1)) {
                    return localRoot;
                }
            }
        }
        return null;
    }
}