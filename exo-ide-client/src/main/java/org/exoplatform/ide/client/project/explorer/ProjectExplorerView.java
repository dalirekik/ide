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
package org.exoplatform.ide.client.project.explorer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.gwtframework.ui.client.api.TreeGridItem;
import org.exoplatform.gwtframework.ui.client.component.IconButton;
import org.exoplatform.gwtframework.ui.client.component.TreeIconPosition;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay;
import org.exoplatform.ide.client.framework.ui.ProjectTree;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ProjectExplorerView extends ViewImpl implements ProjectExplorerDisplay
{

   public static final String ID = "ideTinyProjectExplorerView";

   /**
    * Initial width of this view
    */
   private static final int WIDTH = 250;

   /**
    * Initial height of this view
    */
   private static final int HEIGHT = 450;

   private static TinyProjectExplorerViewUiBinder uiBinder = GWT.create(TinyProjectExplorerViewUiBinder.class);

   interface TinyProjectExplorerViewUiBinder extends UiBinder<Widget, ProjectExplorerView>
   {
   }

   @UiField
   //ItemTree treeGrid;
   ProjectTree treeGrid;

   @UiField
   HTMLPanel projectNotOpenedPanel;

   @UiField
   ProjectsListGrid projectsListGrid;

   @UiField
   IconButton linkWithEditorButton;

   private static final String TITLE = IDE.IDE_LOCALIZATION_CONSTANT.projectExplorerViewTitle();

   public ProjectExplorerView()
   {
      super(ID, "navigation", TITLE, new Image(IDEImageBundle.INSTANCE.projectExplorer()), WIDTH, HEIGHT);
      add(uiBinder.createAndBindUi(this));
      setCanShowContextMenu(true);
   }
   
   @Override
   public void activate()
   {
      super.activate();
      
      if (treeGrid.isVisible())
      {
         treeGrid.getElement().focus();
      }
      else
      {
         projectsListGrid.getElement().focus();
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#getBrowserTree()
    */
   @Override
   public TreeGridItem<Item> getBrowserTree()
   {
      return treeGrid;
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#getSelectedItems()
    */
   @Override
   public List<Item> getSelectedItems()
   {
      return treeGrid.getSelectedItems();
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#deselectItem(java.lang.String)
    */
   @Override
   public void deselectItem(String path)
   {
      treeGrid.deselectItem(path);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#updateItemState(org.exoplatform.ide.vfs.client.model.FileModel)
    */
   @Override
   public void updateItemState(FileModel file)
   {
      treeGrid.updateFileState(file);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#setLockTokens(java.util.Map)
    */
   @Override
   public void setLockTokens(Map<String, String> locktokens)
   {
      treeGrid.setLockTokens(locktokens);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#addItemsIcons(java.util.Map)
    */
   @Override
   public void addItemsIcons(Map<Item, Map<TreeIconPosition, ImageResource>> itemsIcons)
   {
      treeGrid.addItemsIcons(itemsIcons);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#removeItemIcons(java.util.Map)
    */
   @Override
   public void removeItemIcons(Map<Item, TreeIconPosition> itemsIcons)
   {
      treeGrid.removeItemIcons(itemsIcons);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#setProjectExplorerTreeVisible(boolean)
    */
   @Override
   public void setProjectExplorerTreeVisible(boolean visible)
   {
      treeGrid.setVisible(visible);

      if (visible)
      {
         projectNotOpenedPanel.setVisible(!visible);
         projectsListGrid.setVisible(!visible);
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#setProjectsListGridVisible(boolean)
    */
   @Override
   public void setProjectsListGridVisible(boolean visible)
   {
      projectsListGrid.setVisible(visible);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#setProjectNotOpenedPanelVisible(boolean)
    */
   @Override
   public void setProjectNotOpenedPanelVisible(boolean visible)
   {
      projectNotOpenedPanel.setVisible(visible);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#getLinkWithEditorButton()
    */
   @Override
   public HasClickHandlers getLinkWithEditorButton()
   {
      return linkWithEditorButton;
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#setLinkWithEditorButtonEnabled(boolean)
    */
   @Override
   public void setLinkWithEditorButtonEnabled(boolean enabled)
   {
      linkWithEditorButton.setEnabled(enabled);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#setLinkWithEditorButtonSelected(boolean)
    */
   @Override
   public void setLinkWithEditorButtonSelected(boolean selected)
   {
      linkWithEditorButton.setSelected(selected);
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#getProjectsListGrid()
    */
   @Override
   public ProjectsListGrid getProjectsListGrid()
   {
      return projectsListGrid;
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay#getSelectedProjects()
    */
   @Override
   public List<ProjectModel> getSelectedProjects()
   {
      return projectsListGrid.getSelectedItems();
   }

   @Override
   public void navigateToItem(Item item)
   {
      treeGrid.navigateToItem(item);
   }

   @Override
   public void refreshTree()
   {
      treeGrid.refresh();
   }

   @Override
   public boolean selectItem(Item item)
   {
      return treeGrid.selectItem(item);
   }

   @Override
   public List<Item> getVisibleItems()
   {
      return treeGrid.getVisibleItems();
   }

}
