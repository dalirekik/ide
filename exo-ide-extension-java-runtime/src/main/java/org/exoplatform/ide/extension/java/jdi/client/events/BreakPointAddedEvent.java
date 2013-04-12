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
package org.exoplatform.ide.extension.java.jdi.client.events;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.extension.java.jdi.shared.BreakPoint;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class BreakPointAddedEvent extends GwtEvent<BreakPointAddedHandler> {

    private BreakPoint breakPoint;

    public BreakPointAddedEvent(BreakPoint breakPoint) {
        this.breakPoint = breakPoint;
    }

    public static final GwtEvent.Type<BreakPointAddedHandler> TYPE = new GwtEvent.Type<BreakPointAddedHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<BreakPointAddedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(BreakPointAddedHandler handler) {
        handler.onAddBreakPoint(this);
    }

    public BreakPoint getBreakPoint() {
        return breakPoint;
    }

}
