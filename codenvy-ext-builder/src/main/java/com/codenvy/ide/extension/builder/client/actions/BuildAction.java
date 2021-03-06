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
package com.codenvy.ide.extension.builder.client.actions;

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.extension.builder.client.BuilderLocalizationConstant;
import com.codenvy.ide.extension.builder.client.BuilderResources;
import com.codenvy.ide.extension.builder.client.build.BuildController;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to build current project.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class BuildAction extends ProjectAction {

    private final BuildController      buildController;
    private final AnalyticsEventLogger eventLogger;
    private       BuildContext         buildContext;

    @Inject
    public BuildAction(BuildController buildController, BuilderResources resources,
                       BuilderLocalizationConstant localizationConstant,
                       AnalyticsEventLogger eventLogger, BuildContext buildContext) {
        super(localizationConstant.buildProjectControlTitle(),
              localizationConstant.buildProjectControlDescription(), resources.build());
        this.buildController = buildController;
        this.eventLogger = eventLogger;
        this.buildContext = buildContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        buildController.buildActiveProject(true);
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(appContext.getCurrentProject().getBuilder() != null && !buildContext.isBuilding());
    }
}
