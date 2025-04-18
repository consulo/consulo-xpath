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
package org.intellij.lang.xpath.psi.impl;

import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.Comparing;
import org.intellij.lang.xpath.psi.XPathElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class ReferenceBase implements PsiReference {
    private final XPathElement element;
    private final ASTNode nameNode;

    public ReferenceBase(XPathElement element, ASTNode nameNode) {
        this.element = element;
        this.nameNode = nameNode;
    }

    public XPathElement getElement() {
        return element;
    }

    public TextRange getRangeInElement() {
        final int outer = element.getTextRange().getStartOffset();
        return TextRange.from(nameNode.getTextRange().getStartOffset() - outer, nameNode.getTextLength());
    }

    @Nullable
    public PsiElement resolve() {
        return null;
    }

    @Nonnull
    public String getCanonicalText() {
        return nameNode.getText();
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        throw new IncorrectOperationException("unsupported");
    }

    public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
        throw new IncorrectOperationException("unsupported");
    }

    public boolean isReferenceTo(PsiElement element) {
        return Comparing.equal(resolve(), element);
    }

    @Nonnull
    public abstract Object[] getVariants();

    public boolean isSoft() {
        return true;
    }

    public ASTNode getNameNode() {
      return nameNode;
    }
}
