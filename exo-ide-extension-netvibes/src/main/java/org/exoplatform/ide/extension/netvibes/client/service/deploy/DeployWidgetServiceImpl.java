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
package org.exoplatform.ide.extension.netvibes.client.service.deploy;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.extension.netvibes.client.model.Categories;
import org.exoplatform.ide.extension.netvibes.client.model.DeployResult;
import org.exoplatform.ide.extension.netvibes.client.model.DeployWidget;
import org.exoplatform.ide.extension.netvibes.client.service.deploy.marshaller.DeployWidgetMarshaller;

/**
 * Implementation of {@link DeployWidgetService}.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Nov 30, 2010 $
 * 
 */
public class DeployWidgetServiceImpl extends DeployWidgetService
{
   private final String LOGIN = "login";

   private final String PASSWORD = "password";

   private final String APIKEY = "apikey";

   private final String SECRET_KEY = "secretkey";

   /**
    * URL for retrieving available categories.
    */
   public static final String CATEGORIES_URL = "http://api.eco.netvibes.com/categories";

   /**
    * Path to Netvibes REST service.
    */
   public static final String SERVICE_PATH = "/ide/netvibes";

   /**
    * Path to deploy method of Netvibes REST service.
    */
   public static final String DEPLOY = "/deploy";

   /**
    * Loader to display.
    */
   private Loader loader;

   /**
    * REST context.
    */
   private String restContext;

   /**
    * Constructor.
    * 
    * @param eventBus event bus
    * @param restContext REST context
    * @param loader loader to display
    */
   public DeployWidgetServiceImpl(String restContext, Loader loader)
   {
      this.loader = loader;
      this.restContext = restContext;
   }

   /**
    * @throws RequestException 
    * @see org.exoplatform.ide.client.module.netvibes.service.deploy.DeployWidgetService#getCategories(org.exoplatform.ide.client.module.netvibes.service.deploy.callback.WidgetCategoryCallback)
    */
   @Override
   public void getCategories(AsyncRequestCallback<Categories> callback) throws RequestException
   {
      AsyncRequest.build(RequestBuilder.GET, CATEGORIES_URL).loader(loader).send(callback);
   }

   /**
    * @see org.exoplatform.ide.client.module.netvibes.service.deploy.DeployWidgetService#deploy(org.exoplatform.ide.client.module.netvibes.model.DeployWidget,
    *      java.lang.String, java.lang.String,
    *      org.exoplatform.ide.client.module.netvibes.service.deploy.callback.WidgetDeployCallback)
    */
   @Override
   public void deploy(DeployWidget deployWidget, String login, String password, AsyncRequestCallback<DeployResult> callback) throws RequestException
   {
      String url = restContext + SERVICE_PATH + DEPLOY;
      String params = PASSWORD + "=" + password + "&";
      params += LOGIN + "=" + login + "&";
      params += APIKEY + "=" + deployWidget.getApiKey() + "&";
      params += SECRET_KEY + "=" + deployWidget.getSecretKey();

      DeployWidgetMarshaller marshaller = new DeployWidgetMarshaller(deployWidget);
      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader).header(LOGIN, login)
         .header(PASSWORD, password).data(marshaller.marshal()).send(callback);
   }

}