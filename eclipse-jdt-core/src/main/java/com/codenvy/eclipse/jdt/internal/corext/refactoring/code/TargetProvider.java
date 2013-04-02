/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dmitry Stalnov (dstalnov@fusionone.com) - contributed fixes for:
 *       o Allow 'this' constructor to be inlined
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38093)
 *******************************************************************************/
package com.codenvy.eclipse.jdt.internal.corext.refactoring.code;

import com.codenvy.eclipse.core.runtime.Assert;
import com.codenvy.eclipse.core.runtime.CoreException;
import com.codenvy.eclipse.core.runtime.IProgressMonitor;
import com.codenvy.eclipse.core.runtime.SubProgressMonitor;
import com.codenvy.eclipse.jdt.core.ICompilationUnit;
import com.codenvy.eclipse.jdt.core.IMethod;
import com.codenvy.eclipse.jdt.core.IType;
import com.codenvy.eclipse.jdt.core.JavaModelException;
import com.codenvy.eclipse.jdt.core.SourceRange;
import com.codenvy.eclipse.jdt.core.dom.AST;
import com.codenvy.eclipse.jdt.core.dom.ASTNode;
import com.codenvy.eclipse.jdt.core.dom.ASTVisitor;
import com.codenvy.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import com.codenvy.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import com.codenvy.eclipse.jdt.core.dom.Block;
import com.codenvy.eclipse.jdt.core.dom.BodyDeclaration;
import com.codenvy.eclipse.jdt.core.dom.ClassInstanceCreation;
import com.codenvy.eclipse.jdt.core.dom.ConstructorInvocation;
import com.codenvy.eclipse.jdt.core.dom.EnumDeclaration;
import com.codenvy.eclipse.jdt.core.dom.FieldDeclaration;
import com.codenvy.eclipse.jdt.core.dom.IBinding;
import com.codenvy.eclipse.jdt.core.dom.IMethodBinding;
import com.codenvy.eclipse.jdt.core.dom.ITypeBinding;
import com.codenvy.eclipse.jdt.core.dom.Initializer;
import com.codenvy.eclipse.jdt.core.dom.MethodDeclaration;
import com.codenvy.eclipse.jdt.core.dom.MethodInvocation;
import com.codenvy.eclipse.jdt.core.dom.SuperMethodInvocation;
import com.codenvy.eclipse.jdt.core.dom.TypeDeclaration;
import com.codenvy.eclipse.jdt.core.search.IJavaSearchConstants;
import com.codenvy.eclipse.jdt.core.search.IJavaSearchScope;
import com.codenvy.eclipse.jdt.core.search.SearchEngine;
import com.codenvy.eclipse.jdt.core.search.SearchMatch;
import com.codenvy.eclipse.jdt.core.search.SearchPattern;
import com.codenvy.eclipse.jdt.internal.corext.dom.ASTNodes;
import com.codenvy.eclipse.jdt.internal.corext.refactoring.CollectingSearchRequestor;
import com.codenvy.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import com.codenvy.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import com.codenvy.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import com.codenvy.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import com.codenvy.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import com.codenvy.eclipse.jdt.internal.corext.util.SearchUtils;
import com.codenvy.eclipse.ltk.core.refactoring.RefactoringStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/** A TargetProvider provides all targets that have to be adapted, i.e. all method invocations that should be inlined. */
abstract class TargetProvider {

    public abstract void initialize();

    public abstract ICompilationUnit[] getAffectedCompilationUnits(RefactoringStatus status,
                                                                   ReferencesInBinaryContext binaryRefs, IProgressMonitor pm)
            throws CoreException;

    public abstract BodyDeclaration[] getAffectedBodyDeclarations(ICompilationUnit unit, IProgressMonitor pm);

    // constructor invocation is not an expression but a statement
    public abstract ASTNode[] getInvocations(BodyDeclaration declaration, IProgressMonitor pm);

    public abstract RefactoringStatus checkActivation() throws JavaModelException;

    public abstract int getStatusSeverity();

    public boolean isSingle() {
        return false;
    }

    public static TargetProvider create(ICompilationUnit cu, MethodInvocation invocation) {
        return new SingleCallTargetProvider(cu, invocation);
    }

    public static TargetProvider create(ICompilationUnit cu, SuperMethodInvocation invocation) {
        return new SingleCallTargetProvider(cu, invocation);
    }

    public static TargetProvider create(ICompilationUnit cu, ConstructorInvocation invocation) {
        return new SingleCallTargetProvider(cu, invocation);
    }

