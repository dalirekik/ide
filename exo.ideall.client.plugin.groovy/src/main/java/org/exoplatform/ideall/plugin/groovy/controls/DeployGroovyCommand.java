/**
 * Copyright (C) 2009 eXo Platform SAS.
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
 *
 */
package org.exoplatform.ideall.client.groovy.command;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ideall.plugin.groovy.event.DeployGroovyScriptEvent;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class DeployGroovyCommand extends IDECommand implements EditorActiveFileChangedHandler
{

   private static final String ID = "Run/Deploy";

   public DeployGroovyCommand()
   {
      super(ID);
      setTitle("Deploy");
      setPrompt("Deploy REST Service");
      setImages(IDEImageBundle.INSTANCE.deployGroovy(), IDEImageBundle.INSTANCE.deployGroovyDisabled());
      setEvent(new DeployGroovyScriptEvent());
   }

   @Override
   protected void onRegisterHandlers()
   {
      addHandler(EditorActiveFileChangedEvent.TYPE, this);
   }

   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      if (event.getFile() == null)
      {
         setEnabled(false);
         setVisible(false);
         return;
      }

      setVisible(true);

      if (MimeType.SCRIPT_GROOVY.equals(event.getFile().getContentType())
         || MimeType.APPLICATION_GROOVY.equals(event.getFile().getContentType()))
      {
         setVisible(true);

         if (event.getFile().isNewFile())
         {
            setEnabled(false);
         }
         else
         {
            setEnabled(true);
         }
      }
      else
      {
         setVisible(false);
         setEnabled(false);
      }
   }

}
