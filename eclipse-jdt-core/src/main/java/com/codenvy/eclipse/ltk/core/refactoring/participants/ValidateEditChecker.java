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
import com.codenvy.eclipse.core.runtime.Assert;
import com.codenvy.eclipse.core.runtime.CoreException;
import com.codenvy.eclipse.core.runtime.IProgressMonitor;
import com.codenvy.eclipse.core.runtime.IStatus;
import com.codenvy.eclipse.ltk.core.refactoring.RefactoringStatus;
import com.codenvy.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import com.codenvy.eclipse.ltk.internal.core.refactoring.Resources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A validate edit checker is a shared checker to collect files
 * to be validated all at once. A validate edit checker checks
 * if the files are in sync with the underlying files system.
 * Additionally <code>IWorkspace#validateEdit</code> is called for
 * all read-only resources.
 * <p>
 * Note: Since 3.2, a {@link ResourceChangeChecker} exists. If clients
 * add their changed files to the {@link ResourceChangeChecker}
 * there is no need to add them to a validate edit checker as
 * well. Files marked as changed in the resource operation checker
 * will be automatically added to a validate edit checker (if one
 * exists).
 * </p>
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see com.codenvy.eclipse.core.resources.IWorkspace#validateEdit(com.codenvy.eclipse.core.resources.IFile[], Object)
 * @since 3.0
 */
public class ValidateEditChecker implements IConditionChecker
{

   private Set fFiles = new HashSet();

   private Object fContext;

   /**
    * The context passed to the validate edit call.
    *
    * @param context the <code>org.eclipse.swt.widgets.Shell</code> that is
    *                to be used to parent any dialogs with the user, or <code>null</code> if
    *                there is no UI context (declared as an <code>Object</code> to avoid any
    *                direct references on the SWT component)
    * @see com.codenvy.eclipse.core.resources.IWorkspace#validateEdit(com.codenvy.eclipse.core.resources.IFile[], Object)
    */
   public ValidateEditChecker(Object context)
   {
      fContext = context;
   }

   /**
    * Adds the given file to this checker.
    *
    * @param file the file to add
    */
   public void addFile(IFile file)
   {
      Assert.isNotNull(file);
      fFiles.add(file);
   }

   /**
    * Adds the given array of files.
    *
    * @param files the array of files to add
    */
   public void addFiles(IFile[] files)
   {
      Assert.isNotNull(files);
      fFiles.addAll(Arrays.asList(files));
   }

   /**
    * {@inheritDoc}
    */
   public RefactoringStatus check(IProgressMonitor monitor) throws CoreException
   {
      IResource[] resources = (IResource[])fFiles.toArray(new IResource[fFiles.size()]);
      RefactoringStatus result = new RefactoringStatus();
      IStatus status = Resources.checkInSync(resources);
      if (!status.isOK())
      {
         result.merge(RefactoringStatus.create(status));
      }
      status = Resources.makeCommittable(resources, fContext);
      if (!status.isOK())
      {
         result.merge(RefactoringStatus.create(status));
         if (!result.hasFatalError())
         {
            result.addFatalError(RefactoringCoreMessages.ValidateEditChecker_failed);
         }
      }
      return result;
   }
}