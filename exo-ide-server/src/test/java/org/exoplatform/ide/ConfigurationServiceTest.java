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
package org.exoplatform.ide;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.provider.json.JsonGenerator;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.JsonWriter;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.test.mock.MockPrincipal;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.services.security.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.codenvy.ide.commons.IdeUser;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: May 24, 2011 evgen $
 */
public class ConfigurationServiceTest extends BaseTest {
    private SecurityContext securityContext;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Authenticator authr = (Authenticator)container.getComponentInstanceOfType(Authenticator.class);
        String validUser =
                authr.validateUser(new Credential[]{new UsernameCredential("root"), new PasswordCredential("exo")});
        Identity id = authr.createIdentity(validUser);
        Set<String> roles = new HashSet<String>();
        roles.add("users");
        roles.add("administrators");
        id.setRoles(roles);
        ConversationState s = new ConversationState(id);
        ConversationState.setCurrent(s);
    }

    @Test
    public void appConfiguration() throws Exception {
        Set<String> userRoles = new HashSet<String>();
        userRoles.add("users");
        securityContext = new SimpleSecurityContext(new MockPrincipal("root"), userRoles, "BASIC", false);
        EnvironmentContext ctx = new EnvironmentContext();
        ctx.put(SecurityContext.class, securityContext);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        ContainerResponse cres = launcher.service("GET", "/ide/configuration/init", "", headers, null, null, ctx);
        System.err.println(cres.getEntity());
        Assert.assertEquals(200, cres.getStatus());

        Assert.assertNotNull(cres.getEntity());
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>)cres.getEntity();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonValue jv = JsonGenerator.createJsonObject(entity);
        JsonWriter jsonWriter = new JsonWriter(out);
        jv.writeTo(jsonWriter);
        jsonWriter.flush();
        jsonWriter.close();

        Assert.assertTrue(entity.containsKey("defaultEntrypoint"));
        Assert.assertTrue(entity.containsKey("user"));
        Assert.assertTrue(entity.containsKey("discoverable"));
        Assert.assertTrue(entity.containsKey("userSettings"));
        Assert.assertTrue(entity.containsKey("vfsId"));
        Assert.assertTrue(entity.containsKey("vfsBaseUrl"));
    }

    @Test
    public void whoami() throws Exception {
        Set<String> userRoles = new HashSet<String>();
        userRoles.add("users");
        securityContext = new SimpleSecurityContext(new MockPrincipal("root"), userRoles, "BASIC", false);
        EnvironmentContext ctx = new EnvironmentContext();
        ctx.put(SecurityContext.class, securityContext);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        ContainerResponse cres = launcher.service("GET", "/ide/configuration/init", "", headers, null, null, ctx);
        Assert.assertEquals(200, cres.getStatus());
        Assert.assertNotNull(cres.getEntity());
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>)cres.getEntity();
        Assert.assertTrue(entity.containsKey("user"));
        Assert.assertTrue(entity.get("user") instanceof IdeUser);
        IdeUser user = (IdeUser)entity.get("user");
        Assert.assertEquals("root", user.getUserId());
        Assert.assertTrue(user.getRoles().contains("users"));
        Assert.assertTrue(user.getRoles().contains("administrators"));
        Assert.assertEquals(2, user.getRoles().size());
    }

    @Test
    public void entryPoint() throws Exception {
        Set<String> userRoles = new HashSet<String>();
        userRoles.add("users");
        securityContext = new SimpleSecurityContext(new MockPrincipal("root"), userRoles, "BASIC", false);
        EnvironmentContext ctx = new EnvironmentContext();
        ctx.put(SecurityContext.class, securityContext);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        ContainerResponse cres = launcher.service("GET", "/ide/configuration/init", "", headers, null, null, ctx);
        Assert.assertEquals(200, cres.getStatus());

        Assert.assertNotNull(cres.getEntity());
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>)cres.getEntity();
        Assert.assertTrue(entity.containsKey("defaultEntrypoint"));
        Assert.assertTrue(entity.containsKey("discoverable"));
    }

    @Test
    public void setConfiguration() throws Exception {
        Set<String> userRoles = new HashSet<String>();
        userRoles.add("users");
        securityContext = new SimpleSecurityContext(new MockPrincipal("root"), userRoles, "BASIC", false);
        EnvironmentContext ctx = new EnvironmentContext();
        ctx.put(SecurityContext.class, securityContext);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("userSettings.js");
        ContainerResponse cres =
                launcher.service("PUT", "/ide/configuration", "", headers, IOUtil.getStreamContentAsBytes(stream), null, ctx);
        Assert.assertEquals(204, cres.getStatus());
    }

    public void getConfiguration() throws Exception {
        Set<String> userRoles = new HashSet<String>();
        userRoles.add("users");
        securityContext = new SimpleSecurityContext(new MockPrincipal("root"), userRoles, "BASIC", false);
        EnvironmentContext ctx = new EnvironmentContext();
        ctx.put(SecurityContext.class, securityContext);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        ContainerResponse cres = launcher.service("GET", "/ide/configuration", "", headers, null, null, ctx);
        Assert.assertEquals(200, cres.getStatus());

        Assert.assertNotNull(cres.getEntity());
    }

    @Test
    public void userConfiguration() throws Exception {

        Set<String> userRoles = new HashSet<String>();
        userRoles.add("users");
        securityContext = new SimpleSecurityContext(new MockPrincipal("root"), userRoles, "BASIC", false);
        EnvironmentContext ctx = new EnvironmentContext();
        ctx.put(SecurityContext.class, securityContext);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("userSettings.js");
        ContainerResponse cres =
                launcher.service("PUT", "/ide/configuration", "", headers, IOUtil.getStreamContentAsBytes(stream), null, ctx);
        Assert.assertEquals(204, cres.getStatus());

        cres = launcher.service("GET", "/ide/configuration/init", "", headers, null, null, ctx);
        Assert.assertEquals(200, cres.getStatus());
        Assert.assertNotNull(cres.getEntity());
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>)cres.getEntity();
        Assert.assertNotNull(entity.get("userSettings"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getExistingUserSettings() throws Exception {
        Set<String> userRoles = new HashSet<String>();
        userRoles.add("users");
        securityContext = new SimpleSecurityContext(new MockPrincipal("root"), userRoles, "BASIC", false);
        EnvironmentContext ctx = new EnvironmentContext();
        ctx.put(SecurityContext.class, securityContext);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();

        ContainerResponse cres = launcher.service("GET", "/ide/configuration/init", "", headers, null, null, ctx);
        Assert.assertEquals(200, cres.getStatus());
        Assert.assertNotNull(cres.getEntity());
        Map<String, Object> entity = (Map<String, Object>)cres.getEntity();
        Assert.assertNotNull(entity.get("userSettings"));
        Map<String, Object> userSettingsMap = (Map<String, Object>)entity.get("userSettings");
        Assert.assertNotNull(userSettingsMap.get("hotkeys"));
        Assert.assertNotNull(userSettingsMap.get("toolbar-items"));
    }
}
