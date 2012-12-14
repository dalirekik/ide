/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.extension.cloudbees.server.rest;

import com.cloudbees.api.BeesClientException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Provider
public class BeesClientExceptionMapper implements ExceptionMapper<BeesClientException>
{
   /**
    * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
    */
   @Override
   public Response toResponse(BeesClientException bce)
   {
      // TODO Get correct HTTP status from BeesClientException ??? 
      return Response.status(500).header("JAXRS-Body-Provided", "Error-Message").entity(bce.getMessage())
         .type(MediaType.TEXT_PLAIN).build();
   }
}