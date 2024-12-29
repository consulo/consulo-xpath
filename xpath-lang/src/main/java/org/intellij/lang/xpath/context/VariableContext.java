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
package org.intellij.lang.xpath.context;

import consulo.language.editor.intention.IntentionAction;
import consulo.language.psi.PsiElement;
import org.intellij.lang.xpath.psi.XPathElement;
import org.intellij.lang.xpath.psi.XPathVariable;
import org.intellij.lang.xpath.psi.XPathVariableReference;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface VariableContext<VarType> {
  @Nonnull
  VarType[] getVariablesInScope(XPathElement element);

  boolean canResolve();

  @Nullable
  XPathVariable resolve(XPathVariableReference reference);

  @Nonnull
  IntentionAction[] getUnresolvedVariableFixes(XPathVariableReference reference);

  boolean isReferenceTo(PsiElement element, XPathVariableReference reference);
}
