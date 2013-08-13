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
package com.codenvy.ide.ext.java.client.inject;

import com.codenvy.ide.ext.java.client.wizard.NewJavaClassPageView;
import com.codenvy.ide.ext.java.client.wizard.NewJavaClassPageViewImpl;
import com.codenvy.ide.ext.java.client.wizard.NewPackagePageView;
import com.codenvy.ide.ext.java.client.wizard.NewPackagePageViewImpl;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.google.gwt.inject.client.AbstractGinModule;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
@ExtensionGinModule
public class JavaGinModule extends AbstractGinModule {

    /** @see com.google.gwt.inject.client.AbstractGinModule#configure() */
    @Override
    protected void configure() {
        bind(NewPackagePageView.class).to(NewPackagePageViewImpl.class);
        bind(NewJavaClassPageView.class).to(NewJavaClassPageViewImpl.class);
    }
}