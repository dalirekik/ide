/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *******************************************************************************/
package com.codenvy.ide.java.client.internal.compiler.ast;

import com.codenvy.ide.java.client.internal.compiler.ASTVisitor;
import com.codenvy.ide.java.client.internal.compiler.ClassFileConstants;
import com.codenvy.ide.java.client.internal.compiler.codegen.BranchLabel;
import com.codenvy.ide.java.client.internal.compiler.flow.FlowContext;
import com.codenvy.ide.java.client.internal.compiler.flow.FlowInfo;
import com.codenvy.ide.java.client.internal.compiler.flow.LoopingFlowContext;
import com.codenvy.ide.java.client.internal.compiler.impl.Constant;
import com.codenvy.ide.java.client.internal.compiler.lookup.BlockScope;
import com.codenvy.ide.java.client.internal.compiler.lookup.TypeBinding;
import com.codenvy.ide.java.client.internal.compiler.lookup.TypeIds;

public class WhileStatement extends Statement {

    public Expression condition;

    public Statement action;

    private BranchLabel breakLabel, continueLabel;

    int preCondInitStateIndex = -1;

    int condIfTrueInitStateIndex = -1;

    int mergedInitStateIndex = -1;

    public WhileStatement(Expression condition, Statement action, int s, int e) {

        this.condition = condition;
        this.action = action;
        // remember useful empty statement
        if (action instanceof EmptyStatement) {
            action.bits |= IsUsefulEmptyStatement;
        }
        this.sourceStart = s;
        this.sourceEnd = e;
    }

    @Override
    public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

        this.breakLabel = new BranchLabel();
        this.continueLabel = new BranchLabel();
        int initialComplaintLevel =
                (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE
                                                                   : Statement.NOT_COMPLAINED;

        Constant cst = this.condition.constant;
        boolean isConditionTrue = cst != Constant.NotAConstant && cst.booleanValue() == true;
        boolean isConditionFalse = cst != Constant.NotAConstant && cst.booleanValue() == false;

        cst = this.condition.optimizedBooleanConstant();
        boolean isConditionOptimizedTrue = cst != Constant.NotAConstant && cst.booleanValue() == true;
        boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && cst.booleanValue() == false;

        this.preCondInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
        LoopingFlowContext condLoopContext;
        FlowInfo condInfo = flowInfo.nullInfoLessUnconditionalCopy();

        // we need to collect the contribution to nulls of the coming paths through the
        // loop, be they falling through normally or branched to break, continue labels
        // or catch blocks
        condInfo =
                this.condition.analyseCode(currentScope, (condLoopContext =
                        new LoopingFlowContext(flowContext, flowInfo, this, null, null, currentScope)), condInfo);
        if ((this.condition.implicitConversion & TypeIds.UNBOXING) != 0) {
            this.condition.checkNPE(currentScope, flowContext, flowInfo);
        }

        LoopingFlowContext loopingContext;
        FlowInfo actionInfo;
        FlowInfo exitBranch;
        if (this.action == null
            || (this.action.isEmptyBlock() && currentScope.compilerOptions().complianceLevel <= ClassFileConstants.JDK1_3)) {
            condLoopContext.complainOnDeferredFinalChecks(currentScope, condInfo);
            condLoopContext.complainOnDeferredNullChecks(currentScope, condInfo.unconditionalInits());
            if (isConditionTrue) {
                return FlowInfo.DEAD_END;
            } else {
                FlowInfo mergedInfo = flowInfo.copy().addInitializationsFrom(condInfo.initsWhenFalse());
                if (isConditionOptimizedTrue) {
                    mergedInfo.setReachMode(FlowInfo.UNREACHABLE_OR_DEAD);
                }
                this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
                return mergedInfo;
            }
        } else {
            // in case the condition was inlined to false, record the fact that there is no way to reach any
            // statement inside the looping action
            loopingContext =
                    new LoopingFlowContext(flowContext, flowInfo, this, this.breakLabel, this.continueLabel, currentScope);
            if (isConditionFalse) {
                actionInfo = FlowInfo.DEAD_END;
            } else {
                actionInfo = condInfo.initsWhenTrue().copy();
                if (isConditionOptimizedFalse) {
                    actionInfo.setReachMode(FlowInfo.UNREACHABLE_OR_DEAD);
                }
            }

            // for computing local var attributes
            this.condIfTrueInitStateIndex =
                    currentScope.methodScope().recordInitializationStates(condInfo.initsWhenTrue());

            if (this.action.complainIfUnreachable(actionInfo, currentScope, initialComplaintLevel) < Statement.COMPLAINED_UNREACHABLE) {
                actionInfo = this.action.analyseCode(currentScope, loopingContext, actionInfo);
            }

            // code generation can be optimized when no need to continue in the loop
            exitBranch = flowInfo.copy();
            // need to start over from flowInfo so as to get null inits
            int combinedTagBits = actionInfo.tagBits & loopingContext.initsOnContinue.tagBits;
            if ((combinedTagBits & FlowInfo.UNREACHABLE) != 0) {
                if ((combinedTagBits & FlowInfo.UNREACHABLE_OR_DEAD) != 0) {
                    this.continueLabel = null;
                }
                exitBranch.addInitializationsFrom(condInfo.initsWhenFalse());
            } else {
                condLoopContext.complainOnDeferredFinalChecks(currentScope, condInfo);
                actionInfo = actionInfo.mergedWith(loopingContext.initsOnContinue.unconditionalInits());
                condLoopContext.complainOnDeferredNullChecks(currentScope, actionInfo);
                loopingContext.complainOnDeferredFinalChecks(currentScope, actionInfo);
                loopingContext.complainOnDeferredNullChecks(currentScope, actionInfo);
                exitBranch.addPotentialInitializationsFrom(actionInfo.unconditionalInits()).addInitializationsFrom(
                        condInfo.initsWhenFalse());
            }
            if (loopingContext.hasEscapingExceptions()) { // https://bugs.eclipse.org/bugs/show_bug.cgi?id=321926
                FlowInfo loopbackFlowInfo = flowInfo.copy();
                if (this.continueLabel != null) { // we do get to the bottom
                    loopbackFlowInfo.mergedWith(actionInfo.unconditionalCopy());
                }
                loopingContext.simulateThrowAfterLoopBack(loopbackFlowInfo);
            }
        }

        // end of loop
        FlowInfo mergedInfo =
                FlowInfo
                        .mergedOptimizedBranches((loopingContext.initsOnBreak.tagBits & FlowInfo.UNREACHABLE) != 0
                                                 ? loopingContext.initsOnBreak
                                                 : flowInfo.addInitializationsFrom(loopingContext.initsOnBreak),
                                                 // recover upstream null info
                                                 isConditionOptimizedTrue, exitBranch, isConditionOptimizedFalse,
                                                 !isConditionTrue /*while(true); unreachable(); */);
        this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
        return mergedInfo;
    }

