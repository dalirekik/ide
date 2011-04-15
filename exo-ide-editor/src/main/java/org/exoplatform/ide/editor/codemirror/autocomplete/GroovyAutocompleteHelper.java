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
package org.exoplatform.ide.editor.codemirror.autocomplete;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.editor.api.codeassitant.Modifier;
import org.exoplatform.ide.editor.api.codeassitant.Token;
import org.exoplatform.ide.editor.api.codeassitant.TokenBeenImpl;
import org.exoplatform.ide.editor.api.codeassitant.TokenType;
import org.exoplatform.ide.editor.codevalidator.GroovyCodeValidator;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author <a href="mailto:dmitry.nochevnov@exoplatform.com">Dmytro Nochevnov</a>
 * @version $Id
 *
 */
public class GroovyAutocompleteHelper extends AutocompleteHelper
{
   public Token getTokenBeforeCursor(JavaScriptObject node, int lineNumber, int cursorPosition, List<? extends Token> tokenList, String currentLineMimeType)
   {
      return getTokenBeforeCursor(node, lineNumber, cursorPosition, tokenList);
   }
   
   /**
    * 
    * @param node
    * @param lineNumber 
    * @param cursorPosition
    * @param tokenList
    * @return
    */
   public Token getTokenBeforeCursor(JavaScriptObject node, int lineNumber, int cursorPosition, List<? extends Token> tokenList)
   {
      // interrupt at the end of the line or content
      if ((node == null) || (node).equals("BR"))
      {
         return null;
      }

      String nodeContent = getStatementBeforePoint(node, cursorPosition);

      TokenBeenImpl tokenBeforeCursor;
      
      if (nodeContent != null && !nodeContent.isEmpty())
      {       
         int numberOfChainsBetweenPoint = nodeContent.split("[.]").length;   // nodeContent.split("[.]") returns 1 for "name", and 3 for "java.lang.Integer"         

         // search token for variables like "name._" or "name.ch_"
         if (numberOfChainsBetweenPoint == 1)
         {
            tokenBeforeCursor = getGenericToken(nodeContent, lineNumber, (List<TokenBeenImpl>) tokenList);            
            if (tokenBeforeCursor != null) 
            {
               return new TokenBeenImpl(
                  tokenBeforeCursor.getName(), 
                  tokenBeforeCursor.getType(), 
                  lineNumber, 
                  tokenBeforeCursor.getMimeType(), 
                  tokenBeforeCursor.getElementType(),
                  tokenBeforeCursor.getModifiers(), 
                  tokenBeforeCursor.getFqn()
               );
            }
         }

         // search fqn among default packages
         String fqn = GroovyCodeValidator.getFqnFromDefaultPackages(nodeContent);
         if (fqn != null) 
            return new TokenBeenImpl(null, TokenType.TYPE, lineNumber, MimeType.APPLICATION_GROOVY, nodeContent, Arrays.asList(Modifier.STATIC), fqn);
         
         // search fqn among the import statements from the import block 
         List<TokenBeenImpl> importStatementBlock = GroovyCodeValidator.getImportStatementBlock((List<TokenBeenImpl>)tokenList);
         for (TokenBeenImpl importStatement : importStatementBlock)
         {
            if (importStatement.getElementType().endsWith(nodeContent))
            {
               return (Token) new TokenBeenImpl(null, TokenType.TYPE, lineNumber, MimeType.APPLICATION_GROOVY, nodeContent, Arrays.asList(Modifier.STATIC), importStatement.getElementType());
            }
         }           
         
      }
      
      // if this is "name_" or " _" cases, return Token of parent element, like method or class
      else
      {
         return (Token) getParentToken(lineNumber, (List<TokenBeenImpl>) tokenList);
      }
      
      return null;
   }
   
   protected static TokenBeenImpl getGenericToken(String nodeContent, int targetLineNumber, List<TokenBeenImpl> tokenList)
   {
      if (tokenList == null || tokenList.size() == 0)
         return null;

      nearestToken = tokenList.get(0);
      
      for (TokenBeenImpl token : tokenList)
      {
         if (isContainerTokenAfterTheCurrentLine(targetLineNumber, token.getLineNumber()))
            break;

         searchNearestToken(targetLineNumber, token);
      }
      
      TokenBeenImpl genericToken;
      
      if (nearestToken != null)
      {
         // test if nearest token is within the method
         if (nearestToken.getParentToken() != null
               && TokenType.METHOD.equals(nearestToken.getParentToken().getType()))
         {
            // search as local variables among the subTokens
            genericToken = searchGenericTokenAmongLocalVariables(nodeContent, nearestToken, nearestToken.getParentToken());
            if (genericToken != null) return genericToken;
   
            // search among the parameters of method
            genericToken = searchGenericTokenAmongParameters(nodeContent, nearestToken.getParentToken().getParameters());
            if (genericToken != null) return genericToken;
            
            // search among the properties (fields) of class
            genericToken = searchGenericTokenAmongProperties(nodeContent, nearestToken.getParentToken().getParentToken());
            if (genericToken != null) return genericToken;
         }
         
         // test if nearest token is method token 
         else if (TokenType.METHOD.equals(nearestToken.getType()))
         {
            // search among the parameters of method
            genericToken = searchGenericTokenAmongParameters(nodeContent, nearestToken.getParameters());
            if (genericToken != null) return genericToken;
            
            // search among the properties (fields) of class
            genericToken = searchGenericTokenAmongProperties(nodeContent, nearestToken.getParentToken());
            if (genericToken != null) return genericToken;
         }
         
         // trying to search generic token whitin the scriptlets of JSP, or Groovy Template files
         else 
         {
            // search among the properties (fields) of class
            genericToken = searchGenericTokenAmongProperties(nodeContent, nearestToken.getParentToken());
            if (genericToken != null) return genericToken;
         }
      }      
         
      return null;
   }   
   
