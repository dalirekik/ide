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
package org.exoplatform.ide.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.thoughtworks.selenium.Selenium;

import org.exoplatform.ide.IDE;
import org.exoplatform.ide.TestConstants;
import org.openqa.selenium.WebDriver;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public abstract class AbstractTestModule
{

   protected WebDriver driver()
   {
      return IDE.getInstance().driver();
   }

   protected Selenium selenium()
   {
      return IDE.getInstance().getSelenium();
   }

   protected IDE IDE()
   {
      return IDE.getInstance();
   }
}