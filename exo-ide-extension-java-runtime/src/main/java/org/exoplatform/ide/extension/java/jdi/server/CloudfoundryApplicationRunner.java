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
package org.exoplatform.ide.extension.java.jdi.server;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ide.commons.NameGenerator;
import org.exoplatform.ide.extension.cloudfoundry.server.Cloudfoundry;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryAuthenticator;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryCredentials;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryException;
import org.exoplatform.ide.extension.cloudfoundry.server.DebugMode;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudfoundryApplicationStatistics;
import org.exoplatform.ide.extension.cloudfoundry.shared.Instance;
import org.exoplatform.ide.extension.java.jdi.server.model.ApplicationInstanceImpl;
import org.exoplatform.ide.extension.java.jdi.server.model.DebugApplicationInstanceImpl;
import org.exoplatform.ide.extension.java.jdi.shared.ApplicationInstance;
import org.exoplatform.ide.extension.java.jdi.shared.DebugApplicationInstance;
import org.exoplatform.ide.helper.ParsingResponseException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.exoplatform.ide.commons.ContainerUtils.readValueParam;
import static org.exoplatform.ide.commons.FileUtils.*;
import static org.exoplatform.ide.commons.NameGenerator.generate;
import static org.exoplatform.ide.commons.ZipUtils.listEntries;
import static org.exoplatform.ide.commons.ZipUtils.unzip;

