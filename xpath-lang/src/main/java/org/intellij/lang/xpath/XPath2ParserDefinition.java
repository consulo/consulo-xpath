/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import org.intellij.lang.xpath.psi.impl.*;

import jakarta.annotation.Nonnull;

/*
 * Created by IntelliJ IDEA.
 * User: sweinreuter
 * Date: 04.01.11
 */
@ExtensionImpl
public class XPath2ParserDefinition extends XPathParserDefinition {
  @Nonnull
  @Override
  public Language getLanguage() {
    return XPath2Language.INSTANCE;
  }

  @Nonnull
  @Override
  public Lexer createLexer(@Nonnull LanguageVersion languageVersion) {
    return XPathLexer.create(true);
  }

  @Override
  @Nonnull
  public IFileElementType getFileNodeType() {
    return XPath2ElementTypes.FILE;
  }

  @Nonnull
  @Override
  public PsiParser createParser(@Nonnull LanguageVersion languageVersion) {
    return new XPath2Parser();
  }

  @Nonnull
  @Override
  public TokenSet getCommentTokens(LanguageVersion languageVersion) {
    return TokenSet.create(XPath2TokenTypes.COMMENT);
  }

  @Override
  protected PsiElement createElement(IElementType type, ASTNode node) {
    final PsiElement element = super.createElement(type, node);
    if (element != null) {
      return element;
    }

    if (type == XPath2ElementTypes.VARIABLE_DECL) {
      return new XPath2VariableImpl(node);
    }
    else if (type == XPath2ElementTypes.CONTEXT_ITEM) {
      return new XPathStepImpl(node);
    }
    else if (type == XPath2ElementTypes.IF) {
      return new XPath2IfImpl(node);
    }
    else if (type == XPath2ElementTypes.QUANTIFIED) {
      return new XPath2QuantifiedExprImpl(node);
    }
    else if (type == XPath2ElementTypes.FOR) {
      return new XPath2ForImpl(node);
    }
    else if (type == XPath2ElementTypes.BINDING_SEQ) {
      return new XPath2VariableDeclarationImpl(node);
    }
    else if (type == XPath2ElementTypes.SEQUENCE) {
      return new XPath2SequenceImpl(node);
    }
    else if (type == XPath2ElementTypes.RANGE_EXPRESSION) {
      return new XPath2RangeExpressionImpl(node);
    }
    else if (type == XPath2ElementTypes.CASTABLE_AS) {
      return new XPath2CastableImpl(node);
    }
    else if (type == XPath2ElementTypes.CAST_AS) {
      return new XPath2CastImpl(node);
    }
    else if (type == XPath2ElementTypes.INSTANCE_OF) {
      return new XPath2InstanceOfImpl(node);
    }
    else if (type == XPath2ElementTypes.TREAT_AS) {
      return new XPath2TreatAsImpl(node);
    }
    else if (XPath2ElementTypes.TYPE_ELEMENTS.contains(type)) {
      return new XPath2TypeElementImpl(node);
    }

    return null;
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new XPathFile(viewProvider, XPathFileType.XPATH2);
  }
}