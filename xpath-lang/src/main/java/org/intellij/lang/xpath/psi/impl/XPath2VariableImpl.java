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
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import org.intellij.lang.xpath.psi.*;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

public class XPath2VariableImpl extends XPathElementImpl implements XPathVariable {
  public XPath2VariableImpl(ASTNode node) {
    super(node);
  }

  @Nonnull
  public XPathType getType() {
    final XPathExpression value = getValue();
    return value != null ? value.getType() : XPathType.UNKNOWN;
  }

  @Override
  public XPathExpression getValue() {
    final XPathVariableDeclaration d = PsiTreeUtil.getParentOfType(this, XPathVariableDeclaration.class);
    if (d != null) {
      return d.getInitializer();
    } else {
      return null;
    }
  }

  @Override
  public int getTextOffset() {
    return getTextRange().getStartOffset() + 1;
  }

  @Override
  public String getName() {
    return getText().substring(1);
  }

  @Override
  public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException {
    return replace(XPathChangeUtil.createVariableReference(this, name));
  }

  public void accept(XPathElementVisitor visitor) {
    visitor.visitXPathVariable(this);
  }
}