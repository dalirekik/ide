/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.api.projecttree.generic;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.web.bindery.event.shared.EventBus;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/** @author Artem Zatsarynnyy */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseNodeTest {
    @Mock
    protected EventBus               eventBus;
    @Mock
    protected ProjectServiceClient   projectServiceClient;
    @Mock
    protected DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    protected GenericTreeStructure   treeStructure;

    @Before
    public void setUp() {
    }
}
