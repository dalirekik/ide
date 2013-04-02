/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.ide.client.framework.application.event;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.ide.client.framework.configuration.IDEConfiguration;

/**
 * Called after success configuration loading (IDEConficuration, UserInfo, ApplicationSettings)
 * <p/>
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $Id: $
 */

public class InitializeServicesEvent extends GwtEvent<InitializeServicesHandler> {

    public static final GwtEvent.Type<InitializeServicesHandler> TYPE = new GwtEvent.Type<InitializeServicesHandler>();

    private IDEConfiguration applicationConfiguration;

    private Loader loader;

    public InitializeServicesEvent(IDEConfiguration applicationConfiguration, Loader loader) {
        this.applicationConfiguration = applicationConfiguration;
        this.loader = loader;
    }

    public IDEConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public Loader getLoader() {
        return loader;
    }

    @Override
    protected void dispatch(InitializeServicesHandler handler) {
        handler.onInitializeServices(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<InitializeServicesHandler> getAssociatedType() {
        return TYPE;
    }

}
