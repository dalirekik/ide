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
package org.exoplatform.ide.git.server.rest;

import org.exoplatform.gwtframework.commons.rest.HTTPHeader;
import org.exoplatform.gwtframework.commons.rest.HTTPStatus;
import org.exoplatform.ide.git.server.GitException;
import org.exoplatform.ide.git.server.NotAuthorizedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception mapper for GitException.
 * 
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
@Provider
public class GitExceptionMapper implements ExceptionMapper<GitException> {
    /** @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable) */
    @Override
    public Response toResponse(GitException e) {
        if (e instanceof NotAuthorizedException) {
            return Response.status(HTTPStatus.UNAUTHORIZED)
                           .header(HTTPHeader.JAXRS_BODY_PROVIDED, "Authentication-required")
                           .entity("You need to authorize to perform this operation")
                           .type(MediaType.TEXT_PLAIN)
                           .build();
        }

        // Insert error message in <pre> tags even content-type is text/plain.
        // Message will be included in HTML page by client.
        return Response.status(HTTPStatus.INTERNAL_ERROR)
                       .header(HTTPHeader.JAXRS_BODY_PROVIDED, "Error-Message")
                       .entity(e.getLocalizedMessage())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
