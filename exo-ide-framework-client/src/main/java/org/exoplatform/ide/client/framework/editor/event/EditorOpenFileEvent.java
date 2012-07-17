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
package org.exoplatform.ide.client.framework.editor.event;

import org.exoplatform.ide.client.framework.event.CursorPosition;
import org.exoplatform.ide.editor.api.Editor;
import org.exoplatform.ide.vfs.client.model.FileModel;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 */
public class EditorOpenFileEvent extends GwtEvent<EditorOpenFileHandler>
{

   public static final GwtEvent.Type<EditorOpenFileHandler> TYPE = new GwtEvent.Type<EditorOpenFileHandler>();

   private FileModel file;

   private Editor editor;
   
   private CursorPosition cursorPosition;

   public EditorOpenFileEvent(FileModel file, Editor editor)
   {
      this(file, editor, new CursorPosition(1,1));
   }
   
   public EditorOpenFileEvent(FileModel file, Editor editor, CursorPosition cursorPosition)
   {
      this.file = file;
      this.editor= editor;
      this.cursorPosition = cursorPosition;
   }

   public FileModel getFile()
   {
      return file;
   }

   /**
    * @return the editor
    */
   public Editor getEditor()
   {
      return editor;
   }

   public CursorPosition getCursorPosition()
   {
      return cursorPosition;
   }

   @Override
   protected void dispatch(EditorOpenFileHandler handler)
   {
      handler.onEditorOpenFile(this);
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<EditorOpenFileHandler> getAssociatedType()
   {
      return TYPE;
   }

}
