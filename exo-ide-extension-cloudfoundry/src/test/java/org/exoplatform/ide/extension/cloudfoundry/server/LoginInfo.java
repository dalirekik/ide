/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.ide.extension.cloudfoundry.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
class LoginInfo
{
   static String email;
   static String password;
   static String target = "http://api.cloudfoundry.com";

   static
   {
      Properties properties = new Properties();
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("security.properties");
      if (in != null)
      {
         try
         {
            properties.load(in);
            email = (String)properties.get("email");
            password = (String)properties.get("password");
            if (properties.containsKey("target"))
            {
               target = (String)properties.get("target");
            }
         }
         catch (IOException ignored)
         {
            // Lets resolve this in tests.
         }
         finally
         {
            try
            {
               in.close();
            }
            catch (IOException ignored2)
            {
            }
         }
      }
   }
}