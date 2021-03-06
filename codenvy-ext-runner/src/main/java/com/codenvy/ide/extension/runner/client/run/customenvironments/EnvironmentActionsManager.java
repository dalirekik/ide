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
package com.codenvy.ide.extension.runner.client.run.customenvironments;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.keybinding.KeyBindingAgent;
import com.codenvy.ide.api.keybinding.KeyBuilder;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.extension.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.extension.runner.client.RunnerResources;
import com.codenvy.ide.extension.runner.client.actions.EnvironmentAction;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.util.input.CharCodeWithModifiers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import java.util.HashMap;
import java.util.Map;

import static com.codenvy.ide.extension.runner.client.RunnerExtension.GROUP_RUN_WITH;

/**
 * Listens for opening/closing a project and adds/removes
 * a corresponding action for executing every custom Docker-script.
 *
 * @author Artem Zatsarynnyy
 */
public class EnvironmentActionsManager implements ProjectActionHandler {

    private final String                                        envFolderPath;
    private final Map<EnvironmentAction, CharCodeWithModifiers> actions2HotKeys;
    private final EnvironmentActionFactory                      environmentActionFactory;
    private final RunnerLocalizationConstant                    constants;
    private final ActionManager                                 actionManager;
    private final KeyBindingAgent                               keyBindingAgent;
    private final ProjectServiceClient                          projectServiceClient;
    private final DtoUnmarshallerFactory                        dtoUnmarshallerFactory;

    @Inject
    public EnvironmentActionsManager(@Named("envFolderPath") String envFolderPath,
                                     EnvironmentActionFactory environmentActionFactory,
                                     RunnerLocalizationConstant constants,
                                     ActionManager actionManager,
                                     KeyBindingAgent keyBindingAgent,
                                     RunnerResources resources,
                                     EventBus eventBus,
                                     ProjectServiceClient projectServiceClient,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.envFolderPath = envFolderPath;
        this.environmentActionFactory = environmentActionFactory;
        this.constants = constants;
        this.actionManager = actionManager;
        this.keyBindingAgent = keyBindingAgent;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        actions2HotKeys = new HashMap<>();
        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectOpened(ProjectActionEvent event) {
        requestCustomEnvironmentsForProject(event.getProject(), new AsyncCallback<Array<CustomEnvironment>>() {
            @Override
            public void onSuccess(Array<CustomEnvironment> result) {
                for (CustomEnvironment env : result.asIterable()) {
                    addActionForEnvironment(env);
                }
            }

            @Override
            public void onFailure(Throwable ignore) {
                // no custom environments
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectClosed(ProjectActionEvent event) {
        removeAllActions();
    }

    /**
     * Get list of custom environments for the specified project.
     *
     * @param project
     *         project for which need to get list of environments
     * @param callback
     *         callback to return custom environments
     */
    public void requestCustomEnvironmentsForProject(ProjectDescriptor project, final AsyncCallback<Array<CustomEnvironment>> callback) {
        final Unmarshallable<Array<ItemReference>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ItemReference.class);
        projectServiceClient.getChildren(project.getPath() + '/' + envFolderPath,
                                         new AsyncRequestCallback<Array<ItemReference>>(unmarshaller) {
                                             @Override
                                             protected void onSuccess(Array<ItemReference> result) {
                                                 final Array<CustomEnvironment> environments = Collections.createArray();
                                                 for (ItemReference item : result.asIterable()) {
                                                     environments.add(new CustomEnvironment(item.getName()));
                                                 }
                                                 callback.onSuccess(environments);
                                             }

                                             @Override
                                             protected void onFailure(Throwable caught) {
                                                 callback.onFailure(caught);
                                             }
                                         });
    }

    /**
     * Add action to run the specified custom environment.
     *
     * @param env
     *         name of the custom environment for which need to create action
     */
    public void addActionForEnvironment(CustomEnvironment env) {
        final int actionNum = actions2HotKeys.size() + 1;
        final EnvironmentAction action = environmentActionFactory.createAction(constants.environmentActionText(env.getName()),
                                                                               constants.environmentActionDescription(env.getName()),
                                                                               new CustomEnvironment(env.getName()));
        final String actionId = constants.environmentActionId(env.getName());
        actionManager.registerAction(actionId, action);
        ((DefaultActionGroup)actionManager.getAction(GROUP_RUN_WITH)).add(action);

        CharCodeWithModifiers hotKey = null;
        // Bind hot-key only for the first 10 actions (Ctrl+Alt+0...9)
        if (actionNum <= 10) {
            hotKey = new KeyBuilder().action().alt().charCode(actionNum + 47).build();
            keyBindingAgent.getGlobal().addKey(hotKey, actionId);
        }
        actions2HotKeys.put(action, hotKey);
    }

    /**
     * Remove action which corresponds to the specified environment.
     *
     * @param env
     *         environment for which need to remove action
     */
    public void removeActionForEnvironment(CustomEnvironment env) {
        for (EnvironmentAction action : actions2HotKeys.keySet()) {
            if (env.equals(action.getEnvironment())) {
                removeAction(action);
                break;
            }
        }
    }

    private void removeAction(EnvironmentAction action) {
        DefaultActionGroup customImagesGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RUN_WITH);
        customImagesGroup.remove(action);

        final String actionId = actionManager.getId(action);
        actionManager.unregisterAction(actionId);

        // unbind hot-key if action has it
        final CharCodeWithModifiers hotKey = actions2HotKeys.get(action);
        if (hotKey != null) {
            keyBindingAgent.getGlobal().removeKey(hotKey, actionId);
        }

        actions2HotKeys.remove(action);
    }

    private void removeAllActions() {
        for (Map.Entry<EnvironmentAction, CharCodeWithModifiers> entry : actions2HotKeys.entrySet()) {
            removeAction(entry.getKey());
        }
    }
}
