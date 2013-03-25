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

package org.exoplatform.ide.client.operation.autocompletion;

import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.editor.client.api.Editor;
import org.exoplatform.ide.editor.api.codeassitant.RunCodeAssistantEvent;
import org.exoplatform.ide.editor.codemirror.CodeMirror;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class AutocompletionHandler implements AutocompleteCalledHandler, EditorActiveFileChangedHandler
{

   private Editor activeEditor;

   public AutocompletionHandler()
   {
//      IDE.getInstance().addControl(new OpenAutocompleteControl());

      IDE.addHandler(AutocompleteCalledEvent.TYPE, this);
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
   }

   @Override
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeEditor = event.getEditor();
   }

   @Override
   public void onAutocompleteCalled(AutocompleteCalledEvent event)
   {
      if (activeEditor != null && activeEditor instanceof CodeMirror)
      {
         CodeMirror codemirror = (CodeMirror)activeEditor;
         codemirror.onAutocomplete();
      }
      else
      {
         IDE.fireEvent(new RunCodeAssistantEvent());
      }
   }

}