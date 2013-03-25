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
package org.exoplatform.ide.git.client.control;

import org.exoplatform.ide.client.framework.control.GroupNames;
import org.exoplatform.ide.git.client.GitClientBundle;
import org.exoplatform.ide.git.client.GitExtension;
import org.exoplatform.ide.git.client.init.InitRepositoryEvent;
import org.exoplatform.ide.vfs.client.model.ItemContext;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.List;

/**
 * Control for initializing the repository.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Mar 23, 2011 5:36:00 PM anya $
 * 
 */
public class InitRepositoryControl extends GitControl
{
   public InitRepositoryControl()
   {
      super(GitExtension.MESSAGES.initControlId());
      setTitle(GitExtension.MESSAGES.initControlTitle());
      setPrompt(GitExtension.MESSAGES.initControlPrompt());
      setEvent(new InitRepositoryEvent());
      setImages(GitClientBundle.INSTANCE.initRepo(), GitClientBundle.INSTANCE.initRepoDisabled());
      setVisible(true);
      setEnableState(EnableState.BEFORE_INIT);
      setGroupName(GroupNames.ACTIONS);
   }

//   protected void updateControlState()
//   {
//      if (workspace == null)
//      {
//         setVisible(false);
//         return;
//      }
//
//      if (selectedProject == null || !isProjectExplorerVisible)
//      {
//         setVisible(false);
//         return;
//      }
//
//      if (selectedItem == null
//         || (isWorkspaceSelected(selectedItem.getId()) || !isProjectSelected((ItemContext)selectedItem)))
//      {
//         setVisible(false);
//         return;
//      }
//
//      setVisible(true);
//
//      List<Item> itemList = selectedProject.getChildren().getItems();
//      for (Item child : itemList)
//      {
//         if (".git".equals(child.getName()))
//         {
//            setEnabled(false);
//            return;
//         }
//      }
//
//      if (enableState == EnableState.BEFORE_INIT)
//         setEnabled(true);
//      else
//         setEnabled(false);
//   }
}