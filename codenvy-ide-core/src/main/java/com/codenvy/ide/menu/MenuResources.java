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
package com.codenvy.ide.menu;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Evgen Vidolob
 */
public interface MenuResources extends ClientBundle {
    public interface Css extends CssResource {

        String menuBar();

        String menuBarTable();

        String menuBarItem();

        String menuBarItemSelected();

        String menuBarItemDisabled();

        String menuBarItemOver();

        String leftPanel();

        String rightPanel();

        String customComponent();

        String panelSeparator();
    }

    @Source({"menu.css", "com/codenvy/ide/api/ui/style.css"})
    Css menuCss();
}
