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
package org.exoplatform.ide.client.module.groovy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $Id: $
 */

public interface GroovyPluginImageBundle extends ClientBundle
{

   public static final GroovyPluginImageBundle INSTANCE = GWT.create(GroovyPluginImageBundle.class);

   /**
    * To active bundle, call 
    * <code>GroovyPluginImageBundle.INSTANCE.css().ensureInjected()</code>
    * method in your module.
    * 
    * @return {@link GroovyCss}
    */
   @Source("groovy.css")
   public GroovyCss css();
   
   @Source("images/codeassistant/class.gif")
   ImageResource classItem();

   @Source("images/codeassistant/annotation.gif")
   ImageResource annotationItem();

   @Source("images/codeassistant/innerinterface_public.gif")
   ImageResource intrfaceItem();
   
   @Source("images/codeassistant/enum.gif")
   ImageResource enumItem();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/class-item.png")
   ImageResource classItem1();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/default-field.png")
   ImageResource defaultField();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/default-method.png")
   ImageResource defaultMethod();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/private-field.png")
   ImageResource privateField();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/private-method.png")
   ImageResource privateMethod();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/protected-field.png")
   ImageResource protectedField();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/protected-method.png")
   ImageResource protectedMethod();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/public-field.png")
   ImageResource publicField();

   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/public-method.png")
   ImageResource publicMethod();
   
   @Source("org/exoplatform/ide/client/module/groovy/images/codeassistant/local.png")
   ImageResource variable();

   @Source("org/exoplatform/ide/client/module/groovy/images/blank.png")
   ImageResource blankImage();

   //   @Source("../public/images/module/groovy/bundled/set_autoload.png")
//   ImageResource setAutoLoad();
//
//   @Source("../public/images/module/groovy/bundled/set_autoload_Disabled.png")
//   ImageResource setAutoLoadDisabled();
//
//   @Source("../public/images/module/groovy/bundled/unset_autoload.png")
//   ImageResource unsetAutoLoad();
//
//   @Source("../public/images/module/groovy/bundled/unset_autoload_Disabled.png")
//   ImageResource unsetAutoLoadDisabled();
//
//   @Source("../public/images/module/groovy/bundled/validate.png")
//   ImageResource validateGroovy();
//
//   @Source("../public/images/module/groovy/bundled/validate_Disabled.png")
//   ImageResource validateGroovyDisabled();
//
//   @Source("../public/images/module/groovy/bundled/deploy.png")
//   ImageResource deployGroovy();
//
//   @Source("../public/images/module/groovy/bundled/deploy_Disabled.png")
//   ImageResource deployGroovyDisabled();
//
//   @Source("../public/images/module/groovy/bundled/undeploy.png")
//   ImageResource undeployGroovy();
//
//   @Source("../public/images/module/groovy/bundled/undeploy_Disabled.png")
//   ImageResource undeployGroovyDisabled();
//
//   @Source("../public/images/module/groovy/bundled/output.png")
//   ImageResource groovyOutput();
//
//   @Source("../public/images/module/groovy/bundled/output_Disabled.png")
//   ImageResource groovyOutputDisabled();
   
}
