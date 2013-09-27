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
package org.exoplatform.ide.extension.openshift.client.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.HasValue;

import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.ui.client.api.TextFieldItem;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.extension.openshift.client.OpenShiftClientService;
import org.exoplatform.ide.extension.openshift.client.OpenShiftExceptionThrownEvent;
import org.exoplatform.ide.extension.openshift.client.OpenShiftExtension;

/**
 * Presenter for login view. The view must be pointed in Views.gwt.xml.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: May 25, 2011 3:56:55 PM anya $
 */
public class LoginPresenter implements LoginHandler, ViewClosedHandler, SwitchAccountHandler {
    interface Display extends IsView {
        /**
         * Get login button click handler.
         *
         * @return {@link HasClickHandlers} click handler
         */
        HasClickHandlers getLoginButton();

        /**
         * Get cancel button click handler.
         *
         * @return {@link HasClickHandlers} click handler
         */
        HasClickHandlers getCancelButton();

        /**
         * Get email field.
         *
         * @return {@link HasValue}
         */
        HasValue<String> getEmailField();

        /**
         * Get login result label.
         *
         * @return {@link HasValue}
         */
        HasValue<String> getLoginResult();

        /**
         * Get password field.
         *
         * @return {@link TextFieldItem}
         */
        TextFieldItem getPasswordField();

        /**
         * Change the enable state of the login button.
         *
         * @param enabled
         */
        void enableLoginButton(boolean enabled);

        /** Give focus to login field. */
        void focusInEmailField();
    }

    private Display display;

    private LoggedInHandler loggedInHandler;

    private LoginCanceledHandler loginCanceledHandler;

    public LoginPresenter() {
        IDE.addHandler(LoginEvent.TYPE, this);
        IDE.addHandler(ViewClosedEvent.TYPE, this);
        IDE.addHandler(SwitchAccountEvent.TYPE, this);
    }

    /**
     * Bind display with presenter.
     *
     * @param d
     */
    public void bindDisplay(Display d) {
        this.display = d;

        display.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                IDE.getInstance().closeView(display.asView().getId());
                if (loginCanceledHandler != null) {
                    loginCanceledHandler.onLoginCanceled(new LoginCanceledEvent());
                    loginCanceledHandler = null;
                }
            }
        });

        display.getLoginButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doLogin();
            }
        });

        display.getEmailField().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                display.enableLoginButton(isFieldsFullFilled());
            }
        });

        display.getPasswordField().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                display.enableLoginButton(isFieldsFullFilled());
            }
        });

        display.getPasswordField().addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == 13 && isFieldsFullFilled()) {
                    doLogin();
                }
            }
        });

    }

    /**
     * Check whether necessary fields are fullfilled.
     *
     * @return if <code>true</code> all necessary fields are fullfilled
     */
    private boolean isFieldsFullFilled() {
        return (display.getEmailField().getValue() != null && !display.getEmailField().getValue().isEmpty()
                && display.getPasswordField().getValue() != null && !display.getPasswordField().getValue().isEmpty());
    }

    /** @see org.exoplatform.ide.extension.openshift.client.login.LoginHandler#onLogin(org.exoplatform.ide.extension.openshift.client
     * .login.LoginEvent) */
    @Override
    public void onLogin(LoginEvent event) {
        if (display == null) {
            Display display = GWT.create(Display.class);
            bindDisplay(display);
            IDE.getInstance().openView(display.asView());
            display.enableLoginButton(false);
            display.focusInEmailField();
        }

        loggedInHandler = event.loggedInHandler();
        loginCanceledHandler = event.loginCanceledHandler();
    }

    /** Perform log in OpenShift. */
    protected void doLogin() {
        String email = display.getEmailField().getValue();
        String password = display.getPasswordField().getValue();

        try {
            OpenShiftClientService.getInstance().login(email, password, new AsyncRequestCallback<String>() {
                /**
                 * @see org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback#onSuccess(java.lang.Object)
                 */
                @Override
                protected void onSuccess(String result) {
                    IDE.getInstance().closeView(display.asView().getId());
                    IDE.fireEvent(new OutputEvent(OpenShiftExtension.LOCALIZATION_CONSTANT.loginSuccess(), Type.INFO));

                    if (loggedInHandler != null) {
                        loggedInHandler.onLoggedIn(new LoggedInEvent());
                        loggedInHandler = null;
                    }

                    IDE.fireEvent(new LoggedInEvent(false));
                }

                /**
                 * @see org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback#onFailure(java.lang.Throwable)
                 */
                @Override
                protected void onFailure(Throwable exception) {
                    IDE.fireEvent(new LoggedInEvent(true));
                    IDE.fireEvent(new OpenShiftExceptionThrownEvent(exception, OpenShiftExtension.LOCALIZATION_CONSTANT
                                                                                                 .loginFailed()));
                }
            });
        } catch (RequestException e) {
            IDE.fireEvent(new LoggedInEvent(true));
            IDE.fireEvent(new OpenShiftExceptionThrownEvent(e, OpenShiftExtension.LOCALIZATION_CONSTANT.loginFailed()));
        }
    }

    /** @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api
     * .event.ViewClosedEvent) */
    @Override
    public void onViewClosed(ViewClosedEvent event) {
        if (event.getView() instanceof Display) {
            display = null;
        }
    }

    /** @see org.exoplatform.ide.extension.openshift.client.login.SwitchAccountHandler#onSwitchAccount(org.exoplatform.ide.extension
     * .openshift.client.login.SwitchAccountEvent) */
    @Override
    public void onSwitchAccount(org.exoplatform.ide.extension.openshift.client.login.SwitchAccountEvent event) {
        if (display == null) {
            openLoginForm();
        }
    }

    /** Open form to login to OpenShift */
    private void openLoginForm() {
        Display display = GWT.create(Display.class);
        bindDisplay(display);
        IDE.getInstance().openView(display.asView());
        display.enableLoginButton(false);
        display.focusInEmailField();
        display.getLoginResult().setValue("");
    }
}
