/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.workspace;

import com.codenvy.ide.api.parts.PartStack;
import com.codenvy.ide.api.parts.PartStackView;

/**
 * Gin factory for PartStack
 *
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public interface PartStackPresenterFactory {
    PartStack create(PartStackView view, WorkBenchPartController workBenchPartController);
}
