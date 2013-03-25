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
package org.eclipse.jdt.client.internal.corext.refactoring.sorround;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.client.JdtExtension;
import org.eclipse.jdt.client.core.dom.AST;
import org.eclipse.jdt.client.core.dom.ASTNode;
import org.eclipse.jdt.client.core.dom.Assignment;
import org.eclipse.jdt.client.core.dom.Block;
import org.eclipse.jdt.client.core.dom.CatchClause;
import org.eclipse.jdt.client.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.client.core.dom.CompilationUnit;
import org.eclipse.jdt.client.core.dom.Expression;
import org.eclipse.jdt.client.core.dom.ExpressionStatement;
import org.eclipse.jdt.client.core.dom.IExtendedModifier;
import org.eclipse.jdt.client.core.dom.ITypeBinding;
import org.eclipse.jdt.client.core.dom.Modifier;
import org.eclipse.jdt.client.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.client.core.dom.Statement;
import org.eclipse.jdt.client.core.dom.TryStatement;
import org.eclipse.jdt.client.core.dom.Type;
import org.eclipse.jdt.client.core.dom.UnionType;
import org.eclipse.jdt.client.core.dom.VariableDeclaration;
import org.eclipse.jdt.client.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.client.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.client.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.client.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.client.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.client.core.dom.rewrite.ListRewrite;

import org.eclipse.jdt.client.internal.core.dom.CodeScopeBuilder;
import org.eclipse.jdt.client.internal.corext.codemanipulation.ASTResolving;
import org.eclipse.jdt.client.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.client.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.client.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.client.internal.corext.dom.Selection;
import org.eclipse.jdt.client.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.client.internal.corext.refactoring.code.CompilationUnitChange;
import org.eclipse.jdt.client.internal.corext.refactoring.util.SelectionAwareSourceRangeComputer;
import org.eclipse.jdt.client.internal.corext.util.Strings;
import org.eclipse.jdt.client.ltk.refactoring.Change;
import org.eclipse.jdt.client.ltk.refactoring.Refactoring;
import org.eclipse.jdt.client.ltk.refactoring.RefactoringStatus;
import org.eclipse.jdt.client.ltk.refactoring.TextFileChange;
import org.eclipse.jdt.client.runtime.CoreException;
import org.eclipse.jdt.client.runtime.IProgressMonitor;
import org.eclipse.jdt.client.runtime.NullProgressMonitor;
import org.eclipse.jdt.client.runtime.OperationCanceledException;
import org.exoplatform.ide.editor.shared.text.BadLocationException;
import org.exoplatform.ide.editor.shared.text.IDocument;
import org.exoplatform.ide.editor.shared.text.edits.MultiTextEdit;
import org.exoplatform.ide.editor.shared.text.edits.TextEdit;
import org.exoplatform.ide.editor.shared.text.edits.TextEditGroup;

/**
 * Surround a set of statements with a try/catch block or a try/multi-catch block.
 *
 * Special case:
 *
 * URL url= file.toURL();
 *
 * In this case the variable declaration statement gets convert into a
 * declaration without initializer. So the body of the try/catch block
 * only consists of new assignments. In this case we can't move the
 * selected nodes (e.g. the declaration) into the try block.
 */
public class SurroundWithTryCatchRefactoring extends Refactoring
{

   public static final String GROUP_EXC_TYPE = "exc_type"; //$NON-NLS-1$

   public static final String GROUP_EXC_NAME = "exc_name"; //$NON-NLS-1$

   private Selection fSelection;

   private SurroundWithTryCatchAnalyzer fAnalyzer;

   private boolean fLeaveDirty;

   private CompilationUnit fRootNode;

   private ASTRewrite fRewriter;

   private ImportRewrite fImportRewrite;

   private CodeScopeBuilder.Scope fScope;

   private ASTNode[] fSelectedNodes;

   private final boolean fIsMultiCatch;

   private final IDocument document;

   private SurroundWithTryCatchRefactoring(IDocument document, Selection selection, boolean isMultiCatch)
   {
      this.document = document;
      fSelection = selection;
      fIsMultiCatch = isMultiCatch;
      fLeaveDirty = false;
   }

   //   public static SurroundWithTryCatchRefactoring create(ITextSelection selection)
   //   {
   //      return create(cu, selection, false);
   //   }

   public static SurroundWithTryCatchRefactoring create(IDocument document, int offset, int length)
   {
      return create(document, offset, length, false);
   }

