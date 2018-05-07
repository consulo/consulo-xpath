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

import javax.annotation.Nonnull;

import com.intellij.lang.BracePair;
import com.intellij.lang.Language;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.lang.cacheBuilder.SimpleWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.lexer.Lexer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import org.intellij.lang.xpath.completion.CompletionLists;
import org.intellij.lang.xpath.psi.XPathFunction;
import org.intellij.lang.xpath.psi.XPathVariable;

import javax.annotation.Nullable;

public final class XPathLanguage extends Language {
    public static final String ID = "XPath";

    XPathLanguage() {
        super(ID);
    }

  @Override
  public XPathFileType getAssociatedFileType() {
    return XPathFileType.XPATH;
  }

  public static class XPathPairedBraceMatcher implements PairedBraceMatcher {
        private BracePair[] myBracePairs;

        public BracePair[] getPairs() {
            if (myBracePairs == null) {
                myBracePairs = new BracePair[]{
                        new BracePair(XPathTokenTypes.LPAREN, XPathTokenTypes.RPAREN, false),
                        new BracePair(XPathTokenTypes.LBRACKET, XPathTokenTypes.RBRACKET, false),
                };
            }
            return myBracePairs;
        }

        public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
            return openingBraceOffset;
        }

        public boolean isPairedBracesAllowedBeforeType(@Nonnull IElementType lbraceType, @Nullable IElementType contextType) {
            return true;
        }
    }

    public static class XPathFindUsagesProvider implements FindUsagesProvider {
        @Nullable
        public WordsScanner getWordsScanner() {
            return new SimpleWordsScanner();
        }

        public boolean canFindUsagesFor(@Nonnull PsiElement psiElement) {
            return psiElement instanceof XPathFunction || psiElement instanceof XPathVariable;
        }

        @Nullable
        public String getHelpId(@Nonnull PsiElement psiElement) {
            return null;
        }

        @Nonnull
        public String getType(@Nonnull PsiElement element) {
          if (element instanceof XPathFunction) {
            return "function";
          } else if (element instanceof XPathVariable) {
            return "variable";
          } else {
            return "unknown";
          }
        }

        @Nonnull
        public String getDescriptiveName(@Nonnull PsiElement element) {
            if (element instanceof PsiNamedElement) {
                final String name = ((PsiNamedElement)element).getName();
                if (name != null) return name;
            }
            return element.toString();
        }

        @Nonnull
        public String getNodeText(@Nonnull PsiElement element, boolean useFullName) {
            if (useFullName) {
                if (element instanceof NavigationItem) {
                    final NavigationItem navigationItem = ((NavigationItem)element);
                    final ItemPresentation presentation = navigationItem.getPresentation();
                    if (presentation != null) {
                      final String text = presentation.getPresentableText();
                      if (text != null) {
                          return text;
                      }
                    }
                    final String name = navigationItem.getName();
                    if (name != null) {
                        return name;
                    }
                }
            }
            if (element instanceof PsiNamedElement) {
                final String name = ((PsiNamedElement)element).getName();
                if (name != null) return name;
            }
            return element.toString();
        }
    }

    public static class XPathNamesValidator implements NamesValidator {
        private final Lexer xPathLexer = XPathLexer.create(false);

        public synchronized boolean isIdentifier(String text, Project project) {
            xPathLexer.start(text);
            assert xPathLexer.getState() == 0;

            boolean b = xPathLexer.getTokenType() == XPathTokenTypes.NCNAME;
            xPathLexer.advance();

            if (xPathLexer.getTokenType() == null) {
                return b;
            } else if (xPathLexer.getTokenType() == XPathTokenTypes.COL) {
                xPathLexer.advance();
                b = xPathLexer.getTokenType() == XPathTokenTypes.NCNAME;
                xPathLexer.advance();
                return b && xPathLexer.getTokenType() == null;
            }

            return false;
        }

        public boolean isKeyword(String text, Project project) {
            return CompletionLists.AXIS_NAMES.contains(text) || CompletionLists.NODE_TYPE_FUNCS.contains(text) || CompletionLists.OPERATORS.contains(text);
        }
    }

    public static class XPathSyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
        @Nonnull
        protected SyntaxHighlighter createHighlighter() {
            return new XPathHighlighter(false);
        }
    }
}
