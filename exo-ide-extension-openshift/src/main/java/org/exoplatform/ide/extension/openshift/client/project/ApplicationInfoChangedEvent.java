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
package org.exoplatform.ide.extension.openshift.client.project;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public class ApplicationInfoChangedEvent extends GwtEvent<ApplicationInfoChangedHandler> {
    /** Type used to register event. */
    public static final GwtEvent.Type<ApplicationInfoChangedHandler> TYPE =
            new GwtEvent.Type<ApplicationInfoChangedHandler>();


    /** Application name. */
    private String appName;

    /**
     * @param appName
     *         application name
     */
    public ApplicationInfoChangedEvent(String appName) {
        this.appName = appName;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#getAssociatedType() */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ApplicationInfoChangedHandler> getAssociatedType() {
        return TYPE;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler) */
    @Override
    protected void dispatch(ApplicationInfoChangedHandler handler) {
        handler.onApplicationInfoChanged(this);
    }

    /** @return the appName project's id */
    public String getAppName() {
        return appName;
    }
}
