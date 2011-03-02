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
package org.exoplatform.ide.editor.api;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ide.editor.api.codeassitant.Token;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @author <a href="mailto:dmitry.nochevnov@exoplatform.com">Dmytro Nochevnov</a> 
 * @version $Id: CodeValidator Feb 9, 2011 5:48:28 PM $
 *
 */
public abstract class CodeValidator
{
   /**
    * Get list of code errors in the tokenList
    * @param tokenList
    * @return
    */
   public abstract List<CodeLine> getCodeErrorList(List<? extends Token> tokenList);
   
   /**
    * Get import statement "import <fqn>;" in the appropriate place of file
    * @param tokenList
    * @param fqn
    */
   public abstract CodeLine getImportStatement(List<? extends Token> tokenList, String fqn);
     
   /**
    * Returns list of code error in line with lineNumber
    * @param lineNumber
    * @return
    */
   public static List<CodeLine> getCodeErrorList(int lineNumber, List<CodeLine> codeErrors)
   {
      List<CodeLine> lineCodeErrorList = new ArrayList<CodeLine>();
      
      for (CodeLine codeError: codeErrors)
      {
         if (codeError.getLineNumber() == lineNumber)
         {            
            lineCodeErrorList.add(codeError);
         }
      }

      return lineCodeErrorList;
   }
   
   /**
    * Get text summary of registered errors from the lineCodeErrorList within the line 
    * @param lineCodeErrorList
    * @return text summary of errors within the line
    */
   public static String getErrorSummary(List<CodeLine> lineCodeErrorList)
   {
      String errorSummary = "";
      
      for (CodeLine codeError: lineCodeErrorList)
      {
         switch(codeError.getType()) {
            case TYPE_ERROR:
               errorSummary += "'" + codeError.getLineContent() + "' cannot be resolved to a type; ";
               break;
               
            default:
         }
      }
      
      return errorSummary;
   }
   
   /**
    * 
    * @param lineNumber
    * @return true if there is at list one code error in the line with lineNumber
    */
   public static boolean isExistedCodeError(int lineNumber, List<CodeLine> lineCodeErrorList)
   {
      for (CodeLine codeError: lineCodeErrorList)
      {
         if (codeError.getLineNumber() == lineNumber)
         {            
            return true;
         }
      }

      return false;
   }
}
