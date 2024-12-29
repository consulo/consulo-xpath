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
 * Date: 11.04.2006
 * Time: 00:14:22
 */
package org.intellij.lang.xpath.validation.inspections.quickfix;

import consulo.codeEditor.Editor;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.editor.intention.SuppressIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import org.intellij.lang.xpath.psi.XPathExpression;
import org.intellij.lang.xpath.psi.XPathNodeTest;
import org.intellij.lang.xpath.psi.XPathType;
import org.intellij.lang.xpath.validation.inspections.XPathInspection;

import jakarta.annotation.Nonnull;

public interface XPathQuickFixFactory {
  Fix<XPathExpression>[] createImplicitTypeConversionFixes(XPathExpression expression, XPathType type, boolean explicit);

  Fix<XPathExpression>[] createRedundantTypeConversionFixes(XPathExpression expression);

  Fix<XPathNodeTest>[] createUnknownNodeTestFixes(XPathNodeTest test);

  SuppressIntentionAction[] getSuppressActions(XPathInspection inspection);

  boolean isSuppressedFor(PsiElement element, XPathInspection inspection);

  abstract class Fix<E extends PsiElement> extends LocalQuickFixAndIntentionActionOnPsiElement {
    protected Fix(E element) {
      super(element);
    }

    public boolean startInWriteAction() {
      return true;
    }

    @Override
    public boolean isAvailable(@Nonnull Project project,
                               @Nonnull PsiFile file,
                               @Nonnull PsiElement startElement,
                               @Nonnull PsiElement endElement) {
      return startElement.isValid() && startElement.getParent().isValid();
    }

    @Override
    public void invoke(@Nonnull Project project,
                       @Nonnull PsiFile file,
                       Editor editor, @Nonnull PsiElement startElement,
                       @Nonnull PsiElement endElement) {
      if (!FileModificationService.getInstance().prepareFileForWrite(file)) {
        return;
      }

      try {
        invokeImpl(project, file);
      }
      catch (IncorrectOperationException e) {
        Logger.getInstance(getClass().getName()).error(e);
      }
    }


    protected abstract void invokeImpl(Project project, PsiFile file) throws IncorrectOperationException;
  }
}
