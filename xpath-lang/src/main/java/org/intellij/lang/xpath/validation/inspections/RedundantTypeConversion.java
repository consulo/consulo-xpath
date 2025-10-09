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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.psi.XPath2SequenceType;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.XPathFunctionCall;
import org.intellij.lang.xpath.psi.XPathType;
import org.intellij.lang.xpath.validation.ExpectedTypeUtil;
import org.intellij.lang.xpath.validation.inspections.quickfix.XPathQuickFixFactory;

@ExtensionImpl
public class RedundantTypeConversion extends XPathInspection<Object> {
    private static final String SHORT_NAME = "RedundantTypeConversion";

    public boolean CHECK_ANY = false;

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Redundant Type Conversion");
    }

    @Nonnull
    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    protected Visitor createVisitor(InspectionManager manager, ProblemsHolder holder, boolean isOnTheFly, Object state) {
        return new MyElementVisitor(manager, holder, isOnTheFly, state);
    }

    final class MyElementVisitor extends Visitor<Object> {
        MyElementVisitor(InspectionManager manager, ProblemsHolder holder, boolean isOnTheFly, Object state) {
            super(manager, holder, isOnTheFly, state);
        }

        @Override
        @RequiredReadAction
        protected void checkExpression(@Nonnull XPathExpression expr) {
            if (ExpectedTypeUtil.isExplicitConversion(expr)) {
                XPathExpression expression = ExpectedTypeUtil.unparenthesize(expr);
                assert expression != null;

                XPathType convertedType = ((XPathFunctionCall) expression).getArgumentList()[0].getType();
                if (isSameType(expression, convertedType)) {
                    XPathQuickFixFactory fixFactory = ContextProvider.getContextProvider(expression).getQuickFixFactory();
                    LocalQuickFix[] fixes = fixFactory.createRedundantTypeConversionFixes(expression);

                    addProblem(myManager.createProblemDescriptor(
                        expression,
                        "Redundant conversion to type '" + convertedType.getName() + "'",
                        myOnTheFly,
                        fixes,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    ));
                }
                else if (CHECK_ANY) {
                    XPathType expectedType = ExpectedTypeUtil.getExpectedType(expression);
                    if (expectedType == XPathType.ANY) {
                        XPathQuickFixFactory fixFactory = ContextProvider.getContextProvider(expression).getQuickFixFactory();
                        LocalQuickFix[] fixes = fixFactory.createRedundantTypeConversionFixes(expression);

                        addProblem(myManager.createProblemDescriptor(expression,
                            "Redundant conversion to type '" + expectedType.getName() + "'", myOnTheFly, fixes,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        ));
                    }
                }
            }
        }

        private boolean isSameType(XPathExpression expression, XPathType convertedType) {
            XPathType type = ExpectedTypeUtil.mapType(expression, expression.getType());
            while (type instanceof XPath2SequenceType xPath2SequenceType) {
                type = xPath2SequenceType.getType();
            }
            while (convertedType instanceof XPath2SequenceType xPath2SequenceType) {
                convertedType = xPath2SequenceType.getType();
            }
            return ExpectedTypeUtil.mapType(expression, convertedType) == type;
        }
    }
}
