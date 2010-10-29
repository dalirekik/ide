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
package org.exoplatform.ide.operation.edit.outline;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.TestConstants;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * IDE-170:Test closing Code Outline panel.
 * 
 * @author <a href="mailto:roman.iyvshyn@exoplatform.com">Roman Iyvshyn</a>
 * @version $Id: Aug 11, 2010
 *
 */

public class ClosingOutlinePanelTest extends BaseTest
{
   private final static String JAVASCRIPT_FILE_NAME = "TestJavaScriptFile.js";

   private final static String OUTLINE = "ideOutlineTreeGrid";

   //   private final static String LAP = ;

   private final static String TEXT_FILE_NAME = "SampleTextFile.txt";

   private final static String FOLDER_NAME = "TestFolder";

   private final static String URL = BASE_URL + REST_CONTEXT + "/" + WEBDAV_CONTEXT + "/" + REPO_NAME + "/" + WS_NAME
      + "/" + FOLDER_NAME + "/";

   @BeforeClass
   public static void setUp()
   {

      String javaScriptFilePath = "src/test/resources/org/exoplatform/ide/operation/edit/outline/TestJavaScriptFile.js";
      String textFilePath = "src/test/resources/org/exoplatform/ide/operation/edit/outline/SampleTextFile.txt";

      try
      {
         //*******TODO****Fix
         VirtualFileSystemUtils.mkcol(URL);
         //*******************

         VirtualFileSystemUtils.put(javaScriptFilePath, MimeType.APPLICATION_JAVASCRIPT, URL + JAVASCRIPT_FILE_NAME);
         VirtualFileSystemUtils.put(textFilePath, MimeType.TEXT_PLAIN, URL + TEXT_FILE_NAME);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ModuleException e)
      {
         e.printStackTrace();
      }
   }

   @AfterClass
   public static void tearDown()
   {
      try
      {
         VirtualFileSystemUtils.delete(URL + JAVASCRIPT_FILE_NAME);
         VirtualFileSystemUtils.delete(URL + TEXT_FILE_NAME);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ModuleException e)
      {
         e.printStackTrace();
      }
   }

   public void afterMethod()
   {
      deleteCookies();
      cleanRegistry();
      cleanDefaultWorkspace();
   }

   //IDE-170:Test closing Code Outline panel
   @Test
   public void testClosingOutlinePanel() throws Exception
   {
      //----- 1 -------------
      //open JavaScript file.
      Thread.sleep(TestConstants.SLEEP);
      selectItemInWorkspaceTree(WS_NAME);
      runToolbarButton(ToolbarCommands.File.REFRESH);
      Thread.sleep(TestConstants.SLEEP);
      openOrCloseFolder(FOLDER_NAME);
      Thread.sleep(TestConstants.REDRAW_PERIOD);
      openFileFromNavigationTreeWithCodeEditor(JAVASCRIPT_FILE_NAME, false);
      Thread.sleep(TestConstants.SLEEP);

      //----- 2 -------------
      //show Code Outline panel
      runToolbarButton(ToolbarCommands.View.SHOW_OUTLINE);
      Thread.sleep(TestConstants.SLEEP);
      //check Code Outline present
      checkCodeHelperPanelPresent(true);

      //----- 3 -------------
      //open text file.
      openFileFromNavigationTreeWithCodeEditor(TEXT_FILE_NAME, false);
      Thread.sleep(TestConstants.SLEEP);
      //check Code Outline in Not Present
      checkCodeHelperPanelVisibility(false);
      Thread.sleep(TestConstants.SLEEP);

      //return to the tab with JavaScript.
      selectEditorTab(0);
      //check Code Outline present
      checkCodeHelperPanelVisibility(true);
      Thread.sleep(TestConstants.SLEEP);

      //----- 4 -------------
      //close code outline panel.
      closeTab("0");

      //check Code Outline in Not Present
      checkCodeHelperPanelVisibility(false);
      Thread.sleep(TestConstants.SLEEP);

      //----- 5 -------------
      //refresh page
      selenium.refresh();
      selenium.waitForPageToLoad("" + TestConstants.IDE_LOAD_PERIOD);
      Thread.sleep(TestConstants.SLEEP);
      //check outline is not present
      checkCodeHelperPanelPresent(false);

   }

   //---- implementation -----------

   private void checkCodeHelperPanelPresent(boolean isPresent)
   {
      if (isPresent)
      {

         assertTrue(selenium.isElementPresent("scLocator=//TreeGrid[ID=\"ideOutlineTreeGrid\"]/body/"));
      }
      else
      {
         assertFalse(selenium.isElementPresent("scLocator=//TreeGrid[ID=\"ideOutlineTreeGrid\"]/body/"));

      }
   }

   private void checkCodeHelperPanelVisibility(boolean isVisible)
   {
    //TODO********fix***locators*******
      if (isVisible)
      {
         assertTrue(selenium
            .isElementPresent("//div[@eventproxy='CodeHelper' and contains(@style, 'visibility: inherit')]/div[@eventproxy='CodeHelper']"));
         assertFalse(selenium
            .isElementPresent("//div[@eventproxy='CodeHelper' and contains(@style, 'visibility: hidden')]/div[@eventproxy='CodeHelper']"));
      }
      else
      {
         assertTrue(selenium
            .isElementPresent("//div[@eventproxy='CodeHelper' and contains(@style, 'visibility: hidden')]/div[@eventproxy='CodeHelper']"));
         assertFalse(selenium
            .isElementPresent("//div[@eventproxy='CodeHelper' and contains(@style, 'visibility: inherit')]/div[@eventproxy='CodeHelper']"));
      //***********************************
      }
   }

}