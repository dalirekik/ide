/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.exoplatform.ide.java.client.internal.compiler.ast;

import org.exoplatform.ide.java.client.internal.compiler.ASTVisitor;
import org.exoplatform.ide.java.client.internal.compiler.ClassFileConstants;
import org.exoplatform.ide.java.client.internal.compiler.flow.FlowContext;
import org.exoplatform.ide.java.client.internal.compiler.flow.FlowInfo;
import org.exoplatform.ide.java.client.internal.compiler.lookup.BlockScope;
import org.exoplatform.ide.java.client.internal.compiler.lookup.TypeBinding;
import org.exoplatform.ide.java.client.internal.compiler.lookup.TypeIds;

public class ThrowStatement extends Statement
{

   public Expression exception;

   public TypeBinding exceptionType;

   public ThrowStatement(Expression exception, int sourceStart, int sourceEnd)
   {
      this.exception = exception;
      this.sourceStart = sourceStart;
      this.sourceEnd = sourceEnd;
   }

   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo)
   {
      this.exception.analyseCode(currentScope, flowContext, flowInfo);
      this.exception.checkNPE(currentScope, flowContext, flowInfo);
      // need to check that exception thrown is actually caught somewhere
      flowContext.checkExceptionHandlers(this.exceptionType, this, flowInfo, currentScope);
      return FlowInfo.DEAD_END;
   }

   /**
    * Throw code generation
    *
    * @param currentScope org.exoplatform.ide.java.client.internal.compiler.lookup.BlockScope
    * @param codeStream org.exoplatform.ide.java.client.internal.compiler.codegen.CodeStream
    */
   public void generateCode(BlockScope currentScope)
   {
      if ((this.bits & ASTNode.IsReachable) == 0)
         return;
      this.exception.generateCode(currentScope, true);
   }

   public StringBuffer printStatement(int indent, StringBuffer output)
   {
      printIndent(indent, output).append("throw "); //$NON-NLS-1$
      this.exception.printExpression(0, output);
      return output.append(';');
   }

   public void resolve(BlockScope scope)
   {
      this.exceptionType = this.exception.resolveType(scope);
      if (this.exceptionType != null && this.exceptionType.isValidBinding())
      {
         if (this.exceptionType == TypeBinding.NULL)
         {
            if (scope.compilerOptions().complianceLevel <= ClassFileConstants.JDK1_3)
            {
               // if compliant with 1.4, this problem will not be reported
               scope.problemReporter().cannotThrowNull(this.exception);
            }
         }
         else if (this.exceptionType.findSuperTypeOriginatingFrom(TypeIds.T_JavaLangThrowable, true) == null)
         {
            scope.problemReporter().cannotThrowType(this.exception, this.exceptionType);
         }
         this.exception.computeConversion(scope, this.exceptionType, this.exceptionType);
      }
   }

   public void traverse(ASTVisitor visitor, BlockScope blockScope)
   {
      if (visitor.visit(this, blockScope))
         this.exception.traverse(visitor, blockScope);
      visitor.endVisit(this, blockScope);
   }
}