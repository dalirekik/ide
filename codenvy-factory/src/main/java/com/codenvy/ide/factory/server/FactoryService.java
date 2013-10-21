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
package com.codenvy.ide.factory.server;

import com.codenvy.commons.security.oauth.OAuthTokenProvider;
import com.codenvy.ide.commons.shared.ProjectType;

import org.apache.commons.io.IOUtils;
import org.codenvy.mail.MailSenderClient;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.exoplatform.ide.git.server.GitConnection;
import org.exoplatform.ide.git.server.GitConnectionFactory;
import org.exoplatform.ide.git.server.GitException;
import org.exoplatform.ide.git.shared.Branch;
import org.exoplatform.ide.git.shared.BranchListRequest;
import org.exoplatform.ide.git.shared.BranchCheckoutRequest;
import org.exoplatform.ide.git.shared.CloneRequest;
import org.exoplatform.ide.git.shared.GitUser;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.server.LocalPathResolver;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.ItemNotFoundException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.shared.File;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemType;
import org.exoplatform.ide.vfs.shared.Property;
import org.exoplatform.ide.vfs.shared.PropertyFilter;
import org.exoplatform.ide.vfs.shared.PropertyImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for sharing Factory URL by e-mail messages.
 *
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: FactoryService.java Jun 25, 2013 10:50:00 PM azatsarynnyy $
 */
@Path("{ws-name}/factory")
public class FactoryService {
    private static final Log LOG = ExoLogger.getLogger(FactoryService.class);

    private final MailSenderClient          mailSenderClient;
    private final GitConnectionFactory      factory;
    private       VirtualFileSystemRegistry vfsRegistry;
    private       LocalPathResolver         localPathResolver;

    @Inject
    private OAuthTokenProvider oauthTokenProvider;

    private static final Pattern PATTERN        = Pattern.compile("public static final String PROJECT_ID = .*");
    private static final Pattern PATTERN_NUMBER = Pattern.compile("public static final String PROJECT_NUMBER = .*");

    /**
     * Constructs a new {@link FactoryService}.
     *
     * @param mailSenderClient
     *         client for sending messages over e-mail
     */
    public FactoryService(MailSenderClient mailSenderClient, VirtualFileSystemRegistry vfsRegistry,
                          LocalPathResolver localPathResolver, GitConnectionFactory factory) {
        this.mailSenderClient = mailSenderClient;
        this.vfsRegistry = vfsRegistry;
        this.localPathResolver = localPathResolver;
        this.factory = factory;
    }

