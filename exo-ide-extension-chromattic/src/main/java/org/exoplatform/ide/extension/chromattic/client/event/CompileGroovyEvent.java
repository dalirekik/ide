/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.ide.extension.chromattic.client.event;

import org.exoplatform.ide.vfs.shared.File;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class CompileGroovyEvent extends GwtEvent<CompileGroovyHandler>
{

   public static final GwtEvent.Type<CompileGroovyHandler> TYPE = new GwtEvent.Type<CompileGroovyHandler>();

   private File file;

   public CompileGroovyEvent(File file)
   {
      this.file = file;
   }

   public File getFile()
   {
      return file;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<CompileGroovyHandler> getAssociatedType()
   {
      return TYPE;
   }

   @Override
   protected void dispatch(CompileGroovyHandler handler)
   {
      handler.onCompileGroovy(this);
   }

}