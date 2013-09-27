/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.exoplatform.ide.client.framework.ui.impl.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class SetViewVisibleEvent extends GwtEvent<SetViewVisibleHandler> {

    public static final GwtEvent.Type<SetViewVisibleHandler> TYPE = new GwtEvent.Type<SetViewVisibleHandler>();

    private String viewId;

    public SetViewVisibleEvent(String viewId) {
        this.viewId = viewId;
    }

    public String getViewId() {
        return viewId;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<SetViewVisibleHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SetViewVisibleHandler handler) {
        handler.onSetViewVisible(this);
    }

}
