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
package org.exoplatform.ide.operation.browse;

import static org.junit.Assert.assertEquals;

import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.MenuCommands;
import org.junit.Test;

/**
 * IDE-14 Opening file if some files were deleted from the same folder.
 * 
 * Created by The eXo Platform SAS.
 *	
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id:   ${date} ${time}
 *
 */
public class OpeningFilesTest extends BaseTest
{
   
   private final String folderName = OpeningFilesTest.class.getSimpleName();

   private final String file1Name = "File1";

   private final String file2Name = "File2";

   private final String file1Content = "New text file content for test.";

   @Test
   public void testDeleteFileAndOpenFromOneFolder() throws Exception
   {
      IDE.WORKSPACE.waitForRootItem();

      IDE.NAVIGATION.createFolder(folderName);

      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.TEXT_FILE);
      
      IDE.EDITOR.typeTextIntoEditor(0, file1Content);
      IDE.NAVIGATION.saveFileAs(file1Name);
      IDE.WORKSPACE.waitForRootItem();
      IDE.EDITOR.closeFile(0);

      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.HTML_FILE);
      IDE.NAVIGATION.saveFileAs(file2Name);
      IDE.WORKSPACE.waitForRootItem();
      IDE.EDITOR.closeFile(0);
      IDE.NAVIGATION.assertItemVisible(WS_URL + folderName + "/" + file2Name);

      // Delete one file  
      IDE.WORKSPACE.selectItem(WS_URL + folderName + "/" + file2Name);
      IDE.NAVIGATION.deleteSelectedItems();
      IDE.WORKSPACE.waitForRootItem();
      IDE.NAVIGATION.assertItemNotVisible(WS_URL + folderName + "/" + file2Name);

      //Open another file from the same folder
      IDE.NAVIGATION.openFileFromNavigationTreeWithCodeEditor(WS_URL + folderName + "/" + file1Name, false);
      
      //Check text of opened file
      String text = IDE.EDITOR.getTextFromCodeEditor(0);
      assertEquals(file1Content, text);

      //Delete folder with file
      IDE.WORKSPACE.selectItem(WS_URL + folderName + "/");
      IDE.NAVIGATION.deleteSelectedItems();
      IDE.WORKSPACE.waitForRootItem();

      //Check items not present in navigation tree
      IDE.NAVIGATION.assertItemNotVisible(WS_URL + folderName + "/");
      IDE.NAVIGATION.assertItemNotVisible(WS_URL + folderName + "/" + file1Name);
   }

}
