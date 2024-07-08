/*
 * Copyright 2002-2005 Sascha Weinreuter
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
package org.intellij.plugins.xpathView.util;

import consulo.codeEditor.Editor;
import consulo.language.ast.ASTNode;
import consulo.language.editor.annotation.Annotation;
import consulo.language.editor.annotation.AnnotatorUtil;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.*;
import consulo.logging.Logger;
import consulo.xml.psi.xml.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MyPsiUtil {
    private static final Logger LOG = Logger.getInstance(MyPsiUtil.class);

    private MyPsiUtil() {
    }

    @Nullable
    public static XmlElement findContextNode(@Nonnull PsiFile psiFile, @Nonnull Editor editor) {
        PsiElement contextNode = psiFile.findElementAt(editor.getCaretModel().getOffset());
        while (contextNode != null && !isValidContextNode(contextNode)) {
            contextNode = contextNode.getParent();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning context node: " + contextNode);
        }
        assert contextNode == null || contextNode instanceof XmlElement;
        return (XmlElement)contextNode;
    }

    public static boolean isValidContextNode(@Nullable PsiElement contextNode) {
        if (contextNode instanceof XmlTag) {
            return true;
        } else if (contextNode instanceof XmlDocument) {
            return true;
        }
        return false;
    }

    @Nonnull
    public static PsiElement getNameElement(@Nonnull XmlTag tag) {
        final PsiElement element = findNameElement(tag);
        if (element != null) {
            return element;
        }
        LOG.error("Name element not found for " + tag);
        return tag;
    }

    @Nullable
    public static PsiElement findNameElement(@Nonnull XmlTag tag) {
        PsiElement[] children = tag.getChildren();
        for (PsiElement child : children) {
            if (isNameElement(child)) {
                return child;
            }
        }
        return null;
    }

    public static boolean isNameElement(@Nullable PsiElement child) {
      if (child != null) {
        if (child.getParent() instanceof XmlTag) {
          if (child instanceof XmlToken) {
            if (((XmlToken)child).getTokenType() == XmlTokenType.XML_NAME) {
              return true;
            }
          } else if (child instanceof ASTNode) {
            return ((ASTNode)child).getElementType() == XmlTokenType.XML_NAME;
          }
        }
      }
      return false;
    }

    public static String getAttributePrefix(@Nonnull XmlAttribute attribute) {
        final String name = attribute.getName();
        if (name.indexOf(':') == -1) {
            return "";
        } else {
            return name.substring(0, name.indexOf(':'));
        }
    }

    public static boolean isStartTag(PsiElement contextNode) {
        if (contextNode instanceof PsiWhiteSpace) {
            PsiElement sibling = contextNode.getPrevSibling();
            while (sibling != null && !isNameElement(sibling)) {
                sibling = sibling.getPrevSibling();
            }
            return sibling != null;
        } else if (contextNode instanceof XmlToken) {
            if (((XmlToken)contextNode).getTokenType() == XmlTokenType.XML_START_TAG_START) return true;
            if (((XmlToken)contextNode).getTokenType() == XmlTokenType.XML_TAG_END) return true;
            if (((XmlToken)contextNode).getTokenType() == XmlTokenType.XML_EMPTY_ELEMENT_END) return true;
        }
        return false;
    }
    public static boolean isEndTag(PsiElement contextNode) {
        if (contextNode instanceof PsiWhiteSpace) {
            PsiElement sibling = contextNode.getPrevSibling();
            while (sibling != null && !isNameElement(sibling)) {
                sibling = sibling.getPrevSibling();
            }
            return sibling != null;
        } else if (contextNode instanceof XmlToken) {
            if (((XmlToken)contextNode).getTokenType() == XmlTokenType.XML_END_TAG_START) return true;
        }
        return false;
    }

    /**
     * This method checks if the passed element's namespace is actually declared in the document or if has an
     * implicit namespace URI which as of late, IDEA assigns to Ant files, Web descriptors and anything that has a
     * DTD defined. For XPath queries this is very inconvenient when having to enter a namespace-prefix with every
     * element-step. For XPath-Expression generation this results in more complex expressions than necessary.
     */
    public static boolean isInDeclaredNamespace(XmlTag context, String nsUri, String nsPrefix) {

        if (nsUri == null || nsUri.length() == 0 || nsPrefix != null && nsPrefix.length() > 0) {
            return true;
        }

        do {
            if (context.getLocalNamespaceDeclarations().containsValue(nsUri)) return true;
            context = (XmlTag)(context.getParent() instanceof XmlTag ? context.getParent() : null);
        } while (context != null);

        return false;
    }

    public static String checkFile(final PsiFile file) {
        final String[] error = new String[1];
        file.accept(new PsiRecursiveElementVisitor() {
            public void visitErrorElement(PsiErrorElement element) {
                error[0] = element.getErrorDescription();
            }
        });
        if (error[0] != null) return error[0];

        file.accept(new PsiRecursiveElementVisitor() {
            public void visitElement(PsiElement element) {
                List<Annotation> annotations = AnnotatorUtil.runAnnotators(file, element);
                for (Annotation annotation : annotations) {
                    if (annotation.getSeverity() == HighlightSeverity.ERROR) {
                        error[0] = annotation.getMessage().get();
                        return;
                    }
                }
                
                super.visitElement(element);
            }
        });
        return error[0];
    }
}
