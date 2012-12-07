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
package org.exoplatform.ide.texteditor.api.quickassist;

import org.exoplatform.ide.text.annotation.Annotation;
import org.exoplatform.ide.texteditor.api.TextEditorPartDisplay;

/**
 * An <code>QuickAssistAssistant</code> provides support for quick fixes and quick
 * assists.
 * The quick assist assistant is a {@link TextEditorPartDisplay} add-on. Its
 * purpose is to propose, display, and insert quick assists and quick fixes
 * available at the current source viewer's quick assist invocation context.
 * <p>
 * The quick assist assistant can be configured with a {@link QuickAssistProcessor}
 * which provides the possible quick assist and quick fix completions.
 * </p>
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 *
 */
public interface QuickAssistAssistant
{
   /**
    * Installs quick assist support on the given source viewer.
    * @param sourceViewer the source viewer on which quick assist will work
    */
   void install(TextEditorPartDisplay textEditor);

   /**
    * Uninstalls quick assist support from the source viewer it has
    * previously be installed on.
    */
   void uninstall();

   /**
    * Shows all possible quick fixes and quick assists at the viewer's cursor position.
    *
    * @return an optional error message if no proposals can be computed
    */
   String showPossibleQuickAssists();

   /**
    * Registers a given quick assist processor for a particular content type. If there is already
    * a processor registered, the new processor is registered instead of the old one.
    *
    * @param processor the quick assist processor to register, or <code>null</code> to remove
    *        an existing one
    */
   void setQuickAssistProcessor(QuickAssistProcessor processor);

   /**
    * Returns the quick assist processor to be used for the given content type.
    *
    * @return the quick assist processor or <code>null</code> if none exists
    */
   QuickAssistProcessor getQuickAssistProcessor();

   /**
    * Tells whether this assistant has a fix for the given annotation.
    * <p>
    * <strong>Note:</strong> This test must be fast and optimistic i.e. it is OK to return
    * <code>true</code> even though there might be no quick fix.
    * </p>
    *
    * @param annotation the annotation
    * @return <code>true</code> if the assistant has a fix for the given annotation
    */
   boolean canFix(Annotation annotation);

   /**
    * Tells whether this assistant has assists for the given invocation context.
    *
    * @param invocationContext the invocation context
    * @return <code>true</code> if the assistant has a fix for the given annotation
    */
   boolean canAssist(QuickAssistInvocationContext invocationContext);
}