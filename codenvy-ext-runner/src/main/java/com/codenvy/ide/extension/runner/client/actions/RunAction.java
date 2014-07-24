/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.runner.client.actions;

import com.codenvy.api.analytics.logger.AnalyticsEventLogger;
import com.codenvy.api.runner.ApplicationStatus;
import com.codenvy.api.runner.dto.ApplicationProcessDescriptor;
import com.codenvy.ide.api.AppContext;
import com.codenvy.ide.api.CurrentProject;
import com.codenvy.ide.api.ui.action.Action;
import com.codenvy.ide.api.ui.action.ActionEvent;
import com.codenvy.ide.extension.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.extension.runner.client.RunnerResources;
import com.codenvy.ide.extension.runner.client.run.RunnerController;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to run project on runner.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class RunAction extends Action {


    private final RunnerController     runnerController;
    private final AnalyticsEventLogger eventLogger;
    private       AppContext           appContext;

    @Inject
    public RunAction(RunnerController runnerController,
                     RunnerResources resources,
                     RunnerLocalizationConstant localizationConstants,
                     AnalyticsEventLogger eventLogger,
                     AppContext appContext) {
        super(localizationConstants.runAppActionText(),
              localizationConstants.runAppActionDescription(),
              null,
              resources.launchApp());
        this.runnerController = runnerController;
        this.eventLogger = eventLogger;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log("IDE: Run application");
        runnerController.runActiveProject(true);
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            // If project has defined a runner, let see the action
            e.getPresentation().setVisible(currentProject.getAttributeValue("runner.name") != null
                                           || currentProject.getAttributeValue("runner.user_defined_launcher") != null);
            ApplicationProcessDescriptor processDescriptor = currentProject.getProcessDescriptor();
            Log.info(RunAction.class, !isAppRunning(processDescriptor));

            Log.info(RunAction.class, currentProject.getIsRunningEnabled());
            e.getPresentation().setEnabled(!isAppRunning(processDescriptor) &&
                                           currentProject.getIsRunningEnabled());
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    private boolean isAppRunning(ApplicationProcessDescriptor processDescriptor) {
        if (processDescriptor == null)
            return false;
        return processDescriptor.getStatus().equals(ApplicationStatus.RUNNING) ||
               processDescriptor.getStatus().equals(ApplicationStatus.NEW);
    }
}
