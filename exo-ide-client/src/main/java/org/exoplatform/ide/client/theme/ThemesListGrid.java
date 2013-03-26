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

package org.exoplatform.ide.client.theme;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;

import org.exoplatform.gwtframework.ui.client.component.ListGrid;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ThemesListGrid extends ListGrid<Theme>
{

   public ThemesListGrid()
   {
//      Column<Theme, ImageResource> iconColumn = new Column<Theme, ImageResource>(new ImageResourceCell())
//      {
//         @Override
//         public ImageResource getValue(Theme object)
//         {
//            return ProjectResolver.getImageForProject(object.getProjectType());
//         }
//      };

      Column<Theme, String> themeNameColumn = new Column<Theme, String>(new TextCell())
      {
         @Override
         public String getValue(Theme object)
         {
            return object.getName();
         }
      };

      themeNameColumn.setCellStyleNames("default-cursor");

      getCellTable().addColumn(themeNameColumn, "");
      
//      getCellTable().setColumnWidth(iconColumn, "20px");
//      getCellTable().addColumn(nameColumn, "Name");
//      getCellTable().addColumn(typeColumn, "Type");
   }

}