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
package com.codenvy.ide.wizard.project;

import com.codenvy.ide.api.mvp.Presenter;
import com.codenvy.ide.ui.window.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
public class ProjectWizardViewImpl extends Window implements ProjectWizardView {
    private static final String                        defaultRAMRequired  = "256MB";
    private static final String                        defaultRAMAvailable = "256MB";
    private static       ProjectWizardViewImplUiBinder ourUiBinder         = GWT.create(ProjectWizardViewImplUiBinder.class);


    @UiField
    Style       style;
    @UiField
    SimplePanel wizardPanel;
    @UiField
    Label       builderEnvConfText;
    @UiField
    Label       builderEnvConf;
    @UiField
    Label       runnerEnvConfText;
    @UiField
    Label       runnerEnvConf;
    @UiField
    FlowPanel   infoRAMPanel;
    @UiField
    Label       RAMRequired;
    @UiField
    Label       RAMAvailable;
    @UiField
    Button      nextStepButton;
    @UiField
    Button      previousStepButton;
    @UiField
    Button      saveButton;
    private ActionDelegate delegate;
    private Map<Presenter, Widget> pageCache = new HashMap<>();
    private String saveButtonText;

    private HandlerRegistration nativePreviewHandlerRegistration = null;

    @Inject
    public ProjectWizardViewImpl(com.codenvy.ide.Resources resources) {
        super(false);
        setTitle("Project Configuration");
        setWidget(ourUiBinder.createAndBindUi(this));
        nextStepButton.sinkEvents(Event.ONCLICK);
        previousStepButton.sinkEvents(Event.ONCLICK);
        nextStepButton.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!nextStepButton.getStyleName().contains(style.disabled()))
                    delegate.onNextClicked();
            }
        }, ClickEvent.getType());
        previousStepButton.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!previousStepButton.getStyleName().contains(style.disabled()))
                    delegate.onBackClicked();
            }
        }, ClickEvent.getType());
        saveButtonText = saveButton.getText();
        saveButton.addStyleName(resources.Css().buttonLoader());
        builderEnvConfText.setVisible(false);
        builderEnvConf.setVisible(false);
        runnerEnvConfText.setVisible(false);
        runnerEnvConf.setVisible(false);
    }

    @UiHandler("saveButton")
    void saveClick(ClickEvent event) {
        delegate.onSaveClicked();
    }

    @Override
    public void setLoaderVisibled(boolean enabled) {
        if (enabled) {
            saveButton.setHTML("<i></i>");
            saveButton.setEnabled(false);
        } else {
            saveButton.setText(saveButtonText);
            saveButton.setEnabled(true);
        }
    }

    @Override
    public void setRAMRequired(String amountOfRAM) {
        if (amountOfRAM == null) amountOfRAM = defaultRAMRequired;
        RAMRequired.setText(amountOfRAM);
    }

    @Override
    public void setRAMAvailable(String amountOfRAM) {
        if (amountOfRAM == null) amountOfRAM = defaultRAMAvailable;
        RAMAvailable.setText(amountOfRAM);
    }

    @Override
    public void setBuilderEnvirConfig(String configs[]) {
        if (configs == null) {
            if (builderEnvConfText.isVisible()) builderEnvConfText.setVisible(false);
            if (builderEnvConf.isVisible()) builderEnvConf.setVisible(false);
        } else {
            if (!builderEnvConfText.isVisible()) builderEnvConfText.setVisible(true);
            if (!builderEnvConf.isVisible()) builderEnvConf.setVisible(true);
            StringBuilder configsBuilder = new StringBuilder();
            for (String config : configs) {
                if (config.length() > 0) {
                    configsBuilder.append(": " + config);
                }
            }
            builderEnvConf.setText(configsBuilder.toString());
        }
    }

    @Override
    public void setRunnerEnvirConfig(String configs[]) {
        if (configs == null) {
            if (runnerEnvConfText.isVisible()) runnerEnvConfText.setVisible(false);
            if (runnerEnvConf.isVisible()) runnerEnvConf.setVisible(false);
        } else {
            if (!runnerEnvConfText.isVisible()) runnerEnvConfText.setVisible(true);
            if (!runnerEnvConf.isVisible()) runnerEnvConf.setVisible(true);
            StringBuilder configsBuilder = new StringBuilder();
            for (String config : configs) {
                if (config.length() > 0) {
                    configsBuilder.append(": " + config);
                }
            }
            runnerEnvConf.setText(configsBuilder.toString());
        }
    }

    @Override
    public void setInfoVisibled(boolean enabled) {
        if (enabled) {
            infoRAMPanel.getElement().replaceClassName(style.hidden(), style.visible());
        } else {
            infoRAMPanel.getElement().replaceClassName(style.visible(), style.hidden());
        }
    }

    @Override
    public void showPage(Presenter presenter) {
        wizardPanel.clear();
        if (pageCache.containsKey(presenter)) {
            wizardPanel.add(pageCache.get(presenter));
        } else {
            presenter.go(wizardPanel);
            pageCache.put(presenter, wizardPanel.getWidget());
        }
    }

    @Override
    public void showDialog() {
        show();

        if (nativePreviewHandlerRegistration == null) {
            nativePreviewHandlerRegistration = Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
                @Override
                public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                    if (event.getTypeInt() == Event.ONKEYUP &&
                        event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                        if (nextStepButton.isEnabled()) {
                            delegate.onNextClicked();
                        } else if (saveButton.isEnabled()) {
                            delegate.onSaveClicked();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void setEnabledAnimation(boolean enabled) {

    }

    @Override
    public void close() {
        if (nativePreviewHandlerRegistration != null) {
            nativePreviewHandlerRegistration.removeHandler();
            nativePreviewHandlerRegistration = null;
        }

        hide();
        pageCache.clear();
    }

    @Override
    public void setNextButtonEnabled(boolean enabled) {
        nextStepButton.setEnabled(enabled);
    }

    @Override
    public void setFinishButtonEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    @Override
    public void setBackButtonEnabled(boolean enabled) {
        previousStepButton.setEnabled(enabled);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return null;
    }

    @Override
    protected void onClose() {
        pageCache.clear();
        delegate.onCancelClicked();
    }

    interface Style extends CssResource {

        String namePanel();

        String projectNamePosition();

        String project();

        String bottomPanel();

        String privacy();

        String tab();

        String namePanelRight();

        String rootPanel();

        String topPanel();

        String centerPanel();

        String blueButton();

        String button();

        String disabled();

        String infoText();

        String infoValue();

        String labelPanel();

        String visible();

        String hidden();

        String grayColor();
    }

    interface ProjectWizardViewImplUiBinder
            extends UiBinder<FlowPanel, ProjectWizardViewImpl> {
    }
}