   //   public static SurroundWithTryCatchRefactoring create(ICompilationUnit cu, ITextSelection selection,
   //      boolean isMultiCatch)
   //   {
   //      return new SurroundWithTryCatchRefactoring(cu, Selection.createFromStartLength(selection.getOffset(),
   //         selection.getLength()), isMultiCatch);
   //   }

   public static SurroundWithTryCatchRefactoring create(IDocument document, int offset, int length, boolean isMultiCatch)
   {
      return new SurroundWithTryCatchRefactoring(document, Selection.createFromStartLength(offset, length),
         isMultiCatch);
   }

   //   public LinkedProposalModel getLinkedProposalModel()
   //   {
   //      return fLinkedProposalModel;
   //   }

   public void setLeaveDirty(boolean leaveDirty)
   {
      fLeaveDirty = leaveDirty;
   }

   public boolean stopExecution()
   {
      if (fAnalyzer == null)
         return true;
      ITypeBinding[] exceptions = fAnalyzer.getExceptions();
      return exceptions == null || exceptions.length == 0;
   }

   /* non Java-doc
    * @see IRefactoring#getName()
    */
   @Override
   public String getName()
   {
      return RefactoringCoreMessages.INSTANCE.SurroundWithTryCatchRefactoring_name();
   }

   public RefactoringStatus checkActivationBasics(IDocument document, CompilationUnit rootNode) throws CoreException
   {
      RefactoringStatus result = new RefactoringStatus();
      fRootNode = rootNode;

      fAnalyzer = new SurroundWithTryCatchAnalyzer(document, fSelection);
      fRootNode.accept(fAnalyzer);
      result.merge(fAnalyzer.getStatus());
      ITypeBinding[] exceptions = fAnalyzer.getExceptions();
      if (fIsMultiCatch && (exceptions == null || exceptions.length <= 1))
      {
         result.merge(RefactoringStatus.createWarningStatus(RefactoringCoreMessages.INSTANCE
            .SurroundWithTryCatchRefactoring_notMultipleexceptions()));
      }
      return result;
   }

   /*
    * @see Refactoring#checkActivation(IProgressMonitor)
    */
   @Override
   public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException
   {
      CompilationUnit rootNode = ASTResolving.createQuickFixAST(document, pm);
      return checkActivationBasics(document, rootNode);
   }

   //   /*
   //    * @see Refactoring#checkInput(IProgressMonitor)
   //    */
   //   @Override
   //   public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException
   //   {
   //      return Checks
   //         .validateModifiesFiles(ResourceUtil.getFiles(new ICompilationUnit[]{fCUnit}), getValidationContext());
   //   }