    /**
     * Sends e-mail message to share Factory URL.
     *
     * @param recipient
     *         address to share Factory URL
     * @param message
     *         text message that includes Factory URL
     * @return the Response with the corresponded status
     */
    @POST
    @Path("share")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response share(@FormParam("recipient") String recipient, //
                          @FormParam("message") String message) {
        final String sender = "Codenvy <noreply@codenvy.com>";
        final String subject = "Check out my Codenvy project";
        final String mimeType = "text/plain; charset=utf-8";
        try {
            mailSenderClient.sendMail(sender, recipient, null, subject, mimeType, URLDecoder.decode(message, "UTF-8"));
            return Response.ok().build();
        } catch (MessagingException | IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("clone")
    @Produces(MediaType.APPLICATION_JSON)
    public Item cloneProject(@QueryParam("vfsid") String vfsId,
                             @QueryParam("projectid") String projectId,
                             @QueryParam("remoteuri") String remoteUri,
                             @QueryParam("idcommit") String idCommit,
                             @QueryParam("ptype") String projectType,
                             @QueryParam("action") String action,
                             @QueryParam("keepvcsinfo") boolean keepVcsInfo,
                             @QueryParam("gitbranch") String gitBranch)
            throws VirtualFileSystemException, GitException,
                   URISyntaxException, IOException {
        GitConnection gitConnection = getGitConnection(projectId, vfsId);

        try {
            gitConnection.clone(new CloneRequest(remoteUri, null));

            if (idCommit != null && !idCommit.trim().isEmpty()) {
                //Try to checkout to new branch "temp" with HEAD of setted commit ID
                gitConnection.branchCheckout(new BranchCheckoutRequest("temp", idCommit, true));
            } else if (gitBranch != null && !gitBranch.trim().isEmpty()) {
                //Try to checkout to specified branch. For first we need to list all cloned local branches to
                //find if specified branch already exist, if its true, we check if this this branch is active
                List<Branch> branches = gitConnection.branchList(new BranchListRequest(null));
                for (Branch branch : branches) {
                    if (branch.getDisplayName().equals(gitBranch)) {
                        gitConnection
                                .branchCheckout(new BranchCheckoutRequest(gitBranch, "origin/" + gitBranch, false));
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            //Case: branch doesn't exist, or user try to pass into param idcommit something unlike hash of
            //commit then JGit will thrown GitAPIException which will be transformed into IllegalArgumentException in
            //org.exoplatform.ide.git.server.jgit.JGitConnection.branchCheckout()
            if (e.getMessage().matches("Ref .* can not be resolved")) {
                //And there is two cases, when user pass into commit ID parameter some strings unlike hash and when user
                //pass into vcsbranch parameter non existed branch.
                if (idCommit != null && !idCommit.trim().isEmpty()) {
                    publishWebsocketMessage(
                            "Commit <b>" + idCommit + "</b> doesn't exist. Switching to default branch.");
                } else if (gitBranch != null && !gitBranch.trim().isEmpty()) {
                    publishWebsocketMessage(
                            "Branch <b>" + gitBranch + "</b> doesn't exist. Switching to default branch.");
                }
            } else {
                //In other case we simple rethrown exception to client. It maybe "fatal: A branch named 'branchName'
                // already exists."
                //from org.exoplatform.ide.git.server.jgit.JGitConnection.branchCheckout()
                deleteRepository(vfsId, projectId);
                throw new IllegalArgumentException(e);
            }
        } catch (RuntimeException e) {
            //Case: commit ID doesn't exist, if it happens JGit throw exception with message "Missing unknown #hash"
            //We try to publish via websocket message to user that this commit doesn't exist and continue parsing
            //our source directory into project with default cloned branch
            if (e.getMessage().contains("Missing unknown")) {
                publishWebsocketMessage("Commit <b>" + idCommit + "</b> doesn't exist. Switching to default branch.");
            } else {
                //In other case we simple rethrown exception to client. It maybe "not authorized" exception from
                //OAuthCredentialsProvider
                deleteRepository(vfsId, projectId);
                throw new GitException(e);
            }
        } finally {
            //Finally if we found parameter vcsinfo we check that we should delete git repository after cloning.
            if (!keepVcsInfo) {
                try {
                    deleteRepository(vfsId, projectId);
                } catch (VirtualFileSystemException e) {
                    //ignore, folder already deleted
                }
            }
        }

        return convertToProject(vfsId, projectId, remoteUri, projectType, action, keepVcsInfo);
    }

    /**
     * Send message to socket to allow client make output to console.
     *
     * @param content
     *         message content
     */
    private void publishWebsocketMessage(String content) {
        ChannelBroadcastMessage message = new ChannelBroadcastMessage();
        message.setChannel("factory-events");
        message.setType(ChannelBroadcastMessage.Type.NONE);
        message.setBody(content);

        try {
            WSConnectionContext.sendMessage(message);
        } catch (Exception ex) {
            LOG.error("Failed to send message over WebSocket.", ex);
        }
    }

    private Item convertToProject(String vfsId, String projectId, String remoteUri, String projectType, String action,
                                  boolean keepVcsInfo)
            throws VirtualFileSystemException, IOException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        Item itemToUpdate = vfs.getItem(projectId, false, PropertyFilter.ALL_FILTER);
        try {
            Item item = vfs.getItemByPath(itemToUpdate.getPath() + "/.project", null, false, null);
            vfs.delete(item.getId(), null);
        } catch (ItemNotFoundException ignore) {
            // ignore
        }
        if (projectType != null && !projectType.isEmpty()) {
            List<Property> props = new ArrayList<Property>();
            props.addAll(itemToUpdate.getProperties());
            props.add(new PropertyImpl("vfs:mimeType", ProjectModel.PROJECT_MIME_TYPE));
            props.add(new PropertyImpl("vfs:projectType", projectType));
            props.add(new PropertyImpl("codenow", remoteUri));
            if (keepVcsInfo)
                props.add(new PropertyImpl("isGitRepository", "true"));
            itemToUpdate = vfs.updateItem(itemToUpdate.getId(), props, null);
            if (ProjectType.GOOGLE_MBS_ANDROID.toString().equals(projectType)) {
                File constJava = (File)vfs
                        .getItemByPath(itemToUpdate.getPath() + "/src/com/google/cloud/backend/android/Consts.java",
                                       null, false,
                                       PropertyFilter.NONE_FILTER);
                String content = IOUtils.toString(vfs.getContent(constJava.getId()).getStream());

                String[] actionParams = action.replaceAll("'", "").split(";");
                String prjNum = null;
                String prjID = null;

                for (String param : actionParams) {
                    if (param.startsWith("projectNumber")) {
                        prjNum = param.split("=")[1];
                    }
                    if (param.startsWith("projectID")) {
                        prjID = param.split("=")[1];
                    }
                }

                String newContent = PATTERN.matcher(content)
                                           .replaceFirst("public static final String PROJECT_ID = \"" + prjID + "\";");
                newContent =
                        PATTERN_NUMBER.matcher(newContent)
                                      .replaceFirst("public static final String PROJECT_NUMBER = \"" + prjNum + "\";");
                vfs.updateContent(constJava.getId(), MediaType.valueOf(constJava.getMimeType()),
                                  new ByteArrayInputStream(newContent.getBytes()), null);
            }
        }
        return itemToUpdate;
    }

    protected void deleteRepository(String vfsId, String projectId) throws VirtualFileSystemException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        Item project = getGitProject(vfs, projectId);
        String path2gitFolder = project.getPath() + "/.git";
        Item gitItem = vfs.getItemByPath(path2gitFolder, null, false, PropertyFilter.NONE_FILTER);
        vfs.delete(gitItem.getId(), null);
    }

    protected GitConnection getGitConnection(String projectId, String vfsId)
            throws GitException, VirtualFileSystemException {
        GitUser gituser = null;
        ConversationState user = ConversationState.getCurrent();
        if (user != null) {
            gituser = new GitUser(user.getIdentity().getUserId());
        }
        return factory.getConnection(resolveLocalPath(projectId, vfsId), gituser);
    }

    protected String resolveLocalPath(String projectId, String vfsId) throws VirtualFileSystemException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        if (vfs == null) {
            throw new VirtualFileSystemException(
                    "Can't resolve path on the Local File System : Virtual file system not initialized");
        }
        Item gitProject = getGitProject(vfs, projectId);
        return localPathResolver.resolve(vfs, gitProject.getId());
    }

    private Item getGitProject(VirtualFileSystem vfs, String projectId) throws VirtualFileSystemException {
        Item project = vfs.getItem(projectId, false, PropertyFilter.ALL_FILTER);
        Item parent = vfs.getItem(project.getParentId(), false, PropertyFilter.ALL_FILTER);
        if (parent.getItemType().equals(ItemType.PROJECT)) // MultiModule project
            return parent;
        return project;
    }
}
