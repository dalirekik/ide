/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.eclipse.jdt.internal.compiler.impl;

/*
 * Implementors are valid compilation contexts from which we can
 * escape in case of error:
 *	For example: method, type or compilation unit.
 */

import com.codenvy.eclipse.jdt.core.compiler.CategorizedProblem;
import com.codenvy.eclipse.jdt.internal.compiler.CompilationResult;
import com.codenvy.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

public interface ReferenceContext {

    void abort(int abortLevel, CategorizedProblem problem);

    CompilationResult compilationResult();

    CompilationUnitDeclaration getCompilationUnitDeclaration();

    boolean hasErrors();

    void tagAsHavingErrors();

}
