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
package org.intellij.lang.xpath.validation;

import javax.annotation.Nonnull;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.impl.XPathChangeUtil;

class ExpressionReplacementFix implements IntentionAction {
  private final String myReplacement;
  private final String myDisplay;
  private final XPathExpression myExpr;

  public ExpressionReplacementFix(String replacement, XPathExpression expr) {
    this(replacement, replacement, expr);
  }

  public ExpressionReplacementFix(String replacement, String display, XPathExpression expression) {
    myReplacement = replacement;
    myDisplay = display;
    myExpr = expression;
  }

  @Nonnull
  @Override
  public String getText() {
    return "Replace with '" + myDisplay + "'";
  }

  @Nonnull
  @Override
  public String getFamilyName() {
    return "XPath2";
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
    return myExpr.isValid();
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    myExpr.replace(XPathChangeUtil.createExpression(myExpr, myReplacement));
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}