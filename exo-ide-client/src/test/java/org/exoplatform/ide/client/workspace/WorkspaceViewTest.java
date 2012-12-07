/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.ide.client.workspace;

import com.googlecode.gwt.test.GwtTestWithMockito;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 6, 2012  
 */
public class WorkspaceViewTest extends GwtTestWithMockito
{

   /**
   * {@inheritDoc}
   */
   @Override
   public String getModuleName()
   {
      return "org.exoplatform.ide.IDE";
   }
   
   @Ignore
   @Test
   public void shouldSelectOnClick()
   {
      // unable to test cell widgets with gwt-test-utils
   }

}