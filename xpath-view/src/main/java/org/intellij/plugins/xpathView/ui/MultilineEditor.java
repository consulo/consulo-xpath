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
package org.intellij.plugins.xpathView.ui;

import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorEx;
import consulo.document.Document;
import consulo.language.editor.ui.awt.EditorTextField;
import consulo.project.Project;
import consulo.ui.ex.action.*;
import consulo.virtualFileSystem.fileType.FileType;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;

public class MultilineEditor extends JPanel {

  private final EditorModel myModel;
  private EditorTextField myEditorTextField;

  public EditorTextField getField() {
    return myEditorTextField;
  }

  public interface EditorModel extends ListModel {
    int getSelectedIndex();

    void setSelectedIndex(int index);

    String getItemString(int index);

    int getSize();
  }

  private static abstract class ItemAction extends AnAction {
    public ItemAction(String id, JComponent component) {
      copyFrom(ActionManager.getInstance().getAction(id));
      registerCustomShortcutSet(getShortcutSet(), component);
    }
  }

  public MultilineEditor(Document document, Project project, FileType fileType, EditorModel model) {
    super(new BorderLayout());
    this.myModel = model;
    myEditorTextField = new EditorTextField(document, project, fileType) {
      protected EditorEx createEditor() {
        final EditorEx editor = super.createEditor();

        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
        editor.setEmbeddedIntoDialogWrapper(true);

        editor.getComponent().setPreferredSize(null);

        return editor;
      }
    };
    myEditorTextField.setOneLineMode(false);
    add(myEditorTextField, BorderLayout.CENTER);
    model.addListDataListener(new ListDataListener() {
      public void intervalAdded(ListDataEvent e) {
      }

      public void intervalRemoved(ListDataEvent e) {
      }

      public void contentsChanged(ListDataEvent e) {
        final int selectedIndex = myModel.getSelectedIndex();
        if (selectedIndex != -1) {
          myEditorTextField.setText(myModel.getItemString(selectedIndex));
        }
        else {
//                    setText("");
        }
      }
    });
    addHistoryPagers();
  }

  private void addHistoryPagers() {
    final DefaultActionGroup pagerGroup = new DefaultActionGroup();
    pagerGroup.add(new ItemAction("PreviousOccurence", this) {
      public void update(AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(myModel.getSelectedIndex() < myModel.getSize() - 1);
        presentation.setText("Previous history element");
        presentation.setDescription("Navigate to the previous history element");
      }

      public void actionPerformed(AnActionEvent e) {
        myModel.setSelectedIndex(myModel.getSelectedIndex() + 1);
        refocus();

      }
    });
    pagerGroup.add(new ItemAction("NextOccurence", this) {
      public void update(AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(myModel.getSelectedIndex() > 0);
        presentation.setText("Next history element");
        presentation.setDescription("Navigate to the next history element");
      }

      public void actionPerformed(AnActionEvent e) {
        myModel.setSelectedIndex(myModel.getSelectedIndex() - 1);
        refocus();
      }
    });
    final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("HistoryPager", pagerGroup, false);
    add(toolbar.getComponent(), BorderLayout.EAST);
  }

  private void refocus() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        final Editor editor = myEditorTextField.getEditor();
        if (editor != null) {
          editor.getContentComponent().requestFocus();
        }
        myEditorTextField.selectAll();
      }
    });
  }

}