    /**
     * While code generation
     *
     * @param currentScope
     *         com.codenvy.ide.java.client.internal.compiler.lookup.BlockScope
     */
    @Override
    public void generateCode(BlockScope currentScope) {

        if ((this.bits & IsReachable) == 0) {
            return;
        }
        Constant cst = this.condition.optimizedBooleanConstant();
        boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && cst.booleanValue() == false;
        if (isConditionOptimizedFalse) {
            this.condition.generateCode(currentScope, false);
            // May loose some local variable initializations : affecting the local variable attributes
            return;
        }

        // generate condition
        if (this.continueLabel == null) {
            // no need to reverse condition
            if (this.condition.constant == Constant.NotAConstant) {
                this.condition.generateOptimizedBoolean(currentScope, null, this.breakLabel, true);
            }
        }
        // generate the action
        BranchLabel actionLabel = new BranchLabel();
        if (this.action != null) {
            actionLabel.tagBits |= BranchLabel.USED;
            // Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
            if (this.condIfTrueInitStateIndex != -1) {
                // insert all locals initialized inside the condition into the action generated prior to the condition
            }
            this.action.generateCode(currentScope);
            // May loose some local variable initializations : affecting the local variable attributes
        }
        // output condition and branch back to the beginning of the repeated action
        if (this.continueLabel != null) {
            this.condition.generateOptimizedBoolean(currentScope, actionLabel, null, true);
        }
    }

    @Override
    public void resolve(BlockScope scope) {

        TypeBinding type = this.condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
        this.condition.computeConversion(scope, type, type);
        if (this.action != null) {
            this.action.resolve(scope);
        }
    }

    @Override
    public StringBuffer printStatement(int tab, StringBuffer output) {

        printIndent(tab, output).append("while ("); //$NON-NLS-1$
        this.condition.printExpression(0, output).append(')');
        if (this.action == null) {
            output.append(';');
        } else {
            this.action.printStatement(tab + 1, output);
        }
        return output;
    }

    @Override
    public void traverse(ASTVisitor visitor, BlockScope blockScope) {

        if (visitor.visit(this, blockScope)) {
            this.condition.traverse(visitor, blockScope);
            if (this.action != null) {
                this.action.traverse(visitor, blockScope);
            }
        }
        visitor.endVisit(this, blockScope);
    }
}