   /* non Java-doc
    * @see IRefactoring#createChange(IProgressMonitor)
    */
   @Override
   public Change createChange(IProgressMonitor pm) throws CoreException
   {
      final String NN = ""; //$NON-NLS-1$
      if (pm == null)
         pm = new NullProgressMonitor();
      pm.beginTask(NN, 2);
      try
      {
         final CompilationUnitChange result = new CompilationUnitChange(getName(), document);
         if (fLeaveDirty)
            result.setSaveMode(TextFileChange.LEAVE_DIRTY);
         MultiTextEdit root = new MultiTextEdit();
         result.setEdit(root);
         fRewriter = ASTRewrite.create(fAnalyzer.getEnclosingBodyDeclaration().getAST());
         fRewriter.setTargetSourceRangeComputer(new SelectionAwareSourceRangeComputer(fAnalyzer.getSelectedNodes(),
            document, fSelection.getOffset(), fSelection.getLength()));
         fImportRewrite = StubUtility.createImportRewrite(document, fRootNode, true);

         //         fLinkedProposalModel = new LinkedProposalModel();

         fScope =
            CodeScopeBuilder.perform(fAnalyzer.getEnclosingBodyDeclaration(), fSelection).findScope(
               fSelection.getOffset(), fSelection.getLength());
         fScope.setCursor(fSelection.getOffset());

         fSelectedNodes = fAnalyzer.getSelectedNodes();

         try
         {
            createTryCatchStatement(document, "\n");
         }
         catch (BadLocationException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

         if (fImportRewrite.hasRecordedChanges())
         {
            TextEdit edit = fImportRewrite.rewriteImports();
            root.addChild(edit);
            result.addTextEditGroup(new TextEditGroup(NN, new TextEdit[]{edit}));
         }
         TextEdit change = fRewriter.rewriteAST(document, JdtExtension.get().getOptions());
         root.addChild(change);
         result.addTextEditGroup(new TextEditGroup(NN, new TextEdit[]{change}));
         return result;
      }
      finally
      {
         pm.done();
      }
   }

   private AST getAST()
   {
      return fRootNode.getAST();
   }

   private void createTryCatchStatement(IDocument document, String lineDelimiter) throws CoreException,
      BadLocationException
   {
      List<Statement> result = new ArrayList<Statement>(1);
      TryStatement tryStatement = getAST().newTryStatement();
      ITypeBinding[] exceptions = fAnalyzer.getExceptions();
      ImportRewriteContext context =
         new ContextSensitiveImportRewriteContext(fAnalyzer.getEnclosingBodyDeclaration(), fImportRewrite);

      if (!fIsMultiCatch)
      {
         for (int i = 0; i < exceptions.length; i++)
         {
            ITypeBinding exception = exceptions[i];
            String type = fImportRewrite.addImport(exception, context);
            CatchClause catchClause = getAST().newCatchClause();
            tryStatement.catchClauses().add(catchClause);
            SingleVariableDeclaration decl = getAST().newSingleVariableDeclaration();
            String varName = StubUtility.getExceptionVariableName();

            String name = fScope.createName(varName, false);
            decl.setName(getAST().newSimpleName(name));
            decl.setType(ASTNodeFactory.newType(getAST(), type));
            catchClause.setException(decl);
            Statement st = getCatchBody(type, name, lineDelimiter);
            if (st != null)
            {
               catchClause.getBody().statements().add(st);
            }
            //            fLinkedProposalModel.getPositionGroup(GROUP_EXC_TYPE + i, true).addPosition(
            //               fRewriter.track(decl.getType()), i == 0);
            //            fLinkedProposalModel.getPositionGroup(GROUP_EXC_NAME + i, true).addPosition(
            //               fRewriter.track(decl.getName()), false);
         }
      }
      else
      {
         CatchClause catchClause = getAST().newCatchClause();
         SingleVariableDeclaration decl = getAST().newSingleVariableDeclaration();
         String varName = StubUtility.getExceptionVariableName();
         String name = fScope.createName(varName, false);
         decl.setName(getAST().newSimpleName(name));

         UnionType unionType = getAST().newUnionType();
         List<Type> types = unionType.types();
         for (int i = 0; i < exceptions.length; i++)
         {
            ITypeBinding exception = exceptions[i];
            Type type = fImportRewrite.addImport(exception, getAST(), context);
            types.add(type);
            //            fLinkedProposalModel.getPositionGroup(GROUP_EXC_TYPE + i, true).addPosition(fRewriter.track(type), i == 0);
         }

         decl.setType(unionType);
         catchClause.setException(decl);
         //         fLinkedProposalModel.getPositionGroup(GROUP_EXC_NAME + 0, true).addPosition(fRewriter.track(decl.getName()),
         //            false);
         Statement st = getCatchBody("Exception", name, lineDelimiter); //$NON-NLS-1$
         if (st != null)
         {
            catchClause.getBody().statements().add(st);
         }
         tryStatement.catchClauses().add(catchClause);
      }
      List<ASTNode> variableDeclarations = getSpecialVariableDeclarationStatements();
      ListRewrite statements = fRewriter.getListRewrite(tryStatement.getBody(), Block.STATEMENTS_PROPERTY);
      boolean selectedNodeRemoved = false;
      ASTNode expressionStatement = null;
      for (int i = 0; i < fSelectedNodes.length; i++)
      {
         ASTNode node = fSelectedNodes[i];
         if (node instanceof VariableDeclarationStatement && variableDeclarations.contains(node))
         {
            AST ast = getAST();
            VariableDeclarationStatement statement = (VariableDeclarationStatement)node;
            // Create a copy and remove the initializer
            VariableDeclarationStatement copy = (VariableDeclarationStatement)ASTNode.copySubtree(ast, statement);
            List<IExtendedModifier> modifiers = copy.modifiers();
            for (Iterator<IExtendedModifier> iter = modifiers.iterator(); iter.hasNext();)
            {
               IExtendedModifier modifier = iter.next();
               if (modifier.isModifier() && Modifier.isFinal(((Modifier)modifier).getKeyword().toFlagValue()))
               {
                  iter.remove();
               }
            }
            List<VariableDeclarationFragment> fragments = copy.fragments();
            for (Iterator<VariableDeclarationFragment> iter = fragments.iterator(); iter.hasNext();)
            {
               VariableDeclarationFragment fragment = iter.next();
               fragment.setInitializer(null);
            }
            CompilationUnit root = (CompilationUnit)statement.getRoot();
            int extendedStart = root.getExtendedStartPosition(statement);
            // we have a leading comment and the comment is covered by the selection
            if (extendedStart != statement.getStartPosition() && extendedStart >= fSelection.getOffset())
            {
               String commentToken = document.get(extendedStart, statement.getStartPosition() - extendedStart);
               commentToken = Strings.trimTrailingTabsAndSpaces(commentToken);
               Type type = statement.getType();
               String typeName = document.get(type.getStartPosition(), type.getLength());
               copy.setType((Type)fRewriter.createStringPlaceholder(commentToken + typeName, type.getNodeType()));
            }
            result.add(copy);
            // convert the fragments into expression statements
            fragments = statement.fragments();
            if (!fragments.isEmpty())
            {
               List<ExpressionStatement> newExpressionStatements = new ArrayList<ExpressionStatement>();
               for (Iterator<VariableDeclarationFragment> iter = fragments.iterator(); iter.hasNext();)
               {
                  VariableDeclarationFragment fragment = iter.next();
                  Expression initializer = fragment.getInitializer();
                  if (initializer != null)
                  {
                     Assignment assignment = ast.newAssignment();
                     assignment.setLeftHandSide((Expression)fRewriter.createCopyTarget(fragment.getName()));
                     assignment.setRightHandSide((Expression)fRewriter.createCopyTarget(initializer));
                     newExpressionStatements.add(ast.newExpressionStatement(assignment));
                  }
               }
               if (!newExpressionStatements.isEmpty())
               {
                  if (fSelectedNodes.length == 1)
                  {
                     expressionStatement =
                        fRewriter.createGroupNode(newExpressionStatements.toArray(new ASTNode[newExpressionStatements
                           .size()]));
                  }
                  else
                  {
                     fRewriter.replace(statement, fRewriter.createGroupNode(newExpressionStatements
                        .toArray(new ASTNode[newExpressionStatements.size()])), null);
                  }
               }
               else
               {
                  fRewriter.remove(statement, null);
                  selectedNodeRemoved = true;
               }
            }
            else
            {
               fRewriter.remove(statement, null);
               selectedNodeRemoved = true;
            }
         }
      }
      result.add(tryStatement);
      ASTNode replacementNode;
      if (result.size() == 1)
      {
         replacementNode = result.get(0);
      }
      else
      {
         replacementNode = fRewriter.createGroupNode(result.toArray(new ASTNode[result.size()]));
      }
      if (fSelectedNodes.length == 1)
      {
         if (expressionStatement != null)
         {
            statements.insertLast(expressionStatement, null);
         }
         else
         {
            if (!selectedNodeRemoved)
               statements.insertLast(fRewriter.createMoveTarget(fSelectedNodes[0]), null);
         }
         fRewriter.replace(fSelectedNodes[0], replacementNode, null);
      }
      else
      {
         ListRewrite source =
            fRewriter.getListRewrite(fSelectedNodes[0].getParent(),
               (ChildListPropertyDescriptor)fSelectedNodes[0].getLocationInParent());
         ASTNode toMove =
            source
               .createMoveTarget(fSelectedNodes[0], fSelectedNodes[fSelectedNodes.length - 1], replacementNode, null);
         statements.insertLast(toMove, null);
      }
   }

   private List<ASTNode> getSpecialVariableDeclarationStatements()
   {
      List<ASTNode> result = new ArrayList<ASTNode>(3);
      VariableDeclaration[] locals = fAnalyzer.getAffectedLocals();
      for (int i = 0; i < locals.length; i++)
      {
         ASTNode parent = locals[i].getParent();
         if (parent instanceof VariableDeclarationStatement && !result.contains(parent))
            result.add(parent);
      }
      return result;

   }

   private Statement getCatchBody(String type, String name, String lineSeparator) throws CoreException
   {
      String s = StubUtility.getCatchBodyContent(type, name, fSelectedNodes[0], lineSeparator);
      if (s == null)
      {
         return null;
      }
      else
      {
         return (Statement)fRewriter.createStringPlaceholder(s, ASTNode.RETURN_STATEMENT);
      }
   }

   /**
    * @see org.eclipse.jdt.client.ltk.refactoring.Refactoring#checkFinalConditions(org.eclipse.jdt.client.runtime.IProgressMonitor)
    */
   @Override
   public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException
   {
      // TODO Auto-generated method stub
      return null;
   }
}