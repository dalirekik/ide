/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
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
package com.codenvy.ide.ext.github.client.welcome;

import com.codenvy.ide.api.parts.WelcomeItemAction;
import com.codenvy.ide.api.user.User;
import com.codenvy.ide.api.user.UserClientService;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.ext.github.client.GitHubLocalizationConstant;
import com.codenvy.ide.ext.github.client.GitHubResources;
import com.codenvy.ide.ext.github.client.load.ImportPresenter;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.resources.client.ImageResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The action what provides some actions when import project from github item is clicked.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class ImportProjectAction implements WelcomeItemAction {
    private GitHubLocalizationConstant constant;
    private GitHubResources            resources;
    private ImportPresenter            importPresenter;
    private UserClientService          service;
    private DtoFactory                 dtoFactory;

    /**
     * Create action.
     *
     * @param constant
     * @param resources
     */
    @Inject
    public ImportProjectAction(GitHubLocalizationConstant constant,
                               GitHubResources resources,
                               ImportPresenter importPresenter,
                               UserClientService service,
                               DtoFactory dtoFactory) {
        this.constant = constant;
        this.resources = resources;
        this.importPresenter = importPresenter;
        this.service = service;
        this.dtoFactory = dtoFactory;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return constant.welcomeImportTitle();
    }

    /** {@inheritDoc} */
    @Override
    public String getCaption() {
        return constant.welcomeImportText();
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getIcon() {
        return resources.importFromGithub();
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        StringUnmarshaller unmarshaller = new StringUnmarshaller();
        try {
            service.getUser(new AsyncRequestCallback<String>(unmarshaller) {
                @Override
                protected void onSuccess(String result) {
                    User user = dtoFactory.createDtoFromJson(result, User.class);
                    importPresenter.showDialog(user);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    Log.error(ImportProjectAction.class, "Can't get user", exception);
                }
            });
        } catch (RequestException e) {
            Log.error(ImportProjectAction.class, "Can't get user", e);
        }
    }
}