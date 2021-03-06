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
import com.codenvy.ide.navigation.NavigateToFilePresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action for finding file by name and opening it.
 *
 * @author Ann Shumilova
 */
@Singleton
public class NavigateToFileAction extends Action {

    private final NavigateToFilePresenter presenter;
    private final AppContext              appContext;
    private final AnalyticsEventLogger    eventLogger;

    @Inject
    public NavigateToFileAction(NavigateToFilePresenter presenter,
                                AppContext appContext,
                                AnalyticsEventLogger eventLogger, Resources resources) {
        super("Navigate to File", "Navigate to file", null, resources.navigateToFile());
        this.presenter = presenter;
        this.appContext = appContext;
        this.eventLogger = eventLogger;
    }


    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabled(appContext.getCurrentProject() != null);
    }
}
