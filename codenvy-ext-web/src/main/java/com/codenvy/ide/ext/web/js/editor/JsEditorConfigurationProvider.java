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
package com.codenvy.ide.ext.web.js.editor;

import java.util.Set;

import javax.inject.Provider;

import com.codenvy.ide.ext.web.html.editor.AutoEditStrategyFactory;
import com.google.inject.Inject;

/**
 * Provider for HTML Editor configuration.
 * 
 * @author Florent Benoit
 */

public class JsEditorConfigurationProvider implements Provider<JsEditorConfiguration> {

    /**
     * Auto Edit strategies
     */
    @Inject(optional = true)
    private Set<AutoEditStrategyFactory> autoEditStrategyFactories;

    @Inject
    private DefaultCodeAssistProcessor chainedCodeAssistProcessor;


    /**
     * Build a new instance of JsEditor Configuration
     * 
     * @return
     */
    @Override
    public JsEditorConfiguration get() {
        return new JsEditorConfiguration(autoEditStrategyFactories, chainedCodeAssistProcessor);
    }
}
