/*
 * Copyright 2005 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.xpath;

import consulo.codeEditor.CodeInsightColors;
import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
public class XPathHighlighter extends SyntaxHighlighterBase {
    private static final Map keys1;
    private static final Map keys1_2;
    private static final Map keys2;

    private final boolean myXPath2Syntax;

    public XPathHighlighter(boolean xpath2Syntax) {
        myXPath2Syntax = xpath2Syntax;
    }

    @Nonnull
    public Lexer getHighlightingLexer() {
        return XPathLexer.create(myXPath2Syntax);
    }

    static final TextAttributesKey XPATH_KEYWORD = TextAttributesKey.of("XPATH.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

    static final TextAttributesKey XPATH_STRING = TextAttributesKey.of(
        "XPATH.STRING",
        DefaultLanguageHighlighterColors.STRING
    );

    static final TextAttributesKey XPATH_NUMBER = TextAttributesKey.of(
        "XPATH.NUMBER",
        DefaultLanguageHighlighterColors.NUMBER
    );

    static final TextAttributesKey XPATH_OPERATION_SIGN = TextAttributesKey.of(
        "XPATH.OPERATION_SIGN",
        DefaultLanguageHighlighterColors.OPERATION_SIGN
    );

    static final TextAttributesKey XPATH_PARENTH = TextAttributesKey.of(
        "XPATH.PARENTH",
        DefaultLanguageHighlighterColors.PARENTHESES
    );

    static final TextAttributesKey XPATH_BRACKET = TextAttributesKey.of(
        "XPATH.BRACKET",
        DefaultLanguageHighlighterColors.BRACKETS
    );

    static final TextAttributesKey XPATH_FUNCTION = TextAttributesKey.of(
        "XPATH.FUNCTION",
        DefaultLanguageHighlighterColors.FUNCTION_CALL
    );

    static final TextAttributesKey XPATH_VARIABLE = TextAttributesKey.of(
        "XPATH.XPATH_VARIABLE",
        DefaultLanguageHighlighterColors.LOCAL_VARIABLE
    );

    public static final TextAttributesKey XPATH_EVAL_CONTEXT_HIGHLIGHT = TextAttributesKey.of("XPATH.EVAL_CONTEXT_HIGHLIGHT");

    public static final TextAttributesKey XPATH_EVAL_HIGHLIGHT = TextAttributesKey.of("XPATH.EVAL_HIGHLIGHT");

    static final TextAttributesKey XPATH_PREFIX = TextAttributesKey.of("XPATH.XPATH_PREFIX", HighlighterColors.TEXT);

    static final TextAttributesKey XPATH_NAME = TextAttributesKey.of("XPATH.XPATH_NAME", HighlighterColors.TEXT);

    static final TextAttributesKey XPATH_TEXT = TextAttributesKey.of("XPATH.XPATH_TEXT", HighlighterColors.TEXT);

    static {
        keys1 = new HashMap();
        keys2 = new HashMap();

        fillMap(keys1, XPathTokenTypes.BINARY_OPERATIONS, XPATH_OPERATION_SIGN);
        fillMap(keys1, XPathTokenTypes.KEYWORDS, XPATH_KEYWORD);

        fillMap(keys1, XPathTokenTypes.REST, XPATH_TEXT);

        keys1.put(XPathTokenTypes.NCNAME, XPATH_NAME);

        keys1.put(XPathTokenTypes.NUMBER, XPATH_NUMBER);
        keys1.put(XPathTokenTypes.STRING_LITERAL, XPATH_STRING);
        keys1.put(XPathTokenTypes.FUNCTION_NAME, XPATH_FUNCTION);
        keys1.put(XPathTokenTypes.EXT_PREFIX, XPATH_PREFIX);

        keys1.put(XPathTokenTypes.DOLLAR, XPATH_VARIABLE);
        keys1.put(XPathTokenTypes.VARIABLE_NAME, XPATH_VARIABLE);
        keys1.put(XPathTokenTypes.VARIABLE_PREFIX, XPATH_PREFIX);

        keys1.put(XPathTokenTypes.LPAREN, XPATH_PARENTH);
        keys1.put(XPathTokenTypes.LBRACKET, XPATH_BRACKET);
        keys1.put(XPathTokenTypes.RPAREN, XPATH_PARENTH);
        keys1.put(XPathTokenTypes.RBRACKET, XPATH_BRACKET);

        keys1.put(XPathTokenTypes.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
        keys1.put(XPathTokenTypes.BAD_AXIS_NAME, CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);

        keys1_2 = new HashMap(keys1);
        fillMap(keys1_2, XPath2TokenTypes.KEYWORDS, XPATH_KEYWORD);
    }

    @Nonnull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(((TextAttributesKey) (myXPath2Syntax ? keys1_2 : keys1).get(tokenType)), ((TextAttributesKey) keys2.get(tokenType)));
    }
}
