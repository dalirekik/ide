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
package com.codenvy.ide.jseditor.client.partition;

import java.util.List;

/**
 * A {@link TokenScanner} that detects partitions.
 */
public interface PartitionScanner extends TokenScanner {

    /**
     * Set the list of line delimiters.
     * @param delimiters the delimiters
     */
    void setLegalLineDelimiters(final List<String> delimiters);

    /**
     * Set the string to scan.
     * @param content the new content to parse
     */
    void setScannedString(String content);
}
