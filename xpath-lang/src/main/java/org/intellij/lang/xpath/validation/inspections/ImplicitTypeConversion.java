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
package org.intellij.lang.xpath.validation.inspections;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.inspection.InspectionToolState;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.localize.LocalizeValue;
import consulo.ui.ex.awt.util.Alarm;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.lang.xpath.XPathFileType;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.XPathFunctionCall;
import org.intellij.lang.xpath.psi.XPathType;
import org.intellij.lang.xpath.validation.ExpectedTypeUtil;
import org.intellij.lang.xpath.validation.inspections.quickfix.XPathQuickFixFactory;

import javax.swing.*;
import java.awt.event.ItemEvent;

/**
 * @author sweinreuter
 * @since 2006-04-30
 */
// TODO: Option to flag literals: <number> = '123', <string> = 123, etc.
@ExtensionImpl
public class ImplicitTypeConversion extends XPathInspection<ImplicitTypeConversionState> {
    private static final String SHORT_NAME = "ImplicitTypeConversion";

    public ImplicitTypeConversion() {
    }

    @Nonnull
    @Override
    public InspectionToolState<?> createStateProvider() {
        return new ImplicitTypeConversionState();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Implicit Type Conversion");
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
    protected Visitor createVisitor(
        InspectionManager manager,
        ProblemsHolder holder,
        boolean isOnTheFly,
        ImplicitTypeConversionState state
    ) {
        return new MyElementVisitor(manager, holder, isOnTheFly, state);
    }

    @Nullable
    public JComponent createOptionsPanel() {
        return new Options(null);
    }

    @Override
    protected boolean acceptsLanguage(Language language) {
        return language == XPathFileType.XPATH.getLanguage();
    }

    final class MyElementVisitor extends Visitor<ImplicitTypeConversionState> {
        MyElementVisitor(
            InspectionManager manager,
            ProblemsHolder holder,
            boolean isOnTheFly,
            ImplicitTypeConversionState inspectionState
        ) {
            super(manager, holder, isOnTheFly, inspectionState);
        }

        @Override
        @RequiredReadAction
        protected void checkExpression(@Nonnull XPathExpression expression) {
            XPathType expectedType = ExpectedTypeUtil.getExpectedType(expression);
            // conversion to NODESET is impossible (at least not in a portable way) and is flagged by annotator
            if (expectedType != XPathType.NODESET && expectedType != XPathType.UNKNOWN) {
                boolean isExplicit = myState.FLAG_EXPLICIT_CONVERSION && ExpectedTypeUtil.isExplicitConversion(expression);
                checkExpressionOfType(expression, expectedType, isExplicit);
            }
        }

        @RequiredReadAction
        private void checkExpressionOfType(@Nonnull XPathExpression expression, XPathType type, boolean explicit) {
            XPathType exprType = expression.getType();
            if (exprType.isAbstract() || type.isAbstract()) {
                return;
            }

            if (exprType != type && (explicit || isCheckedConversion(exprType, type))) {
                if (explicit && exprType == XPathType.STRING && type == XPathType.BOOLEAN) {
                    XPathExpression expr = ExpectedTypeUtil.unparenthesize(expression);
                    if (expr instanceof XPathFunctionCall functionCall && myState.IGNORE_NODESET_TO_BOOLEAN_VIA_STRING
                        && functionCall.getArgumentList()[0].getType() == XPathType.NODESET) {
                        return;
                    }
                }

                LocalQuickFix[] fixes;
                if (type != XPathType.NODESET) {
                    XPathQuickFixFactory fixFactory = ContextProvider.getContextProvider(expression).getQuickFixFactory();
                    explicit = explicit && !(exprType == XPathType.STRING && type == XPathType.BOOLEAN);
                    fixes = fixFactory.createImplicitTypeConversionFixes(expression, type, explicit);
                }
                else {
                    fixes = null;
                }

                addProblem(myManager.createProblemDescriptor(expression,
                    "Expression should be of type '" + type.getName() + "'", myOnTheFly, fixes,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                ));
            }
        }

        private boolean isCheckedConversion(XPathType exprType, XPathType type) {

            if (exprType == XPathType.NODESET) {
                if (type == XPathType.STRING && myState.OPTIONS.get(0)) {
                    return true;
                }
                if (type == XPathType.NUMBER && myState.OPTIONS.get(4)) {
                    return true;
                }
                if (type == XPathType.BOOLEAN && myState.OPTIONS.get(8)) {
                    return true;
                }
            }
            else if (exprType == XPathType.STRING) {
                if (type == XPathType.NUMBER && myState.OPTIONS.get(5)) {
                    return true;
                }
                if (type == XPathType.BOOLEAN && myState.OPTIONS.get(9)) {
                    return true;
                }
            }
            else if (exprType == XPathType.NUMBER) {
                if (type == XPathType.STRING && myState.OPTIONS.get(2)) {
                    return true;
                }
                if (type == XPathType.BOOLEAN && myState.OPTIONS.get(10)) {
                    return true;
                }
            }
            else if (exprType == XPathType.BOOLEAN) {
                if (type == XPathType.STRING && myState.OPTIONS.get(3)) {
                    return true;
                }
                if (type == XPathType.NUMBER && myState.OPTIONS.get(11)) {
                    return true;
                }
            }
            return false;
        }
    }

    // TODO rewrite to new UI
    public class Options extends JPanel {
        @SuppressWarnings({"UNUSED_SYMBOL", "FieldCanBeLocal"})
        private JPanel root;
        private JCheckBox NS_S;
        private JCheckBox NS_N;
        private JCheckBox NS_B;
        private JCheckBox S_S;
        private JCheckBox S_N;
        private JCheckBox S_B;
        private JCheckBox N_S;
        private JCheckBox N_N;
        private JCheckBox N_B;
        private JCheckBox B_S;
        private JCheckBox B_N;
        private JCheckBox B_B;
        private JCheckBox myAlwaysFlagExplicitConversion;
        private JCheckBox myIgnoreNodesetToString;

        private final JCheckBox[][] matrix = new JCheckBox[][]{
            {NS_S, S_S, N_S, B_S},
            {NS_N, S_N, N_N, B_N},
            {NS_B, S_B, N_B, B_B},
        };

        public Options(ImplicitTypeConversionState state) {
            for (int i = 0; i < matrix.length; i++) {
                JCheckBox[] row = matrix[i];
                for (int j = 0; j < row.length; j++) {
                    JCheckBox to = row[j];
                    int index = row.length * i + j;
                    to.setSelected(state.OPTIONS.get(index));
                    to.addItemListener(e -> state.OPTIONS.set(index, e.getStateChange() == ItemEvent.SELECTED));
                    if (j == i + 1) {
                        to.setEnabled(false);
                    }
                }
            }
            myAlwaysFlagExplicitConversion.setSelected(state.FLAG_EXPLICIT_CONVERSION);
            myAlwaysFlagExplicitConversion.addItemListener(
                e -> state.FLAG_EXPLICIT_CONVERSION = e.getStateChange() == ItemEvent.SELECTED
            );
            myIgnoreNodesetToString.setSelected(state.IGNORE_NODESET_TO_BOOLEAN_VIA_STRING);
            myIgnoreNodesetToString.addItemListener(
                e -> state.IGNORE_NODESET_TO_BOOLEAN_VIA_STRING = e.getStateChange() == ItemEvent.SELECTED
            );
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            new Alarm(Alarm.ThreadToUse.SHARED_THREAD).addRequest(
                () -> {
                    for (int i = 0; i < matrix.length; i++) {
                        JCheckBox[] row = matrix[i];
                        for (int j = 0; j < row.length; j++) {
                            JCheckBox to = row[j];
                            to.setEnabled(enabled && j != i + 1);
                        }
                    }
                },
                200
            );
        }

        private void createUIComponents() {
            root = this;
        }
    }
}
