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
import consulo.codeEditor.markup.RangeHighlighter;
import consulo.document.Document;
import consulo.ide.impl.idea.codeInsight.hint.HintManagerImpl;
import consulo.ide.impl.idea.ui.LightweightHintImpl;
import consulo.language.editor.LangDataKeys;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionPlaces;
import consulo.ui.ex.action.ActionsBundle;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.awt.CopyPasteManager;
import consulo.ui.ex.awt.NonOpaquePanel;
import consulo.ui.ex.awt.hint.HintHint;
import consulo.ui.ex.awt.hint.LightweightHint;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.xml.psi.xml.XmlElement;
import consulo.xml.psi.xml.XmlFile;
import consulo.xpath.view.XPathViewConfig;
import org.intellij.lang.xpath.XPathHighlighter;
import org.intellij.plugins.xpathView.support.XPathSupport;
import org.intellij.plugins.xpathView.util.HighlighterUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ShowXPathAction extends XPathAction {
    @Override
    public void update(AnActionEvent event) {
        super.update(event);

        final Presentation presentation = event.getPresentation();
        if (ActionPlaces.MAIN_MENU.equals(event.getPlace()) && presentation.getText().startsWith("Show ")) {
            final String text = presentation.getText().substring("Show ".length());
            presentation.setText(Character.toUpperCase(text.charAt(0)) + text.substring(1));
        }
    }

    @Override
    @RequiredUIAccess
    protected boolean isEnabledAt(XmlFile xmlFile, int offset) {
        final PsiElement element = xmlFile.findElementAt(offset);
        if (!(element instanceof XmlElement || element instanceof PsiWhiteSpace)) {
            return false;
        }

        final PsiElement node = XPathExpressionGenerator.transformToValidShowPathNode(element);
        return node != null;
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getData(LangDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        final Project project = editor.getProject();
        if (project == null) {
            return;
        }
        final PsiDocumentManager docmgr = PsiDocumentManager.getInstance(project);
        final Document document = editor.getDocument();
        docmgr.commitDocument(document);

        final PsiFile psiFile = docmgr.getPsiFile(document);
        if (!(psiFile instanceof XmlFile)) {
            return;
        }

        final PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
        if (!(element instanceof XmlElement || element instanceof PsiWhiteSpace)) {
            XPathAppComponent.showEditorHint("No suitable context for an XPath-expression selected.", editor);
            return;
        }

        final PsiElement node = XPathExpressionGenerator.transformToValidShowPathNode(element);
        if (node == null) {
            XPathAppComponent.showEditorHint("No suitable context for an XPath-expression selected.", editor);
            return;
        }

        final Config cfg = XPathViewConfig.getInstance().getState();
        final RangeHighlighter h = HighlighterUtil.highlightNode(editor, node, XPathHighlighter.XPATH_EVAL_CONTEXT_HIGHLIGHT, cfg);

        final String path = XPathSupport.getInstance().getUniquePath((XmlElement) node, null);

        final JTextField label = new JTextField(path);
        label.setPreferredSize(new Dimension(label.getPreferredSize().width + new JLabel("M").getPreferredSize().width,
            label.getPreferredSize().height));
        label.setOpaque(false);
        label.setEditable(false);
        label.setBorder(null);
        label.setHorizontalAlignment(JTextField.CENTER);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        final JPanel p = new NonOpaquePanel(new BorderLayout());
        final JLabel l = new JLabel("XPath:");
        p.add(l, BorderLayout.WEST);
        p.add(label, BorderLayout.CENTER);

        JButton button = new JButton(ActionsBundle.message("action.EditorCopy.text"), TargetAWT.to(PlatformIconGroup.actionsCopy()));
        button.putClientProperty("JButton.buttonType", "borderless");
        button.addActionListener(e1 -> CopyPasteManager.getInstance().setContents(new StringSelection(path)));

        p.add(button, BorderLayout.EAST);

        final LightweightHint hint = new LightweightHintImpl(p) {
            @Override
            public void hide() {
                super.hide();
                HighlighterUtil.removeHighlighter(editor, h);
            }
        };

        final Point point = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
        point.y += editor.getLineHeight() / 2;
        HintHint hintHint =
            new HintHint(editor.getComponent(), point).setAwtTooltip(true).setContentActive(true).setExplicitClose(true).setShowImmediately(true);
        HintManagerImpl.getInstanceImpl().showEditorHint(hint, editor, point, HintManagerImpl.HIDE_BY_ANY_KEY, 0, false, hintHint);
    }
}