    public static TargetProvider create(MethodDeclaration declaration) {
        IMethodBinding method = declaration.resolveBinding();
        if (method == null) {
            return new ErrorTargetProvider(RefactoringStatus.createFatalErrorStatus(
                    RefactoringCoreMessages.TargetProvider_method_declaration_not_unique));
        }
        ITypeBinding type = method.getDeclaringClass();
        if (type.isLocal()) {
            if (((IType)type.getJavaElement()).isBinary()) {
                return new ErrorTargetProvider(RefactoringStatus.createFatalErrorStatus(
                        RefactoringCoreMessages.TargetProvider_cannot_local_method_in_binary));
            } else {
                IType declaringClassOfLocal = (IType)type.getDeclaringClass().getJavaElement();
                return new LocalTypeTargetProvider(declaringClassOfLocal.getCompilationUnit(), declaration);
            }
        } else {
            return new MemberTypeTargetProvider(declaration.resolveBinding());
        }
    }

    public static TargetProvider create(IMethodBinding methodBinding) {
        return new MemberTypeTargetProvider(methodBinding);
    }

    static void fastDone(IProgressMonitor pm) {
        if (pm == null) {
            return;
        }
        pm.beginTask("", 1); //$NON-NLS-1$
        pm.worked(1);
        pm.done();
    }

    static class ErrorTargetProvider extends TargetProvider {
        private RefactoringStatus fErrorStatus;

        public ErrorTargetProvider(RefactoringStatus status) {
            fErrorStatus = status;
        }

        @Override
        public RefactoringStatus checkActivation() throws JavaModelException {
            return fErrorStatus;
        }

        @Override
        public void initialize() {
        }

        @Override
        public ICompilationUnit[] getAffectedCompilationUnits(RefactoringStatus status,
                                                              ReferencesInBinaryContext binaryRefs, IProgressMonitor pm)
                throws JavaModelException {
            return null;
        }

        @Override
        public BodyDeclaration[] getAffectedBodyDeclarations(ICompilationUnit unit, IProgressMonitor pm) {
            return null;
        }

        @Override
        public ASTNode[] getInvocations(BodyDeclaration declaration, IProgressMonitor pm) {
            return null;
        }

        @Override
        public int getStatusSeverity() {
            return 0;
        }
    }

    static class SingleCallTargetProvider extends TargetProvider {
        private ICompilationUnit fCUnit;

        private ASTNode fInvocation;

        private boolean fIterated;

        public SingleCallTargetProvider(ICompilationUnit cu, ASTNode invocation) {
            Assert.isNotNull(cu);
            Assert.isNotNull(invocation);
            Assert.isTrue(Invocations.isInvocation(invocation));
            fCUnit = cu;
            fInvocation = invocation;
        }

        @Override
        public void initialize() {
            fIterated = false;
        }

        @Override
        public ICompilationUnit[] getAffectedCompilationUnits(RefactoringStatus status,
                                                              ReferencesInBinaryContext binaryRefs, IProgressMonitor pm) {
            return new ICompilationUnit[]{fCUnit};
        }

        @Override
        public BodyDeclaration[] getAffectedBodyDeclarations(ICompilationUnit unit, IProgressMonitor pm) {
            Assert.isTrue(unit == fCUnit);
            if (fIterated) {
                return new BodyDeclaration[0];
            }
            fastDone(pm);
            return new BodyDeclaration[]{(BodyDeclaration)ASTNodes.getParent(fInvocation, BodyDeclaration.class)};
        }

        @Override
        public ASTNode[] getInvocations(BodyDeclaration declaration, IProgressMonitor pm) {
            fastDone(pm);
            if (fIterated) {
                return null;
            }
            fIterated = true;
            return new ASTNode[]{fInvocation};
        }

        @Override
        public RefactoringStatus checkActivation() throws JavaModelException {
            return new RefactoringStatus();
        }

        @Override
        public int getStatusSeverity() {
            return RefactoringStatus.FATAL;
        }

        @Override
        public boolean isSingle() {
            return true;
        }
    }

    private static class BodyData {
        private List<ASTNode> fInvocations;

        public BodyData() {
        }

        public void addInvocation(ASTNode node) {
            if (fInvocations == null) {
                fInvocations = new ArrayList<ASTNode>(2);
            }
            fInvocations.add(node);
        }

        public ASTNode[] getInvocations() {
            return fInvocations.toArray(new ASTNode[fInvocations.size()]);
        }

        public boolean hasInvocations() {
            return fInvocations != null && !fInvocations.isEmpty();
        }
    }

    private static class InvocationFinder extends ASTVisitor {
        Map<BodyDeclaration, BodyData> result = new HashMap<BodyDeclaration, BodyData>(2);

