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
package com.codenvy.ide.actions;

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.ide.Resources;
import com.codenvy.ide.api.action.Action;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.event.CloseCurrentProjectEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/** @author Andrey Plotnikov */
@Singleton
public class CloseProjectAction extends Action {

    private final AppContext           appContext;
    private final AnalyticsEventLogger eventLogger;
    private final EventBus             eventBus;

    @Inject
    public CloseProjectAction(AppContext appContext,
                              Resources resources,
                              AnalyticsEventLogger eventLogger,
                              EventBus eventBus) {
        super("Close Project", "Close project", null, resources.closeProject());
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.eventBus = eventBus;
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setVisible(appContext.getCurrentProject() != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        eventBus.fireEvent(new CloseCurrentProjectEvent());
    }
}
