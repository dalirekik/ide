/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.ide.extension.heroku.shared;

/**
 * Authentication credentials.
 *
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: Credentials.java Mar 19, 2012 2:57:20 PM azatsarynnyy $
 */
public interface Credentials {
    /**
     * Returns the e-mail.
     *
     * @return e-mail.
     */
    public String getEmail();

    /**
     * Set the e-mail.
     *
     * @param email
     */
    public void setEmail(String email);

    /**
     * Returns the password.
     *
     * @return password.
     */
    public String getPassword();

    /**
     * Set the password.
     *
     * @param password
     *         password.
     */
    public void setPassword(String password);
}