/**
 * ApplicationRunner for deploy Java applications at Cloud Foundry PaaS.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class CloudfoundryApplicationRunner implements ApplicationRunner, Startable
{
   /** Default application lifetime (in minutes). After this time application may be stopped automatically. */
   public static final int DEFAULT_APPLICATION_LIFETIME = 10;

   private static final Log LOG = ExoLogger.getLogger(CloudfoundryApplicationRunner.class);

   private final int applicationLifetime;
   private final long applicationLifetimeMillis;

   private final Cloudfoundry cloudfoundry;
   private final List<Application> applications;
   private final ScheduledExecutorService applicationTerminator;
   private final String cfUser;
   private final String cfPassword;
   private final java.io.File appEngineSdk;

   public CloudfoundryApplicationRunner(InitParams initParams)
   {
      this(
         readValueParam(initParams, "cloudfoundry-target"),
         readValueParam(initParams, "cloudfoundry-user"),
         readValueParam(initParams, "cloudfoundry-password"),
         parseNumber(readValueParam(initParams, "cloudfoundry-application-lifetime"),
            DEFAULT_APPLICATION_LIFETIME).intValue()
      );
   }

   protected CloudfoundryApplicationRunner(String cfTarget, String cfUser, String cfPassword, int applicationLifetime)
   {
      if (cfTarget == null || cfTarget.isEmpty())
      {
         throw new IllegalArgumentException("Cloud Foundry target URL may not be null or empty.");
      }
      if (cfUser == null || cfUser.isEmpty())
      {
         throw new IllegalArgumentException("Cloud Foundry username may not be null or empty.");
      }
      if (cfPassword == null || cfPassword.isEmpty())
      {
         throw new IllegalArgumentException("Cloud Foundry password may not be null or empty.");
      }
      if (applicationLifetime < 1)
      {
         throw new IllegalArgumentException("Invalid application lifetime: " + 1);
      }

      this.cfUser = cfUser;
      this.cfPassword = cfPassword;

      this.applicationLifetime = applicationLifetime;
      this.applicationLifetimeMillis = applicationLifetime * 60 * 1000;

      this.cloudfoundry = new Cloudfoundry(new Auth(cfTarget));
      this.applications = new CopyOnWriteArrayList<Application>();
      this.applicationTerminator = Executors.newSingleThreadScheduledExecutor();
      this.applicationTerminator.scheduleAtFixedRate(new TerminateApplicationTask(), 1, 1, TimeUnit.MINUTES);

      java.io.File lib = null;
      try
      {
         Class cl = Thread.currentThread().getContextClassLoader()
            .loadClass("com.google.appengine.tools.development.DevAppServerMain");
         URL cs = cl.getProtectionDomain().getCodeSource().getLocation();
         lib = new java.io.File(URI.create(cs.toString()));
         while (!(lib == null || "lib".equals(lib.getName())))
         {
            lib = lib.getParentFile();
         }
      }
      catch (ClassNotFoundException ignored)
      {
      }

      appEngineSdk = lib == null ? null : lib.getParentFile();
      if (appEngineSdk == null)
      {
         LOG.error("**********************************\n"
            + "* Google appengine Java SDK not found *\n"
            + "**********************************");
      }
   }

   private static Double parseNumber(String str, double defaultValue)
   {
      if (str != null)
      {
         try
         {
            return Double.parseDouble(str);
         }
         catch (NumberFormatException ignored)
         {
         }
      }
      return defaultValue;
   }

   @Override
   public ApplicationInstance runApplication(URL war) throws ApplicationRunnerException
   {
      try
      {
         return doRunApplication(war);
      }
      catch (ApplicationRunnerException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof CloudfoundryException)
         {
            if (200 == ((CloudfoundryException)cause).getExitCode())
            {
               login();
               return doRunApplication(war);
            }
         }
         throw e;
      }
   }

   private ApplicationInstance doRunApplication(URL url) throws ApplicationRunnerException
   {
      java.io.File path = null;
      try
      {
         path = downloadFile(null, "app-", ".war", url);
         final String target = cloudfoundry.getTarget();
         CloudFoundryApplication cfApp = createApplication(target, path, null);
         final String name = cfApp.getName();
         final int port = getPort(name, target);
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;
         applications.add(new Application(name, expired));
         LOG.debug("Start application {}.", name);
         ApplicationInstance appInst = new ApplicationInstanceImpl(name, cfApp.getUris().get(0), null, applicationLifetime);
         if (port > 0)
         {
            appInst.setPort(port);
         }
         return appInst;
      }
      catch (CloudfoundryException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (ParsingResponseException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (VirtualFileSystemException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (IOException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      finally
      {
         if (path != null)
         {
            deleteRecursive(path);
         }
      }
   }

   @Override
   public DebugApplicationInstance debugApplication(URL war, boolean suspend) throws ApplicationRunnerException
   {
      try
      {
         return doDebugApplication(war, suspend);
      }
      catch (ApplicationRunnerException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof CloudfoundryException)
         {
            if (200 == ((CloudfoundryException)cause).getExitCode())
            {
               login();
               return doDebugApplication(war, suspend);
            }
         }
         throw e;
      }
   }

   private DebugApplicationInstance doDebugApplication(URL url, boolean suspend) throws ApplicationRunnerException
   {
      java.io.File path = null;
      try
      {
         path = downloadFile(null, "app-", ".war", url);
         final String target = cloudfoundry.getTarget();
         CloudFoundryApplication cfApp = createApplication(target, path, suspend ? new DebugMode("suspend") : new DebugMode());
         final String name = cfApp.getName();
         Instance[] instances = cloudfoundry.applicationInstances(target, name, null, null);
         if (instances.length != 1)
         {
            throw new ApplicationRunnerException("Unable run application in debug mode. ");
         }
         final int port = getPort(name, target);
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;
         applications.add(new Application(name, expired));
         LOG.debug("Start application {} under debug.", name);
         DebugApplicationInstanceImpl dAppInst = new DebugApplicationInstanceImpl(name, cfApp.getUris().get(0), null,
            applicationLifetime, instances[0].getDebugHost(), instances[0].getDebugPort());
         if (port > 0)
         {
            dAppInst.setPort(port);
         }
         return dAppInst;
      }
      catch (CloudfoundryException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (ParsingResponseException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (VirtualFileSystemException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (IOException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      finally
      {
         if (path != null)
         {
            deleteRecursive(path);
         }
      }
   }

   private int getPort(String name, String target) throws CloudfoundryException, ParsingResponseException, IOException,
      VirtualFileSystemException
   {
      CloudfoundryApplicationStatistics stats = cloudfoundry.applicationStats(target, name, null, null).get("0");
      if (stats != null)
      {
         return stats.getPort();
      }
      return -1;
   }

   @Override
   public void stopApplication(String name) throws ApplicationRunnerException
   {
      try
      {
         doStopApplication(name);
      }
      catch (ApplicationRunnerException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof CloudfoundryException)
         {
            if (200 == ((CloudfoundryException)cause).getExitCode())
            {
               login();
               doStopApplication(name);
            }
         }
         throw e;
      }
   }

   private void doStopApplication(String name) throws ApplicationRunnerException
   {
      try
      {
         String target = cloudfoundry.getTarget();
         cloudfoundry.stopApplication(target, name, null, null);
         cloudfoundry.deleteApplication(target, name, null, null, true);
         Application app = new Application(name, 0);
         applications.remove(app);
         LOG.debug("Stop application {}.", name);
      }
      catch (CloudfoundryException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (ParsingResponseException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (VirtualFileSystemException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (IOException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   @Override
   public void start()
   {
   }

   @Override
   public void stop()
   {
      applicationTerminator.shutdownNow();
      for (Application app : applications)
      {
         try
         {
            stopApplication(app.name);
         }
         catch (ApplicationRunnerException e)
         {
            LOG.error("Failed to stop application {}.", app.name, e);
         }
      }
      applications.clear();
   }

   private CloudFoundryApplication createApplication(String target, java.io.File path, DebugMode debug)
      throws CloudfoundryException, IOException, ParsingResponseException, VirtualFileSystemException
   {
      final String framework;
      final String command;
      if (APPLICATION_TYPE.JAVA_WEB_APP_ENGINE == determineApplicationType(path))
      {
         // Need to do some additional job to be able run google appengine application with SDK.
         path = prepareAppEngineApplication(path);
         framework = "standalone";
         command = "java -ea -cp appengine-java-sdk/lib/appengine-tools-api.jar"
            + " -javaagent:appengine-java-sdk/lib/agent/appengine-agent.jar"
            + " $JAVA_OPTS"
            + " com.google.appengine.tools.development.DevAppServerMain"
            + " --port=$VCAP_APP_PORT"
            + " --address=0.0.0.0"
            + " --disable_update_check"
            + " application";
      }
      else
      {
         framework = "spring"; // send 'spring' even fot 'regular' web applications
         command = null;
      }
      return cloudfoundry.createApplication(target, generate("app-", 16), framework, null, 1, 256, false, "java",
         command, debug, null, null, path.toURI().toURL());
   }

   private enum APPLICATION_TYPE
   {
      JAVA_WEB,
      JAVA_WEB_APP_ENGINE
   }

   private APPLICATION_TYPE determineApplicationType(java.io.File war) throws IOException
   {
      for (String f : listEntries(war))
      {
         if (f.endsWith("WEB-INF/appengine-web.xml"))
         {
            return APPLICATION_TYPE.JAVA_WEB_APP_ENGINE;
         }
      }
      return APPLICATION_TYPE.JAVA_WEB;
   }

   private java.io.File prepareAppEngineApplication(java.io.File war) throws IOException
   {
      if (appEngineSdk == null)
      {
         throw new RuntimeException("Unable run or debug appengine project. Google appengine Java SDK not found. ");
      }
      java.io.File root = createTempDirectory(null, "gae-app-");

      // copy sdk
      java.io.File sdk = new java.io.File(root, "appengine-java-sdk");
      if (!sdk.mkdir())
      {
         throw new IOException("Unable create directory " + sdk.getAbsolutePath());
      }
      copy(appEngineSdk, sdk, null);

      // unzip content of war file
      java.io.File application = new java.io.File(root, "application");
      if (!application.mkdir())
      {
         throw new IOException("Unable create directory " + application.getAbsolutePath());
      }
      unzip(war, application);

      war.delete(); // Delete war file. Don't need it any more.
      return root;
   }

   private void login() throws ApplicationRunnerException
   {
      try
      {
         cloudfoundry.login(cloudfoundry.getTarget(), cfUser, cfPassword);
      }
      catch (CloudfoundryException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (ParsingResponseException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (VirtualFileSystemException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (IOException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   private class TerminateApplicationTask implements Runnable
   {
      @Override
      public void run()
      {
         List<Application> stopped = new ArrayList<Application>();
         for (Application app : applications)
         {
            if (app.isExpired())
            {
               try
               {
                  stopApplication(app.name);
               }
               catch (ApplicationRunnerException e)
               {
                  LOG.error("Failed to stop application {}.", app.name, e);
               }
               // Do not try to stop application twice.
               stopped.add(app);
            }
         }
         applications.removeAll(stopped);
         LOG.debug("{} APPLICATION REMOVED", stopped.size());
      }
   }

   private static class Auth extends CloudfoundryAuthenticator
   {
      private final String cfTarget;

      private CloudfoundryCredentials credentials;

      public Auth(String cfTarget)
      {
         // We do not use stored cloud foundry credentials.
         // Not need VFS, configuration, etc.
         super(null, null);
         this.cfTarget = cfTarget;
         credentials = new CloudfoundryCredentials();
         //credentials.addToken(this.cfTarget, "");
      }

      @Override
      public String readTarget() throws VirtualFileSystemException, IOException
      {
         return cfTarget;
      }

      @Override
      public CloudfoundryCredentials readCredentials() throws VirtualFileSystemException, IOException
      {
         return credentials;
      }

      @Override
      public void writeTarget(String target) throws VirtualFileSystemException, IOException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeCredentials(CloudfoundryCredentials credentials) throws VirtualFileSystemException, IOException
      {
         this.credentials = new CloudfoundryCredentials();
         this.credentials.addToken(cfTarget, credentials.getToken(cfTarget));
      }
   }

   private static class Application
   {
      final String name;
      final long expirationTime;
      final int hash;

      Application(String name, long expirationTime)
      {
         this.name = name;
         this.expirationTime = expirationTime;
         this.hash = 31 * 7 + name.hashCode();
      }

      boolean isExpired()
      {
         return expirationTime < System.currentTimeMillis();
      }

      @Override
      public boolean equals(Object o)
      {
         return o instanceof Application && name.equals(((Application)o).name);
      }

      @Override
      public int hashCode()
      {
         return hash;
      }
   }
}