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
import org.intellij.lang.xpath.psi.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class XPath2InstanceOfImpl extends XPath2ElementImpl implements XPath2InstanceOf {
  public XPath2InstanceOfImpl(ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public XPathType getTargetType() {
    final XPath2TypeElement node = findChildByClass(XPath2TypeElement.class);
    return node != null ? node.getDeclaredType() : null;
  }

  @Nonnull
  @Override
  public XPathType getType() {
    return XPath2Type.BOOLEAN;
  }

  public void accept(XPath2ElementVisitor visitor) {
    visitor.visitXPath2InstanceOf(this);
  }
}