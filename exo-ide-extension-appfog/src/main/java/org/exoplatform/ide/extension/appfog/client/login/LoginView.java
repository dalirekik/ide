/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.extension.appfog.client.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.gwtframework.ui.client.api.TextFieldItem;
import org.exoplatform.gwtframework.ui.client.component.ImageButton;
import org.exoplatform.gwtframework.ui.client.component.Label;
import org.exoplatform.gwtframework.ui.client.component.PasswordTextInput;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.ui.impl.ViewType;
import org.exoplatform.ide.extension.appfog.client.AppfogExtension;

/**
 * View for login to Appfog.
 *
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public class LoginView extends ViewImpl implements LoginPresenter.Display {
    private static final String ID = "ideLoginView";

    private static final int WIDTH = 400;

    private static final int HEIGHT = 180;

    private static final String LOGIN_BUTTON_ID = "ideLoginViewLoginButton";

    private static final String CANCEL_BUTTON_ID = "ideLoginViewCancelButton";

    private static final String EMAIL_FIELD_ID = "ideLoginViewEmailField";

    private static final String PASSWORD_FIELD_ID = "ideLoginViewPasswordField";

    private static final String TARGET_FIELD_ID = "ideLoginViewTargetField";

    /** UI binder for this view. */
    private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);

    interface LoginViewUiBinder extends UiBinder<Widget, LoginView> {
    }

    /** Field to select target (domain, server), where to login. */
    @UiField
    TextBox targetField;

    /** Email field. */
    @UiField
    TextBox emailField;

    /** Password field. */
    @UiField
    PasswordTextInput passwordField;

    /** Login button. */
    @UiField
    ImageButton loginButton;

    /** Cancel button. */
    @UiField
    ImageButton cancelButton;

    @UiField
    Label errLabel;

    public LoginView() {
        super(ID, ViewType.MODAL, AppfogExtension.LOCALIZATION_CONSTANT.loginViewTitle(), null, WIDTH, HEIGHT,
              false);
        add(uiBinder.createAndBindUi(this));

        targetField.setName(TARGET_FIELD_ID);
        emailField.setName(EMAIL_FIELD_ID);
        passwordField.setName(PASSWORD_FIELD_ID);
        loginButton.setButtonId(LOGIN_BUTTON_ID);
        cancelButton.setButtonId(CANCEL_BUTTON_ID);
        targetField.setReadOnly(true);
    }

    @Override
    public HasClickHandlers getLoginButton() {
        return loginButton;
    }

    @Override
    public HasClickHandlers getCancelButton() {
        return cancelButton;
    }

    @Override
    public HasValue<String> getEmailField() {
        return emailField;
    }

    @Override
    public TextFieldItem getPasswordField() {
        return passwordField;
    }

    @Override
    public void enableLoginButton(boolean enabled) {
        loginButton.setEnabled(enabled);
    }

    @Override
    public void focusInEmailField() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                emailField.setFocus(true);
            }
        });
    }

    @Override
    public HasValue<String> getTargetSelectField() {
        return targetField;
    }

    @Override
    public void setTargetValues(String target) {
        targetField.setValue(target);
    }

    @Override
    public HasValue<String> getErrorLabelField() {
        return errLabel;
    }
}