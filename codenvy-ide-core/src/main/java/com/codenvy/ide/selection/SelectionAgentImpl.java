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
package com.codenvy.ide.selection;

import com.codenvy.ide.api.event.ActivePartChangedEvent;
import com.codenvy.ide.api.event.ActivePartChangedHandler;
import com.codenvy.ide.api.event.SelectionChangedEvent;
import com.codenvy.ide.api.parts.PartPresenter;
import com.codenvy.ide.api.parts.PropertyListener;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Implements {@link SelectionAgent}
 *
 * @author Nikolay Zamosenchuk
 */
public class SelectionAgentImpl implements ActivePartChangedHandler, PropertyListener, SelectionAgent {

    private       PartPresenter activePart;
    private final EventBus      eventBus;

    @Inject
    public SelectionAgentImpl(EventBus eventBus) {
        this.eventBus = eventBus;
        // bind event listener
        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    public Selection<?> getSelection() {
        return activePart != null ? activePart.getSelection() : null;
    }

    protected void notifySelectionChanged() {
        eventBus.fireEvent(new SelectionChangedEvent(getSelection()));
    }

    /** {@inheritDoc} */
    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        // remove listener from previous active part
        if (activePart != null) {
            activePart.removePropertyListener(this);
        }
        // set new active part
        activePart = event.getActivePart();
        if (activePart != null) {
            activePart.addPropertyListener(this);
        }
        notifySelectionChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChanged(PartPresenter source, int propId) {
        // Check property and ensure came from active part
        if (propId == PartPresenter.SELECTION_PROPERTY && source == activePart) {
            notifySelectionChanged();
        }
    }

}
