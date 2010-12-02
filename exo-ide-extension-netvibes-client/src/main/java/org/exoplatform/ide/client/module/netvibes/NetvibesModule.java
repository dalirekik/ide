/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ide.client.module.netvibes;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler;
import org.exoplatform.ide.client.framework.configuration.IDEConfiguration;
import org.exoplatform.ide.client.framework.control.NewItemControl;
import org.exoplatform.ide.client.framework.control.event.RegisterControlEvent;
import org.exoplatform.ide.client.framework.module.IDEModule;
import org.exoplatform.ide.client.module.netvibes.controls.DeployUwaWidgetControl;
import org.exoplatform.ide.client.module.netvibes.service.deploy.DeployWidgetServiceImpl;
import org.exoplatform.ide.client.module.netvibes.ui.DeployUwaWidgetPresenter;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $Id: $
 */

public class NetvibesModule implements IDEModule, InitializeServicesHandler
{
   private HandlerManager eventBus;

   /**
    * IDE application configuration.
    */
   private IDEConfiguration configuration;

   /**
    * @param eventBus
    */
   public NetvibesModule(HandlerManager eventBus)
   {
      this.eventBus = eventBus;

      eventBus.fireEvent(new RegisterControlEvent(new NewItemControl("File/New/New Netvibes Widget", "Netvibes Widget",
         "Create Netvibes Widget file", Images.UWA_WIGET, MimeType.UWA_WIDGET)));
      eventBus.fireEvent(new RegisterControlEvent(new DeployUwaWidgetControl(), true, true));

      eventBus.addHandler(InitializeServicesEvent.TYPE, this);
      
      new DeployUwaWidgetPresenter(eventBus);
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler#onInitializeServices(org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent)
    */
   @Override
   public void onInitializeServices(InitializeServicesEvent event)
   {
      configuration = event.getApplicationConfiguration();
      new DeployWidgetServiceImpl(eventBus, configuration.getContext(), event.getLoader());
   }

}
