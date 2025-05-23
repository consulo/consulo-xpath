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
import consulo.language.impl.DebugUtil;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.inject.InjectedLanguageManagerUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.util.IncorrectOperationException;
import org.intellij.lang.xpath.XPath2ElementTypes;
import org.intellij.lang.xpath.XPathElementTypes;
import org.intellij.lang.xpath.XPathFile;
import org.intellij.lang.xpath.XPathTokenTypes;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.context.XPathVersion;
import org.intellij.lang.xpath.psi.XPathElement;
import org.intellij.lang.xpath.psi.XPathElementVisitor;

import jakarta.annotation.Nonnull;

public class XPathElementImpl extends ASTWrapperPsiElement implements XPathElement {

  public XPathElementImpl(ASTNode node) {
    super(node);
  }

  public String toString() {
    final String name = getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": " + getText();
  }

  public PsiElement addBefore(@Nonnull PsiElement psiElement, final PsiElement anchor) throws IncorrectOperationException {
    final ASTNode node = getNode();
    final ASTNode child = psiElement.getNode();
    assert child != null;
    node.addChild(child, anchor.getNode());
    return node.getPsi();
  }

  public PsiElement addAfter(@Nonnull PsiElement psiElement, final PsiElement anchor) throws IncorrectOperationException {
    final ASTNode astNode = anchor.getNode();
    assert astNode != null;
    final ASTNode next = astNode.getTreeNext();

    final ASTNode node = getNode();
    final ASTNode newNode = psiElement.getNode();
    assert newNode != null;
    if (next != null) {
      node.addChild(newNode, next);
    }
    else {
      node.addChild(newNode);
    }
    return node.getPsi();
  }

  public PsiElement add(@Nonnull PsiElement psiElement) throws IncorrectOperationException {
    final ASTNode child = psiElement.getNode();
    assert child != null;
    getNode().addChild(child);
    return getNode().getPsi();
  }

  public void delete() throws IncorrectOperationException {
    final ASTNode node = getNode();

    final ASTNode parent = node.getTreeParent();
    final ASTNode next = node.getTreeNext();
    parent.removeChild(node);

    if (XPath2ElementTypes.EXPRESSIONS.contains(node.getElementType())) {
      if (parent.getElementType() == XPathElementTypes.FUNCTION_CALL) {
        if (next != null && next.getElementType() == XPathTokenTypes.COMMA) {
          parent.removeChild(next);
        }
      }
    }
  }

  public PsiElement replace(@Nonnull PsiElement psiElement) throws IncorrectOperationException {
    final ASTNode newNode = psiElement.getNode();
    final ASTNode myNode = getNode();

    assert newNode != null;
    myNode.getTreeParent().replaceChild(myNode, newNode);

    return newNode.getPsi();
  }

  @Nonnull
  @SuppressWarnings({"ConstantConditions", "EmptyMethod"})
  public final ASTNode getNode() {
    return super.getNode();
  }

  @Override
  public XPathFile getContainingFile() {
    return (XPathFile)super.getContainingFile();
  }

  @Override
  public ContextProvider getXPathContext() {
    return ContextProvider.getContextProvider(super.getContainingFile());
  }

  @Override
  public XPathVersion getXPathVersion() {
    return getContainingFile().getXPathVersion();
  }

  protected String unexpectedPsiAssertion() {
    return "Unexpected PSI structure: " + DebugUtil.psiToString(this, true, false) + "--\ninside: " + DebugUtil.psiToString(
      getContainingFile(), true, false);
  }

  @Override
  public final void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof XPathElementVisitor) {
      accept((XPathElementVisitor)visitor);
    }
    else {
      super.accept(visitor);
    }
  }

  public void accept(XPathElementVisitor visitor) {
    visitor.visitXPathElement(this);
  }

  public final String getUnescapedText() {
    if (InjectedLanguageManagerUtil.isInInjectedLanguagePrefixSuffix(this)) {
      // do not attempt to decode text if PsiElement is part of prefix/suffix
      return getText();
    }
    return InjectedLanguageManager.getInstance(getProject()).getUnescapedText(this);
  }
}
