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
import consulo.language.Language;
import consulo.language.editor.inspection.*;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.editor.intention.SuppressIntentionAction;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import org.intellij.lang.xpath.XPathFileType;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.psi.XPathElement;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.XPathNodeTest;
import org.intellij.lang.xpath.psi.XPathPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class XPathInspection<S> extends LocalInspectionTool implements CustomSuppressableInspectionTool {
    @Nonnull
    public String getGroupDisplayName() {
        return "General";
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return XPathFileType.XPATH.getLanguage();
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    public SuppressIntentionAction[] getSuppressActions(@Nullable PsiElement element) {
        final XPathElement e = PsiTreeUtil.getContextOfType(element, XPathElement.class, false);
        return ContextProvider.getContextProvider(e != null ? e : element).getQuickFixFactory().getSuppressActions(this);
    }

    public boolean isSuppressedFor(@Nonnull PsiElement element) {
        return ContextProvider.getContextProvider(element.getContainingFile()).getQuickFixFactory().isSuppressedFor(element, this);
    }

    protected abstract Visitor createVisitor(InspectionManager manager, ProblemsHolder holder, boolean isOnTheFly, S state);

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    @RequiredReadAction
    public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder,
                                          boolean isOnTheFly,
                                          @Nonnull LocalInspectionToolSession session,
                                          @Nonnull Object state) {
        if (!acceptsLanguage(holder.getFile().getLanguage())) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }
        return createVisitor(holder.getManager(), holder, isOnTheFly, (S) state);
    }

    protected boolean acceptsLanguage(Language language) {
        return language == XPathFileType.XPATH.getLanguage() || language == XPathFileType.XPATH2.getLanguage();
    }

    protected static abstract class Visitor<S1> extends PsiRecursiveElementVisitor {
        protected final InspectionManager myManager;
        private final ProblemsHolder myProblemsHolder;
        protected boolean myOnTheFly;
        protected S1 myState;

        public Visitor(InspectionManager manager, ProblemsHolder problemsHolder, boolean isOnTheFly, S1 state) {
            myManager = manager;
            myState = state;
            myProblemsHolder = problemsHolder;
            myOnTheFly = isOnTheFly;
        }

        public void visitElement(PsiElement psiElement) {
            super.visitElement(psiElement);

            if (psiElement instanceof XPathExpression) {
                checkExpression(((XPathExpression) psiElement));
            }
            else if (psiElement instanceof XPathNodeTest) {
                checkNodeTest(((XPathNodeTest) psiElement));
            }
            else if (psiElement instanceof XPathPredicate) {
                checkPredicate((XPathPredicate) psiElement);
            }
        }

        protected void checkExpression(XPathExpression expression) {
        }

        protected void checkPredicate(XPathPredicate predicate) {
        }

        protected void checkNodeTest(XPathNodeTest nodeTest) {
        }

        protected void addProblem(ProblemDescriptor problem) {
            myProblemsHolder.registerProblem(problem);
        }
    }
}
