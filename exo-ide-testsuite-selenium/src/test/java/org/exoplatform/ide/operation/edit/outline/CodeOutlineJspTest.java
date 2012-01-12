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
package org.exoplatform.ide.operation.edit.outline;

import static org.junit.Assert.assertTrue;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.exoplatform.ide.vfs.shared.Link;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: CodeOutlineJspTest Apr 27, 2011 10:47:14 AM evgen $
 *
 */
public class CodeOutlineJspTest extends BaseTest
{

   private final static String FILE_NAME = "JspCodeOutline.jsp";

   private final static String PROJECT = CodeOutlineJspTest.class.getSimpleName();

   @BeforeClass
   public static void setUp()
   {
      String filePath = "src/test/resources/org/exoplatform/ide/operation/edit/outline/test-jsp.jsp";
      try
      {
         Map<String, Link> project = VirtualFileSystemUtils.createDefaultProject(PROJECT);
         Link link = project.get(Link.REL_CREATE_FILE);
         VirtualFileSystemUtils.createFileFromLocal(link, FILE_NAME, MimeType.APPLICATION_JSP, filePath);
      }
      catch (Exception e)
      {
      }
   }

   @AfterClass
   public static void tearDown()
   {
      try
      {
         VirtualFileSystemUtils.delete(WS_URL + PROJECT);
      }
      catch (Exception e)
      {
      }
   }

   @Test
   public void testCodeOutlineJSP() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitOpened();
      IDE.PROJECT.OPEN.openProject(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + FILE_NAME);
      IDE.LOADER.waitClosed();

      IDE.PROJECT.EXPLORER.openItem(PROJECT + "/" + FILE_NAME);
      IDE.EDITOR.waitActiveFile(PROJECT + "/" + FILE_NAME);

      IDE.TOOLBAR.runCommand(ToolbarCommands.View.SHOW_OUTLINE);
      IDE.OUTLINE.waitOpened();
      IDE.OUTLINE.waitOutlineTreeVisible();
      
      assertTrue(IDE.OUTLINE.isItemPresentById("html:TAG:1"));
      IDE.GOTOLINE.goToLine(9);

      waitForElementPresent("a:VARIABLE:9");
      assertTrue(IDE.OUTLINE.isItemPresentById("a:VARIABLE:9"));

      IDE.GOTOLINE.goToLine(23);

      waitForElementPresent("a:PROPERTY:23");
      assertTrue(IDE.OUTLINE.isItemPresentById("a:PROPERTY:23"));
      assertTrue(IDE.OUTLINE.isItemPresentById("head:TAG:2"));
      assertTrue(IDE.OUTLINE.isItemPresentById("script:TAG:8"));
      assertTrue(IDE.OUTLINE.isItemPresentById("body:TAG:12"));
      assertTrue(IDE.OUTLINE.isItemPresentById("java code:JSP_TAG:13"));
      assertTrue(IDE.OUTLINE.isItemPresentById("curentState:PROPERTY:14"));
      assertTrue(IDE.OUTLINE.isItemPresentById("identity:PROPERTY:17"));
      assertTrue(IDE.OUTLINE.isItemPresentById("i:PROPERTY:18"));

      IDE.EDITOR.closeFile(FILE_NAME);
      IDE.EDITOR.waitTabNotPresent(FILE_NAME);
   }

}
