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
package org.exoplatform.ide.client.permissions.control;

import org.exoplatform.gwtframework.ui.client.component.command.SimpleControl;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.vfs.File;
import org.exoplatform.ide.client.module.navigation.event.selection.ItemsSelectedEvent;
import org.exoplatform.ide.client.module.navigation.event.selection.ItemsSelectedHandler;
import org.exoplatform.ide.client.permissions.event.ShowPermissionsEvent;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Oct 19, 2010 $
 *
 */
public class ShowPermissionsControl extends SimpleControl implements ItemsSelectedHandler
{
   public static final String ID = "View/Permissions";

   public static final String TITLE = "Show Permissions";

   public static final String PROMPT = "Show Permissions";

  
   public ShowPermissionsControl(HandlerManager eventBus)
   {
      super(ID);
      setTitle(TITLE);
      setPrompt(PROMPT);
      setImages(IDEImageBundle.INSTANCE.properties(), IDEImageBundle.INSTANCE.propertiesDisabled());
      setEvent(new ShowPermissionsEvent());
      setVisible(true);
      eventBus.addHandler(ItemsSelectedEvent.TYPE, this);
   }


   /**
    * @see org.exoplatform.ide.client.module.navigation.event.selection.ItemsSelectedHandler#onItemsSelected(org.exoplatform.ide.client.module.navigation.event.selection.ItemsSelectedEvent)
    */
   public void onItemsSelected(ItemsSelectedEvent event)
   {
      if (event.getSelectedItems().size() != 1 || !(event.getSelectedItems().get(0) instanceof File))
      {
         setEnabled(false);
         return;
      }
      setEnabled(true);
   }

}
