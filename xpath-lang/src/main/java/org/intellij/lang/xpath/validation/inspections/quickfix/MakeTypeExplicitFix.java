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

/*
 * Created by IntelliJ IDEA.
 * User: sweinreuter
 * Date: 10.04.2006
 * Time: 23:07:47
 */
package org.intellij.lang.xpath.validation.inspections.quickfix;

import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.XPathFunctionCall;
import org.intellij.lang.xpath.psi.XPathType;
import org.intellij.lang.xpath.validation.ExpectedTypeUtil;

import jakarta.annotation.Nonnull;

public class MakeTypeExplicitFix extends ReplaceElementFix<XPathExpression> {
    private final XPathType myType;

    public MakeTypeExplicitFix(XPathExpression expression, XPathType type) {
        super(expression);
        myType = type;
    }

    @Nonnull
    public String getText() {
        return "Make Type Conversion Explicit";
    }

    @Nonnull
    public String getFamilyName() {
        return "ImplicitTypeConversion";
    }

    public void invokeImpl(Project project, PsiFile file) throws IncorrectOperationException {
      XPathExpression myElement = (XPathExpression)getStartElement();
      if (myType == XPathType.BOOLEAN) {
        if (myElement.getType() == XPathType.STRING) {
          final String text;
          if (ExpectedTypeUtil.isExplicitConversion(myElement)) {
            final XPathExpression expr = ExpectedTypeUtil.unparenthesize(myElement);
            assert expr != null;

            text = ((XPathFunctionCall)expr).getArgumentList()[0].getText();
          }
          else {
            text = myElement.getText();
          }
          replace("string-length(" + text + ") > 0");
          return;
        }
        else if (myElement.getType() == XPathType.NODESET) {
          replace("count(" + myElement.getText() + ") > 0");
          return;
        }
      }
      replace(myType.getName() + "(" + myElement.getText() + ")");
    }
}
