/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.extension.cloudbees.client.info;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.extension.cloudbees.shared.ApplicationInfo;

/**
 * Event, which set to control to show application info.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: ApplicationInfoEvent.java Jun 30, 2011 4:57:08 PM vereshchaka $
 */
public class ApplicationInfoEvent extends GwtEvent<ApplicationInfoHandler> {

    private ApplicationInfo appInfo;

    /**
     *
     */
    public ApplicationInfoEvent() {
    }

    /** @param appInfo */
    public ApplicationInfoEvent(ApplicationInfo appInfo) {
        super();
        this.appInfo = appInfo;
    }

    /** Type used to register this event. */
    public static final GwtEvent.Type<ApplicationInfoHandler> TYPE = new GwtEvent.Type<ApplicationInfoHandler>();

    /** @see com.google.gwt.event.shared.GwtEvent#getAssociatedType() */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ApplicationInfoHandler> getAssociatedType() {
        return TYPE;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler) */
    @Override
    protected void dispatch(ApplicationInfoHandler handler) {
        handler.onShowApplicationInfo(this);
    }

    /** @return the appInfo */
    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

}