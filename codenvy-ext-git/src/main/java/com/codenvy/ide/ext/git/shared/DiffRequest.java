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
package com.codenvy.ide.ext.git.shared;

import com.codenvy.ide.dto.DTO;

/**
 * Request to show changes between commits. Use {@link #commitA} and {@link #commitB} to specify values for comparison.
 * <ul>
 * <li>If both are omitted then view changes between index and working tree.</li>
 * <li>If both are specified then view changes between two commits.</li>
 * <li>If {@link #commitA} is specified ONLY then behavior is dependent on state of {@link #cached}. If
 * <code>cached==false<code> then view changes between specified commit and working tree. If
 * <code>cached==true<code> then view changes between specified commit and index.</li>
 * </ul>
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: DiffRequest.java 22817 2011-03-22 09:17:52Z andrew00x $
 */
@DTO
public interface DiffRequest extends GitRequest {
    /** Type of diff output. */
    public enum DiffType {
        /** Only names of modified, added, deleted files. */
        NAME_ONLY("--name-only"),
        /**
         * Names staus of modified, added, deleted files.
         * <p/>
         * Example:
         * <p/>
         * <p/>
         * <pre>
         * D   README.txt
         * A   HOW-TO.txt
         * </pre>
         */
        NAME_STATUS("--name-status"),
        RAW("--raw");

        private final String value;

        private DiffType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /** @return filter of file to show diff. It may be either list of file names or name of directory to show all files under them */
    String[] getFileFilter();
    
    DiffRequest withFileFilter(String[] fileFilter);

    /** @return type of diff output */
    DiffType getType();
    
    DiffRequest withDiffType(DiffType type);

    /** @return <code>true</code> if renames must not be showing in diff result */
    boolean isNoRenames();
    
    DiffRequest withNoRenames(boolean noRenames);

    /** @return limit of showing renames in diff output. This attribute has sense if {@link #noRenames} is <code>false</code> */
    int getRenameLimit();
    
    DiffRequest withRenameLimit(int renameLimit);

    /** @return first commit to view changes */
    String getCommitA();
    
    DiffRequest withCommitA(String commitA);

    /** @return second commit to view changes */
    String getCommitB();
    
    DiffRequest withCommitB(String commitB);

    /**
     * @return if <code>false</code> (default) view changes between {@link #commitA} and working tree otherwise between {@link #commitA}
     *         and index
     */
    boolean isCached();
    
    DiffRequest withCached(boolean isCached);
}