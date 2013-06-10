/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.ide.ext.appfog.client.apps;

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.ext.appfog.shared.AppfogApplication;
import com.codenvy.ide.json.JsonArray;

/**
 * The view of {@link ApplicationsPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface ApplicationsView extends View<ApplicationsView.ActionDelegate> {
    /** Needs for delegate some function into Applications view. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Close button. */
        void onCloseClicked();

        /** Performs any actions appropriate in response to the user having pressed the Show button. */
        void onShowClicked();

        /**
         * Performs any actions appropriate in response to the user having pressed the Start button.
         *
         * @param app
         *         current application what need to start.
         */
        void onStartClicked(AppfogApplication app);

        /**
         * Performs any actions appropriate in response to the user having pressed the Stop button.
         *
         * @param app
         *         current application what need to stop.
         */
        void onStopClicked(AppfogApplication app);

        /**
         * Performs any actions appropriate in response to the user having pressed the Restart button.
         *
         * @param app
         *         current application what need to restart.
         */
        void onRestartClicked(AppfogApplication app);

        /**
         * Performs any actions appropriate in response to the user having pressed the Delete button.
         *
         * @param app
         *         current application what need to delete.
         */
        void onDeleteClicked(AppfogApplication app);
    }

    /**
     * Sets available application into special place on the view.
     *
     * @param apps
     *         list of available applications.
     */
    void setApplications(JsonArray<AppfogApplication> apps);

    /**
     * Returns target's name.
     *
     * @return
     */
    String getTarget();

    /**
     * Sets new target's name.
     *
     * @param target
     */
    void setTarget(String target);

    /**
     * Returns whether the view is shown.
     *
     * @return <code>true</code> if the view is shown, and
     *         <code>false</code> otherwise
     */
    boolean isShown();

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}