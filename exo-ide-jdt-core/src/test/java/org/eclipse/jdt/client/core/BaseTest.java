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
package org.eclipse.jdt.client.core;

import com.googlecode.gwt.test.GwtTest;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: 4:00:10 PM 34360 2009-07-22 23:58:59Z evgen $
 */
public abstract class BaseTest extends GwtTest {

    /**
     *
     */
    public BaseTest() {
        super();
    }

    /** @see com.google.gwt.junit.client.GWTTestCase#getModuleName() */
    @Override
    public String getModuleName() {
        return "org.eclipse.jdt.IdeJdt";
    }

}