/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.ext.java.jdi.client.actions;

import com.codenvy.api.runner.dto.ApplicationProcessDescriptor;
import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.api.ui.action.Action;
import com.codenvy.ide.api.ui.action.ActionEvent;
import com.codenvy.ide.collections.StringSet;
import com.codenvy.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import com.codenvy.ide.ext.java.jdi.client.JavaRuntimeResources;
import com.codenvy.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import com.codenvy.ide.extension.runner.client.ProjectRunCallback;
import com.codenvy.ide.extension.runner.client.RunnerController;
import com.codenvy.ide.resources.model.Project;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.codenvy.ide.ext.java.client.JavaExtension.JAVA_WEB_APPLICATION_PROJECT_TYPE;
import static com.codenvy.ide.ext.java.client.JavaExtension.SPRING_APPLICATION_PROJECT_TYPE;

/**
 * Action to run project on runner in debug mode.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class DebugAction extends Action {

    private RunnerController  runnerController;
    private DebuggerPresenter debuggerPresenter;
    private ResourceProvider  resourceProvider;

    @Inject
    public DebugAction(RunnerController runnerController, DebuggerPresenter debuggerPresenter, JavaRuntimeResources resources,
                       ResourceProvider resourceProvider, JavaRuntimeLocalizationConstant localizationConstants) {
        super(localizationConstants.debugAppActionText(), localizationConstants.debugAppActionDescription(), resources.debugApp());
        this.runnerController = runnerController;
        this.debuggerPresenter = debuggerPresenter;
        this.resourceProvider = resourceProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        runnerController.runActiveProject(true, new ProjectRunCallback() {
            @Override
            public void onRun(ApplicationProcessDescriptor appDescriptor) {
                debuggerPresenter.attachDebugger(appDescriptor);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        Project activeProject = resourceProvider.getActiveProject();
        if (activeProject != null) {
            StringSet natures = activeProject.getDescription().getNatures();
            e.getPresentation().setVisible(natures.contains(SPRING_APPLICATION_PROJECT_TYPE)
                                           || natures.contains(JAVA_WEB_APPLICATION_PROJECT_TYPE));
            e.getPresentation().setEnabled(!runnerController.isAnyAppLaunched());
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}
