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
package com.codenvy.vfs.watcher.client;

import com.google.gwt.event.shared.HandlerRegistration;

import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.userinfo.UserInfo;
import org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedEvent;
import org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedHandler;
import org.exoplatform.ide.client.framework.websocket.MessageBus.ReadyState;
import org.exoplatform.ide.client.framework.websocket.events.MainSocketOpenedEvent;
import org.exoplatform.ide.client.framework.websocket.events.MainSocketOpenedHandler;
import org.exoplatform.ide.client.framework.websocket.events.MessageHandler;
import org.exoplatform.ide.communication.MessageFilter;
import org.exoplatform.ide.dtogen.client.RoutableDtoClientImpl;
import org.exoplatform.ide.dtogen.shared.ServerToClientDto;
import org.exoplatform.ide.json.client.Jso;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class VfsWatcherExtension extends Extension implements MainSocketOpenedHandler, UserInfoReceivedHandler
{

   private UserInfo userInfo;

   private HandlerRegistration handlerRegistration;

   private MessageFilter messageFilter = new MessageFilter();

   private VfsApi vfsApi;

   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize()
   {
      IDE.eventBus().addHandler(UserInfoReceivedEvent.TYPE, this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onMainSocketOpened(MainSocketOpenedEvent event)
   {
      handlerRegistration.removeHandler();
      subscribe();
   }

   @Override
   public void onUserInfoReceived(UserInfoReceivedEvent event)
   {
      userInfo = event.getUserInfo();
      if(IDE.messageBus().getReadyState() != ReadyState.OPEN)
      {
         handlerRegistration = IDE.eventBus().addHandler(MainSocketOpenedEvent.TYPE, this);
         return;
      }

      subscribe();
   }

   private void subscribe()
   {
      vfsApi = new VfsApi(IDE.messageBus());
      new VfsWatcher(messageFilter, IDE.eventBus(), vfsApi);
      IDE.messageBus().subscribe("vfs_watcher." + userInfo.getClientId(),new MessageHandler()
      {
         @Override
         public void onMessage(String message)
         {
            ServerToClientDto dto = (ServerToClientDto)Jso.deserialize(message).<RoutableDtoClientImpl>cast();
            messageFilter.dispatchMessage(dto);
         }
      });
   }
}
