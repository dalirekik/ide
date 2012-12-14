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
package org.exoplatform.ide.client.framework.project;

import org.exoplatform.ide.vfs.shared.Property;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event occurs, when user tries to convert folder to project. Implement {@link ConvertToProjectHandler} to handle event.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Oct 27, 2011 3:53:03 PM anya $
 *
 */
public class ConvertToProjectEvent extends GwtEvent<ConvertToProjectHandler>
{
   /**
    * Type used to register event.
    */
   public static final GwtEvent.Type<ConvertToProjectHandler> TYPE = new GwtEvent.Type<ConvertToProjectHandler>();

   private String folderId;

   private String vfsId;
   
   private List<Property>  properties = new ArrayList<Property>();

   public ConvertToProjectEvent()
   {
   }

   public ConvertToProjectEvent(String folderId, String vfsId)
   {
      this.folderId = folderId;
      this.vfsId = vfsId;
   }
   
   public ConvertToProjectEvent(String folderId, String vfsId, List<Property> properties)
   {
      this.folderId = folderId;
      this.vfsId = vfsId;
      this.properties = properties;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
    */
   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<ConvertToProjectHandler> getAssociatedType()
   {
      return TYPE;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
    */
   @Override
   protected void dispatch(ConvertToProjectHandler handler)
   {
      handler.onConvertToProject(this);
   }

   public String getFolderId()
   {
      return folderId;
   }

   public String getVfsId()
   {
      return vfsId;
   }
   
   public List<Property> getProperties()
   {
      return properties;
   }
}