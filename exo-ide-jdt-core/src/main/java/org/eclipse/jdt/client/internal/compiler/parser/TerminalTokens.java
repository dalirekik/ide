/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.client.internal.compiler.parser;

/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation.
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API.
 * The mirror implementation is using the backward compatible ITerminalSymbols constant
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens
 * which constant values reflect the latest parser generation state.
 */

/**
 * Maps each terminal symbol in the java-grammar into a unique integer. This integer is used to represent the terminal when
 * computing a parsing action.
 * <p/>
 * Disclaimer : These constant values are generated automatically using a Java grammar, therefore their actual values are subject
 * to change if new keywords were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

    // special tokens not part of grammar - not autogenerated
    int TokenNameWHITESPACE          = 1000, TokenNameCOMMENT_LINE = 1001, TokenNameCOMMENT_BLOCK = 1002,
            TokenNameCOMMENT_JAVADOC = 1003;

    int TokenNameIdentifier               = 26, TokenNameabstract = 56, TokenNameassert = 74, TokenNameboolean = 32,
            TokenNamebreak                = 75, TokenNamebyte = 33, TokenNamecase = 102, TokenNamecatch = 100, TokenNamechar = 34,
            TokenNameclass                = 72, TokenNamecontinue = 76, TokenNameconst = 108, TokenNamedefault = 97, TokenNamedo = 77,
            TokenNamedouble               = 35, TokenNameelse = 104, TokenNameenum = 98, TokenNameextends = 99, TokenNamefalse = 44,
            TokenNamefinal                = 57, TokenNamefinally = 103, TokenNamefloat = 36, TokenNamefor = 78, TokenNamegoto = 109,
            TokenNameif                   = 79, TokenNameimplements = 106, TokenNameimport = 101, TokenNameinstanceof = 13,
            TokenNameint                  = 37, TokenNameinterface = 95, TokenNamelong = 38, TokenNamenative = 58, TokenNamenew = 43,
            TokenNamenull                 = 45, TokenNamepackage = 96, TokenNameprivate = 59, TokenNameprotected = 60,
            TokenNamepublic               = 61, TokenNamereturn = 80, TokenNameshort = 39, TokenNamestatic = 54,
            TokenNamestrictfp             = 62, TokenNamesuper = 41, TokenNameswitch = 81, TokenNamesynchronized = 55,
            TokenNamethis                 = 42, TokenNamethrow = 82, TokenNamethrows = 105, TokenNametransient = 63,
            TokenNametrue                 = 46, TokenNametry = 83, TokenNamevoid = 40, TokenNamevolatile = 64, TokenNamewhile = 73,
            TokenNameIntegerLiteral       = 47, TokenNameLongLiteral = 48, TokenNameFloatingPointLiteral = 49,
            TokenNameDoubleLiteral        = 50, TokenNameCharacterLiteral = 51, TokenNameStringLiteral = 52,
            TokenNamePLUS_PLUS            = 8, TokenNameMINUS_MINUS = 9, TokenNameEQUAL_EQUAL = 18, TokenNameLESS_EQUAL = 14,
            TokenNameGREATER_EQUAL        = 15, TokenNameNOT_EQUAL = 19, TokenNameLEFT_SHIFT = 17, TokenNameRIGHT_SHIFT = 10,
            TokenNameUNSIGNED_RIGHT_SHIFT = 12, TokenNamePLUS_EQUAL = 84, TokenNameMINUS_EQUAL = 85,
            TokenNameMULTIPLY_EQUAL       = 86, TokenNameDIVIDE_EQUAL = 87, TokenNameAND_EQUAL = 88, TokenNameOR_EQUAL = 89,
            TokenNameXOR_EQUAL            = 90, TokenNameREMAINDER_EQUAL = 91, TokenNameLEFT_SHIFT_EQUAL = 92,
            TokenNameRIGHT_SHIFT_EQUAL    = 93, TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 94, TokenNameOR_OR = 25,
            TokenNameAND_AND              = 24, TokenNamePLUS = 1, TokenNameMINUS = 2, TokenNameNOT = 66, TokenNameREMAINDER = 5,
            TokenNameXOR                  = 21, TokenNameAND = 20, TokenNameMULTIPLY = 4, TokenNameOR = 22, TokenNameTWIDDLE = 67,
            TokenNameDIVIDE               = 6, TokenNameGREATER = 11, TokenNameLESS = 7, TokenNameLPAREN = 29, TokenNameRPAREN = 28,
            TokenNameLBRACE               = 68, TokenNameRBRACE = 31, TokenNameLBRACKET = 16, TokenNameRBRACKET = 70,
            TokenNameSEMICOLON            = 27, TokenNameQUESTION = 23, TokenNameCOLON = 65, TokenNameCOMMA = 30,
            TokenNameDOT                  = 3, TokenNameEQUAL = 71, TokenNameAT = 53, TokenNameELLIPSIS = 107, TokenNameEOF = 69,
            TokenNameERROR                = 110;
}
