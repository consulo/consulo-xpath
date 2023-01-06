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

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import org.intellij.lang.xpath.psi.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class XPathParserDefinition implements ParserDefinition {

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathLanguage.INSTANCE;
  }

  @Override
  @Nonnull
  public Lexer createLexer(@Nonnull LanguageVersion languageVersion) {
    return XPathLexer.create(false);
  }

  @Override
  @Nonnull
  public IFileElementType getFileNodeType() {
    return XPathElementTypes.FILE;
  }

  @Override
  @Nonnull
  public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion languageVersion) {
    return TokenSet.create(XPathTokenTypes.WHITESPACE);
  }

  @Override
  @Nonnull
  public TokenSet getCommentTokens(@Nonnull LanguageVersion languageVersion) {
    return TokenSet.EMPTY;
  }

  @Override
  @Nonnull
  public TokenSet getStringLiteralElements(@Nonnull LanguageVersion languageVersion) {
    return TokenSet.create(XPathTokenTypes.STRING_LITERAL);
  }

  @Override
  @Nonnull
  public PsiParser createParser(@Nonnull LanguageVersion languageVersion) {
    return new XPathParser();
  }

  @Nonnull
  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MUST_NOT;
  }

  @Override
  @Nonnull
  public final PsiElement createElement(@Nonnull ASTNode node) {
    final IElementType type = node.getElementType();

    final PsiElement element = createElement(type, node);
    if (element != null) {
      return element;
    }
    return new XPathTokenImpl(node);
  }

  @Nullable
  protected PsiElement createElement(IElementType type, ASTNode node) {
    if (type == XPathElementTypes.NUMBER) {
      return new XPathNumberImpl(node);
    }
    else if (type == XPathElementTypes.STRING) {
      return new XPathStringImpl(node);
    }
    else if (type == XPathElementTypes.BINARY_EXPRESSION) {
      return new XPathBinaryExpressionImpl(node);
    }
    else if (type == XPathElementTypes.PREFIX_EXPRESSION) {
      return new XPathPrefixExpressionImpl(node);
    }
    else if (type == XPathElementTypes.PARENTHESIZED_EXPR) {
      return new XPathParenthesizedExpressionImpl(node);
    }
    else if (type == XPathElementTypes.FILTER_EXPRESSION) {
      return new XPathFilterExpressionImpl(node);
    }
    else if (type == XPathElementTypes.FUNCTION_CALL) {
      return new XPathFunctionCallImpl(node);
    }
    else if (type == XPathElementTypes.AXIS_SPECIFIER) {
      return new XPathAxisSpecifierImpl(node);
    }
    else if (type == XPathElementTypes.PREDICATE) {
      return new XPathPredicateImpl(node);
    }
    else if (type == XPathElementTypes.LOCATION_PATH) {
      return new XPathLocationPathImpl(node);
    }
    else if (type == XPathElementTypes.STEP) {
      return new XPathStepImpl(node);
    }
    else if (type == XPathElementTypes.NODE_TEST) {
      return new XPathNodeTestImpl(node);
    }
    else if (type == XPathElementTypes.NODE_TYPE) {
      return new XPathNodeTypeTestImpl(node);
    }
    else if (type == XPathElementTypes.VARIABLE_REFERENCE) {
      return new XPathVariableReferenceImpl(node);
    }
    else if (type == XPathElementTypes.EMBEDDED_CONTENT) {
      return new XPathEmbeddedContentImpl(node);
    }

    return null;
  }

  @Override
  public PsiFile createFile(@Nonnull FileViewProvider viewProvider) {
    return new XPathFile(viewProvider, XPathFileType.XPATH);
  }
}
