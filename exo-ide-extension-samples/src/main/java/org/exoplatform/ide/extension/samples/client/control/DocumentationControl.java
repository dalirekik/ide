/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.ide.extension.samples.client.control;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.extension.samples.client.SamplesClientBundle;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public class DocumentationControl extends SimpleControl implements IDEControl {
    private static final String ID = "Help/Documentation";

    private static final String TITLE = "Documentation";

    public static final String SUPPORT_GROUP_ID = "Support";

    public DocumentationControl() {
        super(ID);
        setTitle(TITLE);
        setGroupName(SUPPORT_GROUP_ID);
        setVisible(true);
        setEnabled(true);
        setImages(SamplesClientBundle.INSTANCE.help(), SamplesClientBundle.INSTANCE.helpDisabled());

        getAttributes().put("onClick", "javascript:window.open('https://codenvy.com/docs/');");
    }


    @Override
    public void initialize() {
    }
}