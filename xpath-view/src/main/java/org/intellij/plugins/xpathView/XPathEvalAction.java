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

import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.codeEditor.markup.RangeHighlighter;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorManager;
import consulo.ide.impl.idea.find.FindProgressIndicator;
import consulo.language.Language;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.template.TemplateLanguageFileViewProvider;
import consulo.logging.Logger;
import consulo.navigation.ItemPresentation;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.tree.PresentationData;
import consulo.usage.*;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.ide.highlighter.XmlFileType;
import consulo.xml.psi.xml.XmlElement;
import consulo.xml.psi.xml.XmlFile;
import consulo.xpath.view.XPathViewConfig;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.lang.xpath.XPathHighlighter;
import org.intellij.plugins.xpathView.eval.EvalExpressionDialog;
import org.intellij.plugins.xpathView.support.XPathSupport;
import org.intellij.plugins.xpathView.ui.InputExpressionDialog;
import org.intellij.plugins.xpathView.util.CachedVariableContext;
import org.intellij.plugins.xpathView.util.HighlighterUtil;
import org.intellij.plugins.xpathView.util.MyPsiUtil;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.saxpath.SAXPathException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p>This class implements the core action to enter, evaluate and display the results of an XPath expression.</p>
 * <p>
 * <p>The evaluation is performed by the <a target="_blank" href="http://www.jaxen.org">Jaxen</a> XPath-engine, which allows arbitrary
 * object models to be used. The adapter class for IDEA's object model, the PSI-tree, is located in the class
 * {@link org.intellij.plugins.xpathView.support.jaxen.PsiDocumentNavigator}.</p>
 * <p>
 * <p>The plugin can be invoked in three different ways:<ol>
 * <li>By pressing a keystroke (default: ctrl-alt-x, e) that can be set in the keymap configuration
 * <li>By selecting "Evaluate XPath" from the edtior popup menu
 * <li>By clicking the icon in the toolbar (it's the icon that is associated with xml-files on windows)
 * </ol>
 * <p>
 * <p>The result of an expression is displayed according to its type: Primitive XPath values (Strings, numbers, booleans)
 * are displayed by a message box. If the result is a node/nodelist, the corresponding nodes are highlighted in IDEA's
 * editor.</p>
 * <p>The highlighting is cleared upon each new evaluation. Additionally, the plugin registers an own handler for the
 * &lt;esc&gt; key, which also clears the highlighting.</p>
 * <p>
 * <p>The evalutation can be performed relatively to a context node: When the option "Use node at cursor as context node"
 * is turned on, all XPath expressions are evaluted relatively to this node. This node (which can actually only be a tag
 * element), is then highlighted to give a visual indication when entering the expression. This does not affect
 * expressions that start with <code>/</code> or <code>//</code>.</p>
 * <p>
 * <p><b>Limitations:</b></p>
 * <ul>
 * <li>Namespaces: Although queries containing namespace-prefixes are supported, the XPath namespace-axis
 * (<code>namespace::</code>) is currently unsupported.<br>
 * <li>Matching for text(): Such queries will currently also highlight whitespace <em>inside</em> a start/end tag.<br>
 * This is due the tree-structure of the PSI. Further investigation is needed here.
 * <li>String values with string(): Whitespace handling for the string() function is far from being correctly
 * implemented. To produce somewhat acceptable results, all whitespace inside a string is normalized.<br>
 * <em>DON'T EXPECT THESE RESULTS TO BE THE SAME AS WITH OTHER TOOLS</em>.
 * <li>Entites references: This is a limitation for matching text() as well as for the result produced by string().
 * The only recognized entity refences are the predefined ones for XML:<br>&nbsp;&nbsp;
 * &amp;amp; &amp;lt; &amp;gt; &amp;quot;<br>
 * In all other cases, the text that is returned is the text shown in the editor and does not include resolved
 * entities. Therefore you will get no/false results when entites are involved.<br>
 * It is currently undecided whether it makes sense to recurse into resolved entities, because there seems no
 * reasonable way to display the result.
 * <li><b>This plugin is completely based on IDEA's PSI (Program Structure Interface)</b>. This API is not part of the
 * current Open-API and is completely unsupported by IntelliJ. Interfaces and functionality and may be changed
 * without any prior notice, which might break this plugin.<br>
 * <em>Please don't bother IntelliJ staff in such a case</em>.
 * <li>Probably some others ;-)
 * </ul>
 *
 * @author Sascha Weinreuter
 */
public class XPathEvalAction extends XPathAction {
    private static final Logger LOG = Logger.getInstance(XPathEvalAction.class);

    @Override
    protected void updateToolbar(AnActionEvent event) {
        super.updateToolbar(event);
        event.getPresentation().setIcon(XmlFileType.INSTANCE.getIcon());
    }

    @Override
    protected boolean isEnabledAt(XmlFile xmlFile, int offset) {
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(Project.KEY);
        if (project == null) {
            // no active project
            LOG.debug("No project");
            return;
        }

        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            FileEditorManager fem = FileEditorManager.getInstance(project);
            editor = fem.getSelectedTextEditor();
        }
        if (editor == null) {
            // no editor available
            LOG.debug("No editor");
            return;
        }

        // do we have an xml file?
        final PsiDocumentManager pdm = PsiDocumentManager.getInstance(project);
        final PsiFile psiFile = pdm.getPsiFile(editor.getDocument());
        if (!(psiFile instanceof XmlFile)) {
            // not xml
            LOG.debug("No XML-File: " + psiFile);
            return;
        }

        // make sure PSI is in sync with document
        pdm.commitDocument(editor.getDocument());

        execute(editor);
    }

    private void execute(Editor editor) {
        final Project project = editor.getProject();
        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return;
        }

        InputExpressionDialog.Context input;
        XmlElement contextNode = null;
        final Config cfg = XPathViewConfig.getInstance().getState();
        do {
            RangeHighlighter contextHighlighter = null;
            if (cfg.isUseContextAtCursor()) {
                // find out current context node
                contextNode = MyPsiUtil.findContextNode(psiFile, editor);
                if (contextNode != null) {
                    contextHighlighter = HighlighterUtil.highlightNode(editor, contextNode, XPathHighlighter.XPATH_EVAL_CONTEXT_HIGHLIGHT, cfg);
                }
            }
            if (contextNode == null) {
                // in XPath data model, / is the document itself, including comments, PIs and the root element
                contextNode = ((XmlFile) psiFile).getDocument();
                if (contextNode == null) {
                    FileViewProvider fileViewProvider = psiFile.getViewProvider();
                    if (fileViewProvider instanceof TemplateLanguageFileViewProvider) {
                        Language dataLanguage = ((TemplateLanguageFileViewProvider) fileViewProvider).getTemplateDataLanguage();
                        PsiFile templateDataFile = fileViewProvider.getPsi(dataLanguage);
                        if (templateDataFile instanceof XmlFile) {
                            contextNode = ((XmlFile) templateDataFile).getDocument();
                        }
                    }
                }
            }

            input = inputXPathExpression(project, contextNode);
            if (contextHighlighter != null) {
                contextHighlighter.dispose();
            }
            if (input == null) {
                return;
            }

            HighlighterUtil.clearHighlighters(editor);
        }
        while (contextNode != null && evaluateExpression(input, contextNode, editor, cfg));
    }

    private boolean evaluateExpression(EvalExpressionDialog.Context context, XmlElement contextNode, Editor editor, Config cfg) {
        final Project project = editor.getProject();

        try {
            final XPathSupport support = XPathSupport.getInstance();
            final XPath xpath = support.createXPath((XmlFile) contextNode.getContainingFile(), context.input.expression, context.input.namespaces);

            xpath.setVariableContext(new CachedVariableContext(context.input.variables, xpath, contextNode));

            // evaluate the expression on the whole document
            final Object result = xpath.evaluate(contextNode);
            LOG.debug("result = " + result);
            LOG.assertTrue(result != null, "null result?");

            if (result instanceof List<?>) {
                final List<?> list = (List<?>) result;
                if (!list.isEmpty()) {
                    if (cfg.HIGHLIGHT_RESULTS) {
                        highlightResult(contextNode, editor, list);
                    }
                    if (cfg.SHOW_USAGE_VIEW) {
                        showUsageView(editor, xpath, contextNode, list);
                    }
                    if (!cfg.SHOW_USAGE_VIEW && !cfg.HIGHLIGHT_RESULTS) {
                        final String s = StringUtil.pluralize("match", list.size());
                        Messages.showInfoMessage(project, "Expression produced " + list.size() + " " + s, "XPath Result");
                    }
                }
                else {
                    return Messages.showOkCancelDialog(project,
                        "Sorry, your expression did not return any result",
                        "XPath Result",
                        "OK",
                        "Edit Expression",
                        Messages.getInformationIcon()) == 1;
                }
            }
            else if (result instanceof String) {
                Messages.showMessageDialog("'" + result.toString() + "'", "XPath result (String)", Messages.getInformationIcon());
            }
            else if (result instanceof Number) {
                Messages.showMessageDialog(result.toString(), "XPath result (Number)", Messages.getInformationIcon());
            }
            else if (result instanceof Boolean) {
                Messages.showMessageDialog(result.toString(), "XPath result (Boolean)", Messages.getInformationIcon());
            }
            else {
                LOG.error("Unknown XPath result: " + result);
            }
        }
        catch (XPathSyntaxException e) {
            LOG.debug(e);
            // TODO: Better layout of the error message with non-fixed size fonts
            return Messages.showOkCancelDialog(project,
                e.getMultilineMessage(),
                "XPath syntax error",
                "Edit Expression",
                "Cancel",
                Messages.getErrorIcon()) == 0;
        }
        catch (SAXPathException e) {
            LOG.debug(e);
            Messages.showMessageDialog(project, e.getMessage(), "XPath error", Messages.getErrorIcon());
        }
        return false;
    }

    private void showUsageView(final Editor editor, final XPath xPath, final XmlElement contextNode, final List<?> result) {
        final Project project = editor.getProject();

        //noinspection unchecked
        final List<?> _result = new ArrayList(result);
        final Supplier<UsageSearcher> searcherFactory = new Supplier<UsageSearcher>() {
            @Override
            public UsageSearcher get() {
                return new MyUsageSearcher(_result, xPath, contextNode);
            }
        };
        final MyUsageTarget usageTarget = new MyUsageTarget(xPath.toString(), contextNode);

        showUsageView(project, usageTarget, searcherFactory, new EditExpressionAction() {
            final Config config = XPathViewConfig.getInstance().getState();

            @Override
            protected void execute() {
                config.OPEN_NEW_TAB = false;
                XPathEvalAction.this.execute(editor);
            }

            @Override
            protected Object saveState() {
                return config.OPEN_NEW_TAB;
            }

            @Override
            protected void restoreState(Object o) {
                if (!config.OPEN_NEW_TAB) {
                    config.OPEN_NEW_TAB = Boolean.TRUE.equals(o);
                }
            }
        });
    }

    public static void showUsageView(final Project project,
                                     MyUsageTarget usageTarget,
                                     Supplier<UsageSearcher> searcherFactory,
                                     final EditExpressionAction editAction) {
        final UsageViewPresentation presentation = new UsageViewPresentation();
        presentation.setTargetsNodeText("Expression");
        presentation.setCodeUsages(false);
        presentation.setCodeUsagesString("Result");
        presentation.setNonCodeUsagesString("Result");
        presentation.setUsagesString("XPath Result");
        presentation.setUsagesWord("match");
        presentation.setTabText("XPath");
        presentation.setScopeText("XML Files");

        presentation.setOpenInNewTab(XPathViewConfig.getInstance().getState().OPEN_NEW_TAB);

        final FindUsagesProcessPresentation processPresentation = new FindUsagesProcessPresentation(new UsageViewPresentation());
        processPresentation.setProgressIndicatorFactory(new Supplier<ProgressIndicator>() {
            @Override
            public ProgressIndicator get() {
                return new FindProgressIndicator(project, "XML Document(s)");
            }
        });
        processPresentation.setShowPanelIfOnlyOneUsage(true);
        processPresentation.setShowNotFoundMessage(true);
        final UsageTarget[] usageTargets = {usageTarget};

        UsageViewManager.getInstance(project)
            .searchAndShowUsages(usageTargets,
                searcherFactory,
                processPresentation,
                presentation,
                new UsageViewManager.UsageViewStateListener() {
                    @Override
                    public void usageViewCreated(@Nonnull UsageView usageView) {
                        usageView.addButtonToLowerPane(editAction, "&Edit Expression");
                    }

                    @Override
                    public void findingUsagesFinished(UsageView usageView) {
                    }
                });
    }

    /**
     * Opens an input box to input an XPath expression. The box will have a history dropdown from which
     * previously entered expressions can be selected.
     *
     * @param project The project to take the history from
     * @return The expression or <code>null</code> if the user hits the cancel button
     */
    @Nullable
    private EvalExpressionDialog.Context inputXPathExpression(final Project project, XmlElement contextNode) {
        final XPathProjectComponent pc = XPathProjectComponent.getInstance(project);
        LOG.assertTrue(pc != null);

        // get expression history from project component
        final HistoryElement[] history = pc.getHistory();

        final EvalExpressionDialog dialog = new EvalExpressionDialog(project, XPathViewConfig.getInstance().getState(), history);
        if (!dialog.show(contextNode)) {
            // cancel
            LOG.debug("Input canceled");
            return null;
        }

        final InputExpressionDialog.Context context = dialog.getContext();
        LOG.debug("expression = " + context.input.expression);

        pc.addHistory(context.input);

        return context;
    }

    /**
     * <p>Process the result of an XPath query.</p>
     * <p>If the result is a <code>java.util.List</code> object, iterate over all elements and
     * add a highlighter object in the editor if the element is of type <code>PsiElement</code>.
     * <p>If the result is a primitive value (String, Number, Boolean) a message box displaying
     * the value will be displayed. </p>
     *
     * @param editor The editor object to apply the highlighting to
     */
    private void highlightResult(XmlElement contextNode, @Nonnull final Editor editor, final List<?> list) {
        final Config cfg = XPathViewConfig.getInstance().getState();
        int lowestOffset = Integer.MAX_VALUE;

        for (final Object o : list) {
            LOG.assertTrue(o != null, "null element?");

            if (o instanceof PsiElement) {
                final PsiElement element = (PsiElement) o;

                if (element.getContainingFile() == contextNode.getContainingFile()) {
                    lowestOffset = highlightElement(editor, element, cfg, lowestOffset);
                }
            }
            else {
                LOG.info("Don't know what to do with " + o + " in a list context");
            }
            LOG.debug("o = " + o);
        }

        if (cfg.isScrollToFirst() && lowestOffset != Integer.MAX_VALUE) {
            editor.getScrollingModel().scrollTo(editor.offsetToLogicalPosition(lowestOffset), ScrollType.MAKE_VISIBLE);
            editor.getCaretModel().moveToOffset(lowestOffset);
        }
    }

    private static int highlightElement(Editor editor, PsiElement element, Config cfg, int offset) {
        final RangeHighlighter highlighter = HighlighterUtil.highlightNode(editor, element, XPathHighlighter.XPATH_EVAL_HIGHLIGHT, cfg);
        HighlighterUtil.addHighlighter(editor, highlighter);

        return Math.min(highlighter.getStartOffset(), offset);
    }

    public static class MyUsageTarget implements UsageTarget {
        private final ItemPresentation myItemPresentation;
        private final XmlElement myContextNode;

        public MyUsageTarget(String expression, XmlElement contextNode) {
            myContextNode = contextNode;
            myItemPresentation = new PresentationData(expression, null, null, null);
        }

        @Override
        public void findUsages() {
            throw new IllegalArgumentException();
        }

        @Override
        public void findUsagesInEditor(@Nonnull FileEditor editor) {
            throw new IllegalArgumentException();
        }

        @Override
        public void highlightUsages(@Nonnull PsiFile file, @Nonnull Editor editor, boolean clearHighlights) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isValid() {
            // re-run will become unavailable if the context node is invalid
            return myContextNode == null || myContextNode.isValid();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        @Nullable
        public VirtualFile[] getFiles() {
            return null;
        }

        @Override
        public void update() {
        }

        @Override
        public String getName() {
            return "Expression";
        }

        @Override
        public ItemPresentation getPresentation() {
            return myItemPresentation;
        }

        @Override
        public void navigate(boolean requestFocus) {
        }

        @Override
        public boolean canNavigate() {
            return false;
        }

        @Override
        public boolean canNavigateToSource() {
            return false;
        }
    }

    private static class MyUsageSearcher implements UsageSearcher {
        private final List<?> myResult;
        private final XPath myXPath;
        private final XmlElement myContextNode;

        public MyUsageSearcher(List<?> result, XPath xPath, XmlElement contextNode) {
            myResult = result;
            myXPath = xPath;
            myContextNode = contextNode;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void generate(final Predicate<Usage> processor) {
            Runnable runnable = () -> {
                final List<?> list;
                if (myResult.isEmpty()) {
                    try {
                        list = (List<?>) myXPath.selectNodes(myContextNode);
                    }
                    catch (JaxenException e) {
                        LOG.debug(e);
                        Messages.showMessageDialog(myContextNode.getProject(), e.getMessage(), "XPath error", Messages.getErrorIcon());
                        return;
                    }
                }
                else {
                    list = myResult;
                }

                final int size = list.size();
                final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                indicator.setText("Collecting matches...");

                Collections.sort(list, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        indicator.checkCanceled();
                        if (o1 instanceof PsiElement && o2 instanceof PsiElement) {
                            return ((PsiElement) o1).getTextRange().getStartOffset() - ((PsiElement) o2).getTextRange().getStartOffset();
                        }
                        else {
                            return String.valueOf(o1).compareTo(String.valueOf(o2));
                        }
                    }
                });
                for (int i = 0; i < size; i++) {
                    indicator.checkCanceled();
                    Object o = list.get(i);
                    if (o instanceof PsiElement) {
                        final PsiElement element = (PsiElement) o;
                        processor.test(new UsageInfo2UsageAdapter(new UsageInfo(element)));
                        indicator.setText2(element.getContainingFile().getName());
                    }
                    indicator.setFraction(i / (double) size);
                }
                list.clear();
            };
            ApplicationManager.getApplication().runReadAction(runnable);
        }
    }

    public abstract static class EditExpressionAction implements Runnable {
        @Override
        public void run() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final Object o = saveState();
                    try {
                        execute();
                    }
                    finally {
                        restoreState(o);
                    }
                }

            };
            SwingUtilities.invokeLater(runnable);
        }

        protected abstract void execute();

        protected abstract Object saveState();

        protected abstract void restoreState(Object o);
    }
}