   protected static TokenBeenImpl searchGenericTokenAmongParameters(String nodeContent, List<TokenBeenImpl> parameters)
   {
      if (parameters == null)
         return null;
      
      for (TokenBeenImpl parameter: parameters)
      {
         if (nodeContent.equals(parameter.getName()))
         {
            return parameter;
         }
      }

      return null;
   }   

   protected static TokenBeenImpl searchGenericTokenAmongProperties(String nodeContent, TokenBeenImpl classToken)
   {
      for (TokenBeenImpl subtoken: classToken.getSubTokenList())
      {
         if (TokenType.PROPERTY.equals(subtoken.getType())
                && nodeContent.equals(subtoken.getName()))
         {
            return subtoken;
         }
      }

      return null;
   } 
   
   private static TokenBeenImpl possibleContainerToken;

   private static int nearestTokenLineNumber;
   
   /**
    * Recognize container token of line with lineNumber.  
    * @param targetLineNumber
    * @param tokenList
    * @return container token with token.lineNumber <= targetLineNumber < token.lastLineNumber.
    */
   public TokenBeenImpl getParentToken(int targetLineNumber, List<TokenBeenImpl> tokenList)
   {
      if (tokenList == null || tokenList.size() == 0)
         return null;

      possibleContainerToken = null;
      nearestTokenLineNumber = 0;

      for (TokenBeenImpl token : tokenList)
      {
         // break if token is started at the line after the targetLine
         if (isContainerTokenAfterTheCurrentLine(targetLineNumber, token.getLineNumber()))
         {
            break;
         }
         
         // Test if (token.lineNumber > targetLineNumber) or (targetLineNumber >= token.lastLineNumber)
         else if (isCurrentLineAfterTheContainerToken(targetLineNumber, token.getLastLineNumber()))
         {
            continue;
         }
         
         else if (isPossibleContainerTokenType(token)) 
         {
            searchContainerToken(targetLineNumber, token);
         }
      }

      return possibleContainerToken;
   }

   private void searchContainerToken(int targetLineNumber, TokenBeenImpl currentToken)
   {
      // search appropriate token among the sub token
      List<TokenBeenImpl> subTokenList = currentToken.getSubTokenList();

      if (subTokenList != null && subTokenList.size() != 0)
      {
         for (TokenBeenImpl token : subTokenList)
         {
            // break if token is started at the line after the targetLine
            if (isContainerTokenAfterTheCurrentLine(targetLineNumber, token.getLineNumber()))
            {
               break;
            }
            
            // Test if (token.lineNumber > targetLineNumber) or (targetLineNumber >= token.lastLineNumber)
            else if (isCurrentLineAfterTheContainerToken(targetLineNumber, token.getLastLineNumber()))
            {
               continue;
            }
            
            else if (isPossibleContainerTokenType(token)) 
            {
               searchContainerToken(targetLineNumber, token);
            }
         }
      }

      int currentTokenLineNumber = currentToken.getLineNumber();
      if ((currentTokenLineNumber <= targetLineNumber) && (currentTokenLineNumber >= nearestTokenLineNumber) // taking in mind the last token among them in the line
      )
      {
         nearestTokenLineNumber = currentTokenLineNumber;
         possibleContainerToken = currentToken;
      }
   }
   
   /**
    * Test if this is CLASS or METHOD or INTERFACE token
    * @param targetLineNumber
    * @param token
    * @return
    */
   private boolean isPossibleContainerTokenType(TokenBeenImpl token)
   {
      return TokenType.CLASS.equals(token.getType()) || TokenType.METHOD.equals(token.getType()) || TokenType.INTERFACE.equals(token.getType());
   }   
   
   /**
    * @param targetLineNumber
    * @param lastContainerLineNumber
    * @return true if targetLineNumber => lastContainerLine
    */
   protected static boolean isCurrentLineAfterTheContainerToken(int targetLineNumber, int lastContainerLineNumber)
   {
      return (targetLineNumber >= lastContainerLineNumber);
   }   
}
