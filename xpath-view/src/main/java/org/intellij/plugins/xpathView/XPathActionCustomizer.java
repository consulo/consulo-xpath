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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.codeEditor.markup.RangeHighlighter;
import consulo.ide.impl.idea.codeInsight.hint.HintManagerImpl;
import consulo.ide.impl.idea.ui.LightweightHintImpl;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.ui.awt.HintUtil;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.Gray;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.action.*;
import consulo.ui.ex.awt.hint.LightweightHint;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.ui.ex.keymap.util.KeymapUtil;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.concurrent.coroutine.step.CodeExecution;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.xpathView.util.HighlighterUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.List;

/**
 * This extension holds the application-level configuration and registers an own handler for
 * ESC-Action to clear highlighters.<br>
 */
@ExtensionImpl
public class XPathActionCustomizer implements ActionCustomizer {
    private static final String ACTION_FIND_NEXT = "FindNext";
    private static final String ACTION_FIND_PREVIOUS = "FindPrevious";

    private AnAction nextAction;
    private AnAction prevAction;

    @Override
    public void customize(Session session) {
        nextAction = session.getAction(ACTION_FIND_NEXT);
        prevAction = session.getAction(ACTION_FIND_PREVIOUS);

        session.unregisterAction(ACTION_FIND_NEXT);
        session.unregisterAction(ACTION_FIND_PREVIOUS);
        session.registerAction(ACTION_FIND_NEXT, new MyFindAction(nextAction, false));
        session.registerAction(ACTION_FIND_PREVIOUS, new MyFindAction(prevAction, true));
    }

    class MyFindAction extends AnAction implements DumbAware, AnActionWithAsyncUpdate {
        private final AnAction origAction;
        private final boolean isPrev;
        private boolean wrapAround;

        public MyFindAction(AnAction origAction, boolean isPrev) {
            this.origAction = origAction;
            this.isPrev = isPrev;

            copyFrom(origAction);
            setEnabledInModalContext(origAction.isEnabledInModalContext());
        }

        @Override
        @RequiredUIAccess
        public void actionPerformed(@Nonnull AnActionEvent event) {
            final Editor editor = event.getData(LangDataKeys.EDITOR);
            if (editor != null) {
                if (HighlighterUtil.hasHighlighters(editor)) {
                    final int offset = editor.getCaretModel().getOffset();
                    final List<RangeHighlighter> hl = HighlighterUtil.getHighlighters(editor);
                    int diff = Integer.MAX_VALUE;
                    RangeHighlighter next = null;
                    for (RangeHighlighter highlighter : hl) {
                        if (isPrev) {
                            if (highlighter.getStartOffset() < offset && offset - highlighter.getStartOffset() < diff) {
                                diff = offset - highlighter.getStartOffset();
                                next = highlighter;
                            }
                        }
                        else {
                            if (highlighter.getStartOffset() > offset && highlighter.getStartOffset() - offset < diff) {
                                diff = highlighter.getStartOffset() - offset;
                                next = highlighter;
                            }
                        }
                    }

                    final int startOffset;
                    if (next != null) {
                        startOffset = next.getStartOffset();
                    }
                    else if (wrapAround) {
                        startOffset = hl.get(isPrev ? hl.size() - 1 : 0).getStartOffset();
                    }
                    else {
                        final String info =
                            (isPrev ? "First" : "Last") + " XPath match reached. Press " + (isPrev ? getShortcutText(prevAction) : getShortcutText(
                                nextAction)) + " to search from the" +
                                " " + (isPrev ? "bottom" : "top");

                        showEditorHint(info, editor);

                        wrapAround = true;
                        return;
                    }
                    editor.getScrollingModel().scrollTo(editor.offsetToLogicalPosition(startOffset), ScrollType.MAKE_VISIBLE);
                    editor.getCaretModel().moveToOffset(startOffset);
                    wrapAround = false;
                    return;
                }
            }
            origAction.actionPerformed(event);
        }

        @Override
        public Coroutine<?, ?> updateAsync(AnActionEvent e) {
            if (origAction instanceof AnActionWithAsyncUpdate asyncUpdate) {
                return asyncUpdate.updateAsync(e);
            }
            else if (origAction instanceof AnActionWithSyncUpdate syncUpdate) {
                return CodeExecution.run(() -> {
                    syncUpdate.update(e);
                }).toCoroutine();
            }

            return CodeExecution.run(() -> {
            }).toCoroutine();
        }

        @Override
        public boolean displayTextInToolbar() {
            return origAction.displayTextInToolbar();
        }

        @Override
        public void setDefaultIcon(boolean b) {
            origAction.setDefaultIcon(b);
        }

        @Override
        public boolean isDefaultIcon() {
            return origAction.isDefaultIcon();
        }
    }

    public static void showEditorHint(final String info, final Editor editor) {
        final JLabel label = new JLabel(info);
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, Gray._128),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        label.setForeground(JBColor.foreground());
        label.setBackground(TargetAWT.to(HintUtil.getInformationColor()));
        label.setOpaque(true);
        label.setFont(label.getFont().deriveFont(Font.BOLD));

        final LightweightHint h = new LightweightHintImpl(label);
        final Point point = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
        SwingUtilities.convertPointToScreen(point, editor.getContentComponent());

        final int flags = HintManagerImpl.HIDE_BY_ANY_KEY | HintManagerImpl.HIDE_BY_SCROLLING;
        HintManagerImpl.getInstanceImpl().showEditorHint(h, editor, point, flags, 0, false);
    }

    public static String getShortcutText(final String actionId) {
        return getShortcutText(ActionManager.getInstance().getAction(actionId));
    }

    public static String getShortcutText(final AnAction action) {
        final ShortcutSet shortcutSet = action.getShortcutSet();
        final Shortcut[] shortcuts = shortcutSet.getShortcuts();
        for (final Shortcut shortcut : shortcuts) {
            final String text = KeymapUtil.getShortcutText(shortcut);
            if (text.length() > 0) {
                return text;
            }
        }
        return ActionManager.getInstance().getId(action);
    }
}
