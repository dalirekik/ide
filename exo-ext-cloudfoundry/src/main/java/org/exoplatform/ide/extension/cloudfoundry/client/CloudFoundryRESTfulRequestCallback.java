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
package org.exoplatform.ide.extension.cloudfoundry.client;

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;

import org.exoplatform.ide.commons.exception.ExceptionThrownEvent;
import org.exoplatform.ide.commons.exception.ServerException;
import org.exoplatform.ide.extension.cloudfoundry.client.login.LoggedInHandler;
import org.exoplatform.ide.extension.cloudfoundry.client.login.LoginCanceledHandler;
import org.exoplatform.ide.extension.cloudfoundry.client.login.LoginEvent;
import org.exoplatform.ide.rest.HTTPStatus;
import org.exoplatform.ide.websocket.rest.RequestCallback;
import org.exoplatform.ide.websocket.rest.Unmarshallable;

/**
 * WebSocket CloudFoundry request. The {@link #onFailure(Throwable)} method contains the check for user not authorized
 * exception, in this case - the {@link LoginEvent} is fired.
 * 
 * @author <a href="mailto:azatsarynnyy@exoplatfrom.com">Artem Zatsarynnyy</a>
 * @version $Id: CloudFoundryRESTfulRequestCallback.java Nov 30, 2012 9:58:20 AM azatsarynnyy $
 *
 * @param <T>
 * 
 * @see CloudFoundryAsyncRequestCallback
 */
public abstract class CloudFoundryRESTfulRequestCallback<T> extends RequestCallback<T>
{
   private LoggedInHandler loggedIn;

   private LoginCanceledHandler loginCanceled;

   private String loginUrl;

   private final static String CLOUDFOUNDRY_EXIT_CODE = "Cloudfoundry-Exit-Code";

   private EventBus eventBus;

   public CloudFoundryRESTfulRequestCallback(Unmarshallable<T> unmarshaller, LoggedInHandler loggedIn,
      LoginCanceledHandler loginCanceled, EventBus eventBus)
   {
      this(unmarshaller, loggedIn, loginCanceled, null, eventBus);
   }

   public CloudFoundryRESTfulRequestCallback(Unmarshallable<T> unmarshaller, LoggedInHandler loggedIn,
      LoginCanceledHandler loginCanceled, String loginUrl, EventBus eventBus)
   {
      super(unmarshaller);
      this.loggedIn = loggedIn;
      this.loginCanceled = loginCanceled;
      this.loginUrl = loginUrl;
      this.eventBus = eventBus;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void onFailure(Throwable exception)
   {
      if (exception instanceof ServerException)
      {
         ServerException serverException = (ServerException)exception;
         if (HTTPStatus.OK == serverException.getHTTPStatus() && serverException.getMessage() != null
            && serverException.getMessage().contains("Authentication required."))
         {
            eventBus.fireEvent(new LoginEvent(loggedIn, loginCanceled, loginUrl));
            return;
         }
         else if (HTTPStatus.FORBIDDEN == serverException.getHTTPStatus()
            && serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE) != null
            && "200".equals(serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE)))
         {
            eventBus.fireEvent(new LoginEvent(loggedIn, loginCanceled, loginUrl));
            return;
         }
         else if (HTTPStatus.NOT_FOUND == serverException.getHTTPStatus()
            && serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE) != null
            && "301".equals(serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE)))
         {
            // TODO
            //            Dialogs.getInstance().showError(CloudFoundryExtension.LOCALIZATION_CONSTANT.applicationNotFound());
            Window.alert(CloudFoundryExtension.LOCALIZATION_CONSTANT.applicationNotFound());
            return;
         }
         else
         {
            String msg = "";
            if (serverException.isErrorMessageProvided())
            {
               msg = serverException.getLocalizedMessage();
            }
            else
            {
               msg = "Status:&nbsp;" + serverException.getHTTPStatus();// + "&nbsp;" + serverException.getStatusText();
            }
            // TODO
            //            Dialogs.getInstance().showError(msg);
            Window.alert(msg);
            return;
         }
      }
      eventBus.fireEvent(new ExceptionThrownEvent(exception));
   }
}