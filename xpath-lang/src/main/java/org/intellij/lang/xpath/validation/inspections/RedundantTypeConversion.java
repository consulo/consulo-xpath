/*
 * Copyright 2006 Sascha Weinreuter
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
package org.intellij.lang.xpath.validation.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.psi.XPath2SequenceType;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.XPathFunctionCall;
import org.intellij.lang.xpath.psi.XPathType;
import org.intellij.lang.xpath.validation.ExpectedTypeUtil;
import org.intellij.lang.xpath.validation.inspections.quickfix.XPathQuickFixFactory;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class RedundantTypeConversion extends XPathInspection<Object> {
    @NonNls
    private static final String SHORT_NAME = "RedundantTypeConversion";

    public boolean CHECK_ANY = false;

    @Nonnull
    public String getDisplayName() {
        return "Redundant Type Conversion";
    }

    @Nonnull
    @NonNls
    public String getShortName() {
        return SHORT_NAME;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    protected Visitor createVisitor(InspectionManager manager, ProblemsHolder holder, boolean isOnTheFly, Object state) {
        return new MyElementVisitor(manager, holder, isOnTheFly, state);
    }

    final class MyElementVisitor extends Visitor<Object> {

        MyElementVisitor(InspectionManager manager, ProblemsHolder holder,boolean isOnTheFly, Object state) {
            super(manager, holder, isOnTheFly, state);
        }

        protected void checkExpression(final @Nonnull XPathExpression expr) {
            if (ExpectedTypeUtil.isExplicitConversion(expr)) {
                final XPathExpression expression = ExpectedTypeUtil.unparenthesize(expr);
                assert expression != null;
                
                final XPathType convertedType = ((XPathFunctionCall)expression).getArgumentList()[0].getType();
                if (isSameType(expression, convertedType)) {
                    final XPathQuickFixFactory fixFactory = ContextProvider.getContextProvider(expression).getQuickFixFactory();
                    LocalQuickFix[] fixes = fixFactory.createRedundantTypeConversionFixes(expression);

                    addProblem(myManager.createProblemDescriptor(expression,
                                                                 "Redundant conversion to type '" + convertedType.getName() + "'", myOnTheFly, fixes,
                                                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
                } else if (CHECK_ANY) {
                    final XPathType expectedType = ExpectedTypeUtil.getExpectedType(expression);
                    if (expectedType == XPathType.ANY) {
                        final XPathQuickFixFactory fixFactory = ContextProvider.getContextProvider(expression).getQuickFixFactory();
                        LocalQuickFix[] fixes = fixFactory.createRedundantTypeConversionFixes(expression);

                        addProblem(myManager.createProblemDescriptor(expression,
                                "Redundant conversion to type '" + expectedType.getName() + "'", myOnTheFly, fixes,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
                    }
                }
            }
        }

      private boolean isSameType(XPathExpression expression, XPathType convertedType) {
        XPathType type = ExpectedTypeUtil.mapType(expression, expression.getType());
        while (type instanceof XPath2SequenceType) {
          type = ((XPath2SequenceType)type).getType();
        }
        while (convertedType instanceof XPath2SequenceType) {
          convertedType = ((XPath2SequenceType)convertedType).getType();
        }
        return ExpectedTypeUtil.mapType(expression, convertedType) == type;
      }
    }
}
