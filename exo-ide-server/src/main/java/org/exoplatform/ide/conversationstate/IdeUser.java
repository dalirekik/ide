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
package org.exoplatform.ide.conversationstate;

import java.util.Collection;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class IdeUser
{
   private String userId;

   private Collection<String> groups;

   private Collection<String> roles;

   private String clientId;

   public IdeUser()
   {
   }

   /**
    * @param userId the userId to set
    * @param groups the groups to set
    * @param roles the roles to set
    * @param clientId
    */
   public IdeUser(String userId, Collection<String> groups, Collection<String> roles, String clientId)
   {
      this.userId = userId;
      this.groups = groups;
      this.roles = roles;
      this.clientId = clientId;
   }

   /**
    * @return the userId
    */
   public String getUserId()
   {
      return userId;
   }

   /**
    * @param userId the userId to set
    */
   public void setUserId(String userId)
   {
      this.userId = userId;
   }

   /**
    * @return the groups
    */
   public Collection<String> getGroups()
   {
      return groups;
   }

   /**
    * @param groups the groups to set
    */
   public void setGroups(Collection<String> groups)
   {
      this.groups = groups;
   }

   /**
    * @return the roles
    */
   public Collection<String> getRoles()
   {
      return roles;
   }

   /**
    * @param roles the roles to set
    */
   public void setRoles(Collection<String> roles)
   {
      this.roles = roles;
   }

   /**
    * @return the client id
    */
   public String getClientId()
   {
      return clientId;
   }

   /**
    * @param clientId the client id to set
    */
   public void setClientId(String clientId)
   {
      this.clientId = clientId;
   }
}