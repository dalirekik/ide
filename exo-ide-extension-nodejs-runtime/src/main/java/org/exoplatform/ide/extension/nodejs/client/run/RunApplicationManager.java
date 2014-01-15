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
package org.exoplatform.ide.extension.nodejs.client.run;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.ServerException;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.AutoBeanUnmarshaller;
import org.exoplatform.gwtframework.commons.rest.HTTPStatus;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.control.Docking;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.project.ProjectClosedEvent;
import org.exoplatform.ide.client.framework.project.ProjectClosedHandler;
import org.exoplatform.ide.client.framework.project.ProjectOpenedEvent;
import org.exoplatform.ide.client.framework.project.ProjectOpenedHandler;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.rest.AutoBeanUnmarshallerWS;
import org.exoplatform.ide.client.framework.websocket.rest.RequestCallback;
import org.exoplatform.ide.extension.nodejs.client.NodeJsRuntimeExtension;
import org.exoplatform.ide.extension.nodejs.client.NodeJsRuntimeService;
import org.exoplatform.ide.extension.nodejs.client.run.event.ApplicationStartedEvent;
import org.exoplatform.ide.extension.nodejs.client.run.event.ApplicationStoppedEvent;
import org.exoplatform.ide.extension.nodejs.client.run.event.RunApplicationEvent;
import org.exoplatform.ide.extension.nodejs.client.run.event.RunApplicationHandler;
import org.exoplatform.ide.extension.nodejs.client.run.event.StopApplicationEvent;
import org.exoplatform.ide.extension.nodejs.client.run.event.StopApplicationHandler;
import org.exoplatform.ide.extension.nodejs.shared.ApplicationInstance;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

import com.google.gwt.http.client.RequestException;
import com.google.web.bindery.autobean.shared.AutoBean;

/**
 * Manager for running/stopping Node.js application.
 * 
 * @author <a href="mailto:vsvydenko@codenvy.com">Valeriy Svydenko</a>
 * @version $Id: RunApplicationManager.java Apr 18, 2013 4:13:40 PM vsvydenko $
 *
 */