        Stack<BodyData> fBodies = new Stack<BodyData>();

        BodyData fCurrent;

        private IMethodBinding fBinding;

        public InvocationFinder(IMethodBinding binding) {
            Assert.isNotNull(binding);
            fBinding = binding.getMethodDeclaration();
            Assert.isNotNull(fBinding);
        }

        @Override
        public boolean visit(MethodInvocation node) {
            if (node.resolveTypeBinding() != null && matches(node.resolveMethodBinding()) && fCurrent != null) {
                fCurrent.addInvocation(node);
            }
            return true;
        }

        @Override
        public boolean visit(SuperMethodInvocation node) {
            if (matches(node.getName().resolveBinding()) && fCurrent != null) {
                fCurrent.addInvocation(node);
            }
            return true;
        }

        @Override
        public boolean visit(ConstructorInvocation node) {
            if (matches(node.resolveConstructorBinding()) && fCurrent != null) {
                fCurrent.addInvocation(node);
            }
            return true;
        }

        @Override
        public boolean visit(ClassInstanceCreation node) {
            if (matches(node.resolveConstructorBinding()) && fCurrent != null) {
                fCurrent.addInvocation(node);
            }
            return true;
        }

        @Override
        public boolean visit(TypeDeclaration node) {
            return visitType();
        }

        @Override
        public void endVisit(TypeDeclaration node) {
            endVisitBodyDeclaration();
        }

        @Override
        public boolean visit(EnumDeclaration node) {
            return visitType();
        }

        @Override
        public void endVisit(EnumDeclaration node) {
            endVisitBodyDeclaration();
        }

        @Override
        public boolean visit(AnnotationTypeDeclaration node) {
            return visitType();
        }

        @Override
        public void endVisit(AnnotationTypeDeclaration node) {
            endVisitBodyDeclaration();
        }

        private boolean visitType() {
            fBodies.add(fCurrent);
            fCurrent = null;
            return true;
        }

        protected boolean visitNonTypeBodyDeclaration() {
            fBodies.add(fCurrent);
            fCurrent = new BodyData();
            return true;
        }

        protected void endVisitBodyDeclaration() {
            fCurrent = fBodies.remove(fBodies.size() - 1);
        }

        @Override
        public boolean visit(FieldDeclaration node) {
            return visitNonTypeBodyDeclaration();
        }

        @Override
        public void endVisit(FieldDeclaration node) {
            if (fCurrent.hasInvocations()) {
                result.put(node, fCurrent);
            }
            endVisitBodyDeclaration();
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            return visitNonTypeBodyDeclaration();
        }

        @Override
        public void endVisit(MethodDeclaration node) {
            if (fCurrent.hasInvocations()) {
                result.put(node, fCurrent);
            }
            endVisitBodyDeclaration();

        }

        @Override
        public boolean visit(Initializer node) {
            return visitNonTypeBodyDeclaration();
        }

        @Override
        public void endVisit(Initializer node) {
            if (fCurrent.hasInvocations()) {
                result.put(node, fCurrent);
            }
            endVisitBodyDeclaration();
        }

        private boolean matches(IBinding binding) {
            if (!(binding instanceof IMethodBinding)) {
                return false;
            }
            return fBinding.isEqualTo(((IMethodBinding)binding).getMethodDeclaration());
        }
    }

    private static class LocalTypeTargetProvider extends TargetProvider {
        private ICompilationUnit fCUnit;

        private MethodDeclaration fDeclaration;

        private Map<BodyDeclaration, BodyData> fBodies;

        public LocalTypeTargetProvider(ICompilationUnit unit, MethodDeclaration declaration) {
            Assert.isNotNull(unit);
            Assert.isNotNull(declaration);
            fCUnit = unit;
            fDeclaration = declaration;
        }

        @Override
        public void initialize() {
            IMethodBinding methodBinding = fDeclaration.resolveBinding();
            InvocationFinder finder;
            ASTNode type = ASTNodes.getParent(fDeclaration, AbstractTypeDeclaration.class);
            if (methodBinding.getDeclaringClass().isAnonymous()) {
                finder = new InvocationFinder(methodBinding);
                type.accept(finder);
            } else {
                //scope of local class is enclosing block
                ASTNode block = type.getParent().getParent();
                finder = new InvocationFinder(methodBinding) {
                    @Override
                    public boolean visit(Block node) {
                        return visitNonTypeBodyDeclaration();
                    }

                    @Override
                    public void endVisit(Block node) {
                        if (fCurrent.hasInvocations()) {
                            result.put((BodyDeclaration)ASTNodes.getParent(node, BodyDeclaration.class), fCurrent);
                        }
                        endVisitBodyDeclaration();
                    }
                };
                block.accept(finder);
            }
            fBodies = finder.result;
        }

