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
package org.exoplatform.ide.client.messages;

import com.google.gwt.i18n.client.Constants;

/**
 * Interface to represent the constants contained in resource bundle: 'IdePermissionsLocalizationConstant.properties'.
 * <p/>
 * Localization message for form from permissions group.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: IdePreferencesLocalizationConstant.java Jun 3, 2011 12:58:29 PM vereshchaka $
 */
public interface IdePermissionsLocalizationConstant extends Constants {
    /*
     * PermissionsListGrid
     */
    @Key("permissions.list.grid.identity")
    String listGridIdentity();

    @Key("permissions.list.grid.read")
    String listGridRead();

    @Key("permissions.list.grid.write")
    String listGridWrite();

    /*
     * PermissionsManagerForm
     */
    @Key("permissions.title")
    String permissionsTitle();

    @Key("permissions.name")
    String permissionsName();

    @Key("permissions.owner")
    String permissionsOwner();

    /*
     * PermissionsManagerPresenter
     */
    @Key("permissions.setAclFailure")
    String permissionsSetAclFailure();

    @Key("permissions.noAclProperty")
    String permissionsNoAclProperty();

}