public class RunApplicationManager implements RunApplicationHandler, StopApplicationHandler,
        VfsChangedHandler, ProjectOpenedHandler, ProjectClosedHandler {
    
    private ProjectModel currentProject;

    private VirtualFileSystemInfo currentVfs;

    /** Run application. */
    private ApplicationInstance runApplication;

    public RunApplicationManager() {
        IDE.getInstance().addControl(new StopApplicationControl());
        IDE.getInstance().addControl(new RunApplicationControl(), Docking.TOOLBAR_RIGHT);

        IDE.addHandler(RunApplicationEvent.TYPE, this);
        IDE.addHandler(StopApplicationEvent.TYPE, this);
        IDE.addHandler(VfsChangedEvent.TYPE, this);
        IDE.addHandler(ProjectOpenedEvent.TYPE, this);
        IDE.addHandler(ProjectClosedEvent.TYPE, this);
    }

    /** @see org.exoplatform.ide.extension.nodejs.client.run.event.StopApplicationHandler#onStopApplication(org.exoplatform.ide.extension.nodejs.client.run.event.StopApplicationEvent) */
    @Override
    public void onStopApplication(StopApplicationEvent event) {
        if (runApplication != null) {
            stopApplication();
        }
    }

    /** @see org.exoplatform.ide.extension.nodejs.client.run.event.RunApplicationHandler#onRunApplication(org.exoplatform.ide.extension.nodejs.client.run.event.RunApplicationEvent) */
    @Override
    public void onRunApplication(RunApplicationEvent event) {
        if (currentProject != null && ProjectType.NODE_JS.value().equals(currentProject.getProjectType())) {
            runApplication();
        } else {
            Dialogs.getInstance().showInfo(NodeJsRuntimeExtension.NODEJS_LOCALIZATION.notNodeJsProject());
        }
    }

    /** @see org.exoplatform.ide.client.framework.project.ProjectClosedHandler#onProjectClosed(org.exoplatform.ide.client.framework
     * .project.ProjectClosedEvent) */
    @Override
    public void onProjectClosed(ProjectClosedEvent event) {
        this.currentProject = null;
    }

    /** @see org.exoplatform.ide.client.framework.project.ProjectOpenedHandler#onProjectOpened(org.exoplatform.ide.client.framework
     * .project.ProjectOpenedEvent) */
    @Override
    public void onProjectOpened(ProjectOpenedEvent event) {
        this.currentProject = event.getProject();
    }

    /** @see org.exoplatform.ide.client.framework.application.event.VfsChangedHandler#onVfsChanged(org.exoplatform.ide.client.framework
     * .application.event.VfsChangedEvent) */
    @Override
    public void onVfsChanged(VfsChangedEvent event) {
        this.currentVfs = event.getVfsInfo();
    }

    /** Run Node.js application. */
    private void runApplication() {
        AutoBean<ApplicationInstance> autoBean =
                NodeJsRuntimeExtension.AUTO_BEAN_FACTORY.create(ApplicationInstance.class);
        AutoBeanUnmarshallerWS<ApplicationInstance> unmarshaller = new AutoBeanUnmarshallerWS<ApplicationInstance>(autoBean);

        try {
            IDE.fireEvent(new OutputEvent(NodeJsRuntimeExtension.NODEJS_LOCALIZATION.startingProjectMessage(currentProject
                                                                                                                    .getName()),
                                          Type.INFO));
            NodeJsRuntimeService.getInstance().start(currentVfs.getId(), currentProject,
                                                     new RequestCallback<ApplicationInstance>(unmarshaller) {
                                                         @Override
                                                         protected void onSuccess(ApplicationInstance result) {
                                                             runApplication = result;
                                                             IDE.fireEvent(new ApplicationStartedEvent(runApplication));
                                                             String url =
                                                                     (result.getHost().startsWith("http://")) ? result.getHost()
                                                                                                              : "http://" +
                                                                                                                result.getHost();
                                                             String link = "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>";
                                                             IDE.fireEvent(new OutputEvent(
                                                                     NodeJsRuntimeExtension.NODEJS_LOCALIZATION.applicationStartedUrl(
                                                                             result.getName(), link), Type.INFO));
                                                         }

                                                         @Override
                                                         protected void onFailure(Throwable exception) {
                                                             String message =
                                                                     (exception.getMessage() != null && !exception.getMessage().isEmpty()) ?
                                                                     " : "
                                                                     + exception.getMessage() : "";
                                                             IDE.fireEvent(new OutputEvent(
                                                                     NodeJsRuntimeExtension.NODEJS_LOCALIZATION.startApplicationFailed()
                                                                     + message, OutputMessage.Type.ERROR));
                                                         }
                                                     });
        } catch (WebSocketException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    /** Stop Node.js application. */
    private void stopApplication() {
        try {
            IDE.fireEvent(new OutputEvent(NodeJsRuntimeExtension.NODEJS_LOCALIZATION.stoppingProjectMessage(runApplication
                                                                                                                    .getName()),
                                          Type.INFO));
            NodeJsRuntimeService.getInstance().stop(runApplication.getName(), new AsyncRequestCallback<Object>() {

                @Override
                protected void onSuccess(Object result) {
                    IDE.fireEvent(new ApplicationStoppedEvent(runApplication, true));
                    IDE.fireEvent(new OutputEvent(NodeJsRuntimeExtension.NODEJS_LOCALIZATION
                                                                        .projectStoppedMessage(currentProject.getName()), Type.INFO));
                    runApplication = null;
                }

                @Override
                protected void onFailure(Throwable exception) {
                    String message =
                            (exception.getMessage() != null) ? exception.getMessage()
                                                             : NodeJsRuntimeExtension.NODEJS_LOCALIZATION.stopApplicationFailed();
                    IDE.fireEvent(new OutputEvent(message, OutputMessage.Type.WARNING));

                    if (exception instanceof ServerException) {
                        ServerException serverException = (ServerException)exception;
                        if (HTTPStatus.INTERNAL_ERROR == serverException.getHTTPStatus()
                            && serverException.getMessage() != null && serverException.getMessage().contains("not found")) {
                            IDE.fireEvent(new ApplicationStoppedEvent(runApplication, false));
                            runApplication = null;
                        }
                    }
                }
            });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }
}