/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.eclipse.jdt.core.refactoring.participants;

/**
 * Interface to define the processor IDs provided by JDT refactorings.
 * <p/>
 * <p>
 * This interface declares static final fields only; it is not intended to be
 * implemented.
 * </p>
 *
 * @since 1.4
 */
public interface IRefactoringProcessorIds {

    /**
     * Processor ID of the Change Method Signature processor
     * (value <code>"org.eclipse.jdt.ui.changeMethodSignatureRefactoring"</code>).
     * <p/>
     * The Change Method Signature processor loads {@link ChangeMethodSignatureParticipant}s registered for the
     * <code>IMethod</code> whose signature is changed.
     */
    public static String CHANGE_METHOD_SIGNATURE_PROCESSOR = "org.eclipse.jdt.ui.changeMethodSignatureRefactoring"; //$NON-NLS-1$

    /**
     * Processor ID of the Introduce Parameter Object processor
     * (value <code>"org.eclipse.jdt.ui.introduceParameterObjectRefactoring"</code>).
     * <p/>
     * The Introduce Parameter Object processor loads {@link ChangeMethodSignatureParticipant}s registered for the
     * <code>IMethod</code> whose signature is changed.
     */
    public static String INTRODUCE_PARAMETER_OBJECT_PROCESSOR = "org.eclipse.jdt.ui.introduceParameterObjectRefactoring"; //$NON-NLS-1$
}
