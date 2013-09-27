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
package org.exoplatform.ide.extension.heroku.server.rest;

import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.ide.commons.server.ParsingResponseException;

import org.exoplatform.ide.extension.heroku.server.Heroku;
import org.exoplatform.ide.extension.heroku.server.HerokuException;
import org.exoplatform.ide.extension.heroku.shared.HerokuKey;
import org.exoplatform.ide.extension.heroku.shared.Stack;
import org.exoplatform.ide.extension.ssh.server.SshKeyStoreException;
import org.exoplatform.ide.security.paas.CredentialStoreException;
import org.exoplatform.ide.vfs.server.LocalPathResolver;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.shared.Project;
import org.exoplatform.ide.vfs.shared.Property;
import org.exoplatform.ide.vfs.shared.PropertyFilter;
import org.exoplatform.ide.vfs.shared.PropertyImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST interface to {@link Heroku}.
 *
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("{ws-name}/heroku")
public class HerokuService {
    private static final Log LOG = ExoLogger.getLogger(HerokuService.class);

    @Inject
    private Heroku heroku;

    @Inject
    private LocalPathResolver localPathResolver;

    @Inject
    private VirtualFileSystemRegistry vfsRegistry;

    @QueryParam("vfsid")
    private String vfsId;

    @QueryParam("projectid")
    private String projectId;

    @QueryParam("name")
    private String appName;

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void login(Map<String, String> credentials)
            throws HerokuException, IOException, ParsingResponseException, CredentialStoreException {
        heroku.login(credentials.get("email"), credentials.get("password"));
    }

    @Path("logout")
    @POST
    public void logout() throws CredentialStoreException {
        heroku.logout();
    }

    @Path("keys")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<HerokuKey> keysList(@QueryParam("long") boolean inLongFormat)
            throws HerokuException, IOException, ParsingResponseException, CredentialStoreException {
        return heroku.listSshKeys(inLongFormat);
    }

    @Path("keys/add")
    @POST
    public void keysAdd() throws HerokuException, IOException, SshKeyStoreException, CredentialStoreException {
        heroku.addSshKey();
    }

    @Path("apps/create")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> appsCreate(@QueryParam("remote") final String remote)
            throws HerokuException, IOException, ParsingResponseException, VirtualFileSystemException, CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        Map<String, String> application = heroku.createApplication(appName,
                                                                   remote,
                                                                   projectId != null ? new java.io.File(
                                                                           localPathResolver.resolve(vfs, projectId)) : null
                                                                  );

        // Update VFS properties. Need it to uniform client.
        Property herokuAppProperty = new PropertyImpl("heroku-application", application.get("name"));
        List<Property> properties = new ArrayList<Property>(1);
        properties.add(herokuAppProperty);
        vfs.updateItem(projectId, properties, null);

        Project project = (Project)vfs.getItem(projectId, false, PropertyFilter.ALL_FILTER);
        String value = project.getPropertyValue("isGitRepository");
        if (value == null || !value.equals("true")) {
            Property isGitRepositoryProperty = new PropertyImpl("isGitRepository", "true");
            List<Property> propertiesList = new ArrayList<Property>(1);
            propertiesList.add(isGitRepositoryProperty);
            vfs.updateItem(projectId, propertiesList, null);
        }
        LOG.info("EVENT#application-created# WS#" + EnvironmentContext.getCurrent().getVariable(EnvironmentContext.WORKSPACE_NAME)
                 + "# USER#" + ConversationState.getCurrent().getIdentity().getUserId() + "# PROJECT#" + project.getName() + "# TYPE#" + project.getProjectType()
                 + "# PAAS#Heroku#");
        return application;
    }

    @Path("apps/destroy")
    @POST
    public void appsDestroy() throws HerokuException, IOException, VirtualFileSystemException, CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        heroku.destroyApplication(appName,
                                  projectId != null ? new java.io.File(localPathResolver.resolve(vfs, projectId)) : null
                                 );

        if (projectId != null) {
            // Update VFS properties. Need it to uniform client.
            Property p = new PropertyImpl("heroku-application", Collections.<String>emptyList());
            List<Property> properties = new ArrayList<Property>(1);
            properties.add(p);
            vfs.updateItem(projectId, properties, null);
        }
    }

    @Path("apps/info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> appsInfo(@QueryParam("raw") boolean inRawFormat)
            throws HerokuException, IOException, ParsingResponseException, VirtualFileSystemException, CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        return heroku.applicationInfo(appName,
                                      inRawFormat,
                                      projectId != null ? new java.io.File(localPathResolver.resolve(vfs, projectId)) : null
                                     );
    }

    @Path("apps")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> appsList() throws HerokuException, ParsingResponseException, IOException,
                                          CredentialStoreException {
        return heroku.listApplications();
    }

    @Path("apps/rename")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> appsRename(@QueryParam("newname") String newname)
            throws HerokuException, IOException, ParsingResponseException, VirtualFileSystemException, CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        Map<String, String> application = heroku.renameApplication(appName,
                                                                   newname,
                                                                   projectId != null ? new java.io.File(
                                                                           localPathResolver.resolve(vfs, projectId)) : null
                                                                  );

        if (projectId != null) {
            // Update VFS properties. Need it to uniform client.
            Property p = new PropertyImpl("heroku-application", application.get("name"));
            List<Property> properties = new ArrayList<Property>(1);
            properties.add(p);
            vfs.updateItem(projectId, properties, null);
        }

        return application;
    }

    @Path("apps/stack")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Stack> appsStack()
            throws HerokuException, IOException, ParsingResponseException, VirtualFileSystemException, CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        return heroku.getStacks(appName,
                                projectId != null ? new java.io.File(localPathResolver.resolve(vfs, projectId)) : null
                               );
    }

    @Path("apps/stack-migrate")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public byte[] stackMigrate(@QueryParam("stack") String stack)
            throws HerokuException, IOException, VirtualFileSystemException, CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        return heroku.stackMigrate(appName,
                                   projectId != null ? new java.io.File(localPathResolver.resolve(vfs, projectId)) : null,
                                   stack
                                  );
    }

    @Path("apps/logs")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public byte[] logs(@QueryParam("num") int logLines)
            throws HerokuException, IOException, VirtualFileSystemException, java.security.GeneralSecurityException,
                   CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        return heroku.logs(appName,
                           projectId != null ? new java.io.File(localPathResolver.resolve(vfs, projectId)) : null,
                           logLines
                          );
    }

    @Path("apps/run")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public byte[] run(final String command)
            throws HerokuException, IOException, ParsingResponseException, VirtualFileSystemException, CredentialStoreException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        return heroku.run(appName,
                          projectId != null ? new java.io.File(localPathResolver.resolve(vfs, projectId)) : null,
                          command
                         );
    }

}
