/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.api.text.undo;

import com.codenvy.ide.api.text.Document;
import com.codenvy.ide.runtime.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * This document undo manager registry provides access to a document's
 * undo manager. In order to connect a document a document undo manager
 * call <code>connect</code>. After that call has successfully completed
 * undo manager can be obtained via <code>getDocumentUndoManager</code>.
 * The undo manager is created on the first connect and disposed on the last
 * disconnect, i.e. this registry keeps track of how often a undo manager is
 * connected and returns the same undo manager to each client as long as the
 * document is connected.
 * <p>
 * <em>The recoding of changes starts with the first {@link #connect(Document)}.</em></p>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 3.2
 */
public final class DocumentUndoManagerRegistry {

    private static final class Record {
        public Record(Document document) {
            count = 0;
            undoManager = new DocumentUndoManagerImpl(document);
        }

        private int count;

        private DocumentUndoManager undoManager;
    }

    private static Map<Document, Record> fgFactory = new HashMap<Document, DocumentUndoManagerRegistry.Record>();

    private DocumentUndoManagerRegistry() {
        // 	Do not instantiate
    }

    /**
     * Connects the file at the given location to this manager. After that call
     * successfully completed it is guaranteed that each call to <code>getFileBuffer</code>
     * returns the same file buffer until <code>disconnect</code> is called.
     * <p>
     * <em>The recoding of changes starts with the first {@link #connect(Document)}.</em></p>
     *
     * @param document
     *         the document to be connected
     */
    public static void connect(Document document) {
        Assert.isNotNull(document);
        Record record = (Record)fgFactory.get(document);
        if (record == null) {
            record = new Record(document);
            fgFactory.put(document, record);
        }
        record.count++;
    }

    /**
     * Disconnects the given document from this registry.
     *
     * @param document
     *         the document to be disconnected
     */
    public static void disconnect(Document document) {
        Assert.isNotNull(document);
        Record record = fgFactory.get(document);
        record.count--;
        if (record.count == 0)
            fgFactory.remove(document);

    }

    /**
     * Returns the file buffer managed for the given location or <code>null</code>
     * if there is no such file buffer.
     * <p>
     * The provided location is either a full path of a workspace resource or
     * an absolute path in the local file system. The file buffer manager does
     * not resolve the location of workspace resources in the case of linked
     * resources.
     * </p>
     *
     * @param document
     *         the document for which to get its undo manager
     * @return the document undo manager or <code>null</code>
     */
    public static DocumentUndoManager getDocumentUndoManager(Document document) {
        Assert.isNotNull(document);
        Record record = (Record)fgFactory.get(document);
        if (record == null)
            return null;
        return record.undoManager;
    }

}
