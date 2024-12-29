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
import org.intellij.lang.xpath.XPath2TokenTypes;
import org.intellij.lang.xpath.XPathElementType;
import org.intellij.lang.xpath.psi.*;

import jakarta.annotation.Nonnull;

public class XPath2RangeExpressionImpl extends XPath2ElementImpl implements XPath2RangeExpression {
  public XPath2RangeExpressionImpl(ASTNode node) {
    super(node);
  }

  @Nonnull
  @Override
  public XPathExpression getFrom() {
    return findChildrenByClass(XPathExpression.class)[0];
  }

  @Override
  public XPathExpression getTo() {
    XPathExpression[] expressions = findChildrenByClass(XPathExpression.class);
    return expressions.length > 1 ? expressions[1] : null;
  }

  @Override
  public XPathExpression getLOperand() {
    return getFrom();
  }

  @Override
  public XPathExpression getROperand() {
    return getTo();
  }

  @Nonnull
  @Override
  public XPathElementType getOperator() {
    return (XPathElementType)XPath2TokenTypes.TO;
  }

  @Nonnull
  @Override
  public String getOperationSign() {
    return "to";
  }

  @Nonnull
  @Override
  public XPathType getType() {
    return XPath2SequenceType.create(XPath2Type.INTEGER, XPath2SequenceType.Cardinality.ZERO_OR_MORE);
  }

  public void accept(XPath2ElementVisitor visitor) {
    visitor.visitXPath2RangeExpression(this);
  }
}