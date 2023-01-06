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

import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.inspection.CustomSuppressableInspectionTool;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.editor.intention.SuppressIntentionAction;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.SmartList;
import org.intellij.lang.xpath.XPathFileType;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.psi.XPathElement;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.XPathNodeTest;
import org.intellij.lang.xpath.psi.XPathPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class XPathInspection extends LocalInspectionTool implements CustomSuppressableInspectionTool {

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

    protected abstract Visitor createVisitor(InspectionManager manager, boolean isOnTheFly);

    @Nullable
    public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull InspectionManager manager, boolean isOnTheFly) {
        final Language language = file.getLanguage();
        if (!acceptsLanguage(language)) return null;

        final Visitor visitor = createVisitor(manager, isOnTheFly);

        file.accept(visitor);

        return visitor.getProblems();
    }

  protected abstract boolean acceptsLanguage(Language language);

  protected static abstract class Visitor extends PsiRecursiveElementVisitor {
        protected final InspectionManager myManager;
      protected boolean myOnTheFly;
      private SmartList<ProblemDescriptor> myProblems;

      public Visitor(InspectionManager manager, boolean isOnTheFly) {
            myManager = manager;
          this.myOnTheFly = isOnTheFly;
        }

        public void visitElement(PsiElement psiElement) {
            super.visitElement(psiElement);

            if (myProblems != null) {
                final TextRange textRange = psiElement.getTextRange();
                for (ProblemDescriptor problem : myProblems) {
                    if (textRange.contains(problem.getPsiElement().getTextRange())) {
                        return;
                    }
                }
            }

            if (psiElement instanceof XPathExpression) {
                checkExpression(((XPathExpression)psiElement));
            } else if (psiElement instanceof XPathNodeTest) {
                checkNodeTest(((XPathNodeTest)psiElement));
            } else if (psiElement instanceof XPathPredicate) {
                checkPredicate((XPathPredicate)psiElement);
            }
        }

        protected void checkExpression(XPathExpression expression) { }

        protected void checkPredicate(XPathPredicate predicate) { }

        protected void checkNodeTest(XPathNodeTest nodeTest) { }

        @Nullable
        public ProblemDescriptor[] getProblems() {
            return myProblems == null ? null : myProblems.toArray(new ProblemDescriptor[myProblems.size()]);
        }

        protected void addProblem(ProblemDescriptor problem) {
            if (myProblems == null) {
                myProblems = new SmartList<ProblemDescriptor>();
            }
            myProblems.add(problem);
        }
    }
}