        @Override
        public ICompilationUnit[] getAffectedCompilationUnits(RefactoringStatus status,
                                                              ReferencesInBinaryContext binaryRefs, IProgressMonitor pm) {
            fastDone(pm);
            return new ICompilationUnit[]{fCUnit};
        }

        @Override
        public BodyDeclaration[] getAffectedBodyDeclarations(ICompilationUnit unit, IProgressMonitor pm) {
            Assert.isTrue(unit == fCUnit);
            Set<BodyDeclaration> result = fBodies.keySet();
            fastDone(pm);
            return result.toArray(new BodyDeclaration[result.size()]);
        }

        @Override
        public ASTNode[] getInvocations(BodyDeclaration declaration, IProgressMonitor pm) {
            BodyData data = fBodies.get(declaration);
            Assert.isNotNull(data);
            fastDone(pm);
            return data.getInvocations();
        }

        @Override
        public RefactoringStatus checkActivation() throws JavaModelException {
            return new RefactoringStatus();
        }

        @Override
        public int getStatusSeverity() {
            return RefactoringStatus.ERROR;
        }
    }

    private static class MemberTypeTargetProvider extends TargetProvider {
        private final IMethodBinding fMethodBinding;

        private Map<BodyDeclaration, BodyData> fCurrentBodies;

        public MemberTypeTargetProvider(IMethodBinding methodBinding) {
            Assert.isNotNull(methodBinding);
            fMethodBinding = methodBinding;
        }

        @Override
        public void initialize() {
            // do nothing.
        }

        @Override
        public ICompilationUnit[] getAffectedCompilationUnits(final RefactoringStatus status,
                                                              ReferencesInBinaryContext binaryRefs, IProgressMonitor pm)
                throws CoreException {
            IMethod method = (IMethod)fMethodBinding.getJavaElement();
            Assert.isTrue(method != null);

            SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES,
                                                                SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
            IJavaSearchScope scope = RefactoringScopeFactory.create(method, true, false);
            final HashSet<ICompilationUnit> affectedCompilationUnits = new HashSet<ICompilationUnit>();
            CollectingSearchRequestor requestor = new CollectingSearchRequestor(binaryRefs) {
                private ICompilationUnit fLastCU;

                @Override
                public void acceptSearchMatch(SearchMatch match) throws CoreException {
                    if (filterMatch(match)) {
                        return;
                    }
                    if (match.isInsideDocComment()) {
                        return; // TODO: should warn user (with something like a ReferencesInBinaryContext)
                    }

                    ICompilationUnit unit = SearchUtils.getCompilationUnit(match);
                    if (match.getAccuracy() == SearchMatch.A_INACCURATE) {
                        if (unit != null) {
                            status.addError(RefactoringCoreMessages.TargetProvider_inaccurate_match,
                                            JavaStatusContext.create(unit, new SourceRange(match.getOffset(), match.getLength())));
                        } else {
                            status.addError(RefactoringCoreMessages.TargetProvider_inaccurate_match);
                        }
                    } else if (unit != null) {
                        if (!unit.equals(fLastCU)) {
                            fLastCU = unit;
                            affectedCompilationUnits.add(unit);
                        }
                    }
                }
            };
            new SearchEngine().search(pattern, SearchUtils.getDefaultSearchParticipants(), scope, requestor,
                                      new SubProgressMonitor(pm, 1));
            return affectedCompilationUnits.toArray(new ICompilationUnit[affectedCompilationUnits.size()]);
        }

        @Override
        public BodyDeclaration[] getAffectedBodyDeclarations(ICompilationUnit unit, IProgressMonitor pm) {
            ASTNode root = new RefactoringASTParser(AST.JLS4).parse(unit, true);
            InvocationFinder finder = new InvocationFinder(fMethodBinding);
            root.accept(finder);
            fCurrentBodies = finder.result;
            Set<BodyDeclaration> result = fCurrentBodies.keySet();
            fastDone(pm);
            return result.toArray(new BodyDeclaration[result.size()]);
        }

        @Override
        public ASTNode[] getInvocations(BodyDeclaration declaration, IProgressMonitor pm) {
            BodyData data = fCurrentBodies.get(declaration);
            Assert.isNotNull(data);
            fastDone(pm);
            return data.getInvocations();
        }

        @Override
        public RefactoringStatus checkActivation() throws JavaModelException {
            return new RefactoringStatus();
        }

        @Override
        public int getStatusSeverity() {
            return RefactoringStatus.ERROR;
        }
    }
}
