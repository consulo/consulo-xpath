/*
 * Copyright 2002-2005 Sascha Weinreuter
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
package org.intellij.plugins.xpathView;

import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionPlaces;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.xml.lang.dtd.DTDLanguage;
import consulo.xml.psi.xml.XmlFile;
import consulo.xpath.view.XPathViewConfig;

import jakarta.annotation.Nonnull;

public abstract class XPathAction extends AnAction {
  protected XPathAction() {
  }

  @RequiredUIAccess
  public void update(@Nonnull AnActionEvent event) {
    super.update(event);
    final Presentation presentation = event.getPresentation();

    // keep track of enabled status
    presentation.setEnabled(isEnabled(event, true));

    // provide icon for toolbar
    if (ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace())) {
      updateToolbar(event);
    }
    else if (ActionPlaces.MAIN_MENU.equals(event.getPlace())) {
      updateMainMenu(event);
    }
    else if (ActionPlaces.EDITOR_POPUP.equals(event.getPlace())) {
      presentation.setVisible(presentation.isEnabled());
    }
  }

  protected void updateMainMenu(AnActionEvent event) {
    final boolean b = XPathViewConfig.getInstance().getState().SHOW_IN_MAIN_MENU;
    event.getPresentation().setVisible(b && isEnabled(event, false));
  }

  protected void updateToolbar(AnActionEvent event) {
    event.getPresentation().setVisible(XPathViewConfig.getInstance().getState().SHOW_IN_TOOLBAR);
  }

  protected boolean isEnabled(AnActionEvent event, boolean checkAvailable) {
    final Project project = event.getData(Project.KEY);
    if (project == null) {
      // no active project
      return false;
    }

    Editor editor = event.getData(PlatformDataKeys.EDITOR);
    if (editor == null) {
      FileEditorManager fem = FileEditorManager.getInstance(project);
      editor = fem.getSelectedTextEditor();
    }
    if (editor == null) {
      return false;
    }

    // do we have an xml file?
    final PsiDocumentManager cem = PsiDocumentManager.getInstance(project);
    final PsiFile psiFile = cem.getPsiFile(editor.getDocument());
    // this is also true for DTD documents...
    if (!(psiFile instanceof XmlFile)) {
      return false;
    }
    else if (psiFile.getLanguage() == DTDLanguage.INSTANCE) {
      return false;
    }

    return !checkAvailable || isEnabledAt((XmlFile)psiFile, editor.getCaretModel().getOffset());
  }

  protected abstract boolean isEnabledAt(XmlFile xmlFile, int offset);
}
