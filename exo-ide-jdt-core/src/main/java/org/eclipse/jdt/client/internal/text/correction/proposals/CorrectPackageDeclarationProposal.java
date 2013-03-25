/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.client.internal.text.correction.proposals;

import com.google.gwt.user.client.ui.Image;

import org.eclipse.jdt.client.JdtClientBundle;
import org.eclipse.jdt.client.codeassistant.api.IProblemLocation;

import org.eclipse.jdt.client.internal.text.correction.CorrectionMessages;
import org.eclipse.jdt.client.runtime.CoreException;
import org.exoplatform.ide.editor.shared.text.IDocument;
import org.exoplatform.ide.editor.shared.text.edits.TextEdit;

public class CorrectPackageDeclarationProposal extends CUCorrectionProposal
{

   private IProblemLocation fLocation;

   public CorrectPackageDeclarationProposal(IProblemLocation location, int relevance, IDocument document)
   {
      super(CorrectionMessages.INSTANCE.CorrectPackageDeclarationProposal_name(), relevance, document, new Image(
         JdtClientBundle.INSTANCE.packd_obj()));
      fLocation = location;
   }

   /* (non-Javadoc)
    * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#addEdits(org.eclipse.jdt.internal.corext.textmanipulation.TextBuffer)
    */
   @Override
   protected void addEdits(IDocument doc, TextEdit root) throws CoreException
   {
      super.addEdits(doc, root);

      //      ICompilationUnit cu = getCompilationUnit();
      //TODO
      //      IPackageFragment parentPack = (IPackageFragment)cu.getParent();
      //      IPackageDeclaration[] decls = cu.getPackageDeclarations();
      //
      //      if (parentPack.isDefaultPackage() && decls.length > 0)
      //      {
      //         for (int i = 0; i < decls.length; i++)
      //         {
      //            ISourceRange range = decls[i].getSourceRange();
      //            root.addChild(new DeleteEdit(range.getOffset(), range.getLength()));
      //         }
      //         return;
      //      }
      //      if (!parentPack.isDefaultPackage() && decls.length == 0)
      //      {
      //         String lineDelim = StubUtility.getLineDelimiterUsed(cu);
      //         String str = "package " + parentPack.getElementName() + ';' + lineDelim + lineDelim; //$NON-NLS-1$
      //         root.addChild(new InsertEdit(0, str));
      //         return;
      //      }
      //
      //      root.addChild(new ReplaceEdit(fLocation.getOffset(), fLocation.getLength(), parentPack.getElementName()));
   }

   /* (non-Javadoc)
    * @see org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeCorrectionProposal#getName()
    */
   @Override
   public String getName()
   {
      //TODO
      //      ICompilationUnit cu = getCompilationUnit();
      //      IPackageFragment parentPack = (IPackageFragment)cu.getParent();
      //      try
      //      {
      //         IPackageDeclaration[] decls = cu.getPackageDeclarations();
      //         if (parentPack.isDefaultPackage() && decls.length > 0)
      //         {
      //            return Messages.format(CorrectionMessages.CorrectPackageDeclarationProposal_remove_description,
      //               BasicElementLabels.getJavaElementName(decls[0].getElementName()));
      //         }
      //         if (!parentPack.isDefaultPackage() && decls.length == 0)
      //         {
      //            return (Messages.format(CorrectionMessages.CorrectPackageDeclarationProposal_add_description,
      //               JavaElementLabels.getElementLabel(parentPack, JavaElementLabels.ALL_DEFAULT)));
      //         }
      //      }
      //      catch (JavaModelException e)
      //      {
      //         JavaPlugin.log(e);
      //      }
      //      return (Messages.format(CorrectionMessages.CorrectPackageDeclarationProposal_change_description,
      //         JavaElementLabels.getElementLabel(parentPack, JavaElementLabels.ALL_DEFAULT)));
      return "fix me in " + getClass().getSimpleName();
   }
}