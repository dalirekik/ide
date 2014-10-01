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
package com.codenvy.ide.jseditor.client;

import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.filetypes.FileType;
import com.codenvy.ide.api.filetypes.FileTypeRegistry;
import com.codenvy.ide.jseditor.client.inject.PlainTextFileType;
import com.google.inject.Inject;

@Extension(title = "Common editor components.", version = "3.0.0")
public class JsEditorExtension {

    /** The default editor injection name. */
    public static final String DEFAULT_EDITOR_TYPE_INJECT_NAME = "DefaultEditorType";

    /** The default editor injection name. */
    public static final String DEFAULT_EDITOR_TYPE_INSTANCE    = "DefaultEditorType";


    @Inject
    public JsEditorExtension(final FileTypeRegistry fileTypeRegistry,
                             final @PlainTextFileType FileType plainText) {

        // register text/plain file type
        fileTypeRegistry.registerFileType(plainText);
    }
}
