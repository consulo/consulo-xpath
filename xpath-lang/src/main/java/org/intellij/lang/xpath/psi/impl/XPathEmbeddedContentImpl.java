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

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiElementProcessor;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.xml.psi.xml.XmlTag;
import consulo.xml.psi.xml.XmlTagChild;

import jakarta.annotation.Nonnull;

@SuppressWarnings({"ConstantConditions"})
public class XPathEmbeddedContentImpl extends XPathElementImpl implements XmlTagChild {
    public XPathEmbeddedContentImpl(ASTNode node) {
        super(node);
    }

    public XmlTag getParentTag() {
        final PsiElement parent = getParent();
        if(parent instanceof XmlTag) return (XmlTag)parent;
        return null;
    }

    public XmlTagChild getNextSiblingInTag() {
        PsiElement nextSibling = getNextSibling();
        if(nextSibling instanceof XmlTagChild) return (XmlTagChild)nextSibling;
        return null;
    }

    public XmlTagChild getPrevSiblingInTag() {
        final PsiElement prevSibling = getPrevSibling();
        if(prevSibling instanceof XmlTagChild) return (XmlTagChild)prevSibling;
        return null;
    }

    @SuppressWarnings({"RawUseOfParameterizedType"})
    public boolean processElements(PsiElementProcessor processor, PsiElement place) {
        // TODO
        return true;
    }

    @Override
    public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState substitutor, PsiElement lastParent, @Nonnull PsiElement place) {
        if (lastParent == null) {
            PsiElement child = getFirstChild();
            while (child != null) {
                if (!child.processDeclarations(processor, substitutor, null, place)) return false;
                child = child.getNextSibling();
            }
        }

        return true;
    }
}