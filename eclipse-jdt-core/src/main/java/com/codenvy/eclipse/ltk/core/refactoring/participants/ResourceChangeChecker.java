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
package com.codenvy.eclipse.ltk.core.refactoring.participants;

import com.codenvy.eclipse.core.resources.IFile;
import com.codenvy.eclipse.core.resources.IResource;
import com.codenvy.eclipse.core.resources.IResourceDelta;
import com.codenvy.eclipse.core.resources.IResourceDeltaVisitor;
import com.codenvy.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import com.codenvy.eclipse.core.resources.mapping.ResourceChangeValidator;
import com.codenvy.eclipse.core.runtime.CoreException;
import com.codenvy.eclipse.core.runtime.IProgressMonitor;
import com.codenvy.eclipse.core.runtime.IStatus;
import com.codenvy.eclipse.ltk.core.refactoring.RefactoringStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * A resource operation checker is a shared checker to collect all
 * changes done by the refactoring and the participants to resources
 * so that they can be validated as one change. A resource operation
 * checker supersedes the {@link ValidateEditChecker}. So if clients
 * add their content changes to this checker there is no need to add
 * them to the {@link ValidateEditChecker} as well.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see ResourceChangeValidator
 * @since 3.2
 */
public class ResourceChangeChecker implements IConditionChecker {

    private IResourceChangeDescriptionFactory fDeltaFactory;

    public ResourceChangeChecker() {
        fDeltaFactory = ResourceChangeValidator.getValidator().createDeltaFactory();
    }

    /**
     * A helper method to check a set of changed files.
     *
     * @param files
     *         the array of files that change
     * @param monitor
     *         a progress monitor to report progress or <code>null</code>
     *         if progress reporting is not desired
     * @return a refactoring status containing the detect problems
     * @throws com.codenvy.eclipse.core.runtime.CoreException
     *         a {@link com.codenvy.eclipse.core.runtime.CoreException} if an error occurs
     * @see ResourceChangeValidator#validateChange(IResourceDelta, com.codenvy.eclipse.core.runtime.IProgressMonitor)
     */
    public static RefactoringStatus checkFilesToBeChanged(IFile[] files, IProgressMonitor monitor) throws CoreException {
        ResourceChangeChecker checker = new ResourceChangeChecker();
        for (int i = 0; i < files.length; i++) {
            checker.getDeltaFactory().change(files[i]);
        }
        return checker.check(monitor);
    }

    /**
     * Returns the delta factory to be used to record resource
     * operations.
     *
     * @return the delta factory
     */
    public IResourceChangeDescriptionFactory getDeltaFactory() {
        return fDeltaFactory;
    }

    public RefactoringStatus check(IProgressMonitor monitor) throws CoreException {
        IStatus status = ResourceChangeValidator.getValidator().validateChange(fDeltaFactory.getDelta(), monitor);
        return createFrom(status);
    }

    /* package */ IFile[] getChangedFiles() throws CoreException {
        IResourceDelta root = fDeltaFactory.getDelta();
        final List result = new ArrayList();
        root.accept(new IResourceDeltaVisitor() {
            public boolean visit(IResourceDelta delta) throws CoreException {
                final IResource resource = delta.getResource();
                if (resource.getType() == IResource.FILE) {
                    final int kind = delta.getKind();
                    if (isSet(kind, IResourceDelta.CHANGED)) {
                        result.add(resource);
                    } else if (isSet(kind, IResourceDelta.ADDED) && isSet(delta.getFlags(),
                                                                          IResourceDelta.CONTENT | IResourceDelta.MOVED_FROM)) {
                        final IFile movedFrom = resource.getWorkspace().getRoot().getFile(delta.getMovedFromPath());
                        result.add(movedFrom);
                    }
                }
                return true;
            }
        });
        return (IFile[])result.toArray(new IFile[result.size()]);
    }

    private static final boolean isSet(int flags, int flag) {
        return (flags & flag) == flag;
    }

    private static RefactoringStatus createFrom(IStatus status) {
        if (status.isOK()) {
            return new RefactoringStatus();
        }

        if (!status.isMultiStatus()) {
            switch (status.getSeverity()) {
                case IStatus.OK:
                    return new RefactoringStatus();
                case IStatus.INFO:
                    return RefactoringStatus.createInfoStatus(status.getMessage());
                case IStatus.WARNING:
                    return RefactoringStatus.createWarningStatus(status.getMessage());
                case IStatus.ERROR:
                    return RefactoringStatus.createErrorStatus(status.getMessage());
                case IStatus.CANCEL:
                    return RefactoringStatus.createFatalErrorStatus(status.getMessage());
                default:
                    return RefactoringStatus.createFatalErrorStatus(status.getMessage());
            }
        } else {
            IStatus[] children = status.getChildren();
            RefactoringStatus result = new RefactoringStatus();
            for (int i = 0; i < children.length; i++) {
                result.merge(createFrom(children[i]));
            }
            return result;
        }
    }
}
