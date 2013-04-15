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
 * Event occurs when user tries to show breakpoint properties.
 *
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: ShowBreakpointPropertiesEvent.java May 8, 2012 13:00:37 PM azatsarynnyy $
 */
public class ShowBreakpointPropertiesEvent extends GwtEvent<ShowBreakpointPropertiesHandler> {
    /** Type used to register this event. */
    public static final GwtEvent.Type<ShowBreakpointPropertiesHandler> TYPE =
            new GwtEvent.Type<ShowBreakpointPropertiesHandler>();

    /** Current breakpoint. */
    private BreakPoint breakPoint;

    /**
     * @param breakPoint
     *         current breakpoint
     */
    public ShowBreakpointPropertiesEvent(BreakPoint breakPoint) {
        this.breakPoint = breakPoint;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#getAssociatedType() */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ShowBreakpointPropertiesHandler> getAssociatedType() {
        return TYPE;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler) */
    @Override
    protected void dispatch(ShowBreakpointPropertiesHandler handler) {
        handler.onShowBreakpointProperties(this);
    }

    /** @return the breakpoint */
    public BreakPoint getBreakPoint() {
        return breakPoint;
    }
}