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

package com.codenvy.ide.ui;

import elemental.html.Element;

import com.codenvy.ide.runtime.Assert;
import com.codenvy.ide.ui.menu.AutoHideComponent;
import com.codenvy.ide.ui.menu.AutoHideView;
import com.codenvy.ide.ui.menu.PositionController;
import com.codenvy.ide.ui.menu.PositionController.Positioner;
import com.codenvy.ide.util.dom.Elements;
import com.google.gwt.dom.client.Node;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;


/** Represents a floating popup, that can be attached to any element. */
public class Popup extends AutoHideComponent<Popup.View, AutoHideComponent.AutoHideModel> implements IsWidget {

    public interface Css extends CssResource {
        String root();

        String contentHolder();
    }

    public interface Resources extends ClientBundle {
        @Source({"com/codenvy/ide/common/constants.css", "Popup.css", "com/codenvy/ide/api/ui/style.css"})
        Css popupCss();
    }

    /** The View for the Popup component. */
    public static class View extends AutoHideView<Void> {
        private final Css css;

        private final Element contentHolder;

        View(Resources resources) {
            this.css = resources.popupCss();

            contentHolder = Elements.createDivElement(css.contentHolder());

            Element rootElement = Elements.createDivElement(css.root());
            rootElement.appendChild(contentHolder);
            setElement(rootElement);
        }

        void setContentElement(Element contentElement) {
            contentHolder.setInnerHTML("");
            if (contentElement != null) {
                contentHolder.appendChild(contentElement);
            }
        }
    }

    public static Popup create(Resources resources) {
        View view = new View(resources);
        return new Popup(view);
    }

    private PositionController positionController;

    private HTML widget;

    private Popup(View view) {
        super(view, new AutoHideModel());
    }

    @Override
    public void show() {
        Assert.isNotNull(positionController, "You cannot show this popup without using a position controller");
        positionController.updateElementPosition();

        cancelPendingHide();
        super.show();
    }

    /** Shows the popup anchored to a given element. */
    public void show(Positioner positioner) {
        positionController = new PositionController(positioner, getView().getElement());
        show();
    }

    /**
     * Sets the popup's content element.
     *
     * @param contentElement
     *         the DOM element to show in the popup, or {@code null}
     *         to clean up the popup's DOM
     */
    public void setContentElement(Element contentElement) {
        getView().setContentElement(contentElement);
    }

    public void destroy() {
        forceHide();
        setContentElement(null);
        positionController = null;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        if (widget == null) {
            widget = new HTML();
            widget.getElement().appendChild((Node)getView().getElement());
        }

        return widget;
    }
}
