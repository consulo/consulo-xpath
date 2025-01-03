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

package org.intellij.plugins.xpathView.search;

import consulo.application.progress.ProgressIndicator;
import consulo.ide.impl.idea.find.FindProgressIndicator;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.Messages;
import consulo.usage.*;
import consulo.xpath.view.XPathViewConfig;
import org.intellij.plugins.xpathView.Config;
import org.intellij.plugins.xpathView.XPathEvalAction;
import org.intellij.plugins.xpathView.XPathProjectComponent;
import org.intellij.plugins.xpathView.support.XPathSupport;
import org.intellij.plugins.xpathView.ui.InputExpressionDialog;
import org.jaxen.JaxenException;
import org.jaxen.XPathSyntaxException;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.function.Supplier;

public class FindByXPathAction extends AnAction {
  public void update(AnActionEvent e) {
    final Project project = e.getData(Project.KEY);
    e.getPresentation().setEnabled(project != null);
  }

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(Project.KEY);
    final Module module = e.getData(Module.KEY);

    if (project != null) {
      executeSearch(project, module);
    }
  }

  private void executeSearch(final Project project, final Module module) {
    final Config settings = XPathViewConfig.getInstance().getState();
    final XPathProjectComponent projectComponent = XPathProjectComponent.getInstance(project);

    final FindByExpressionDialog dlg = new FindByExpressionDialog(project, settings, projectComponent.getFindHistory(), module);

    if (!dlg.show(null)) {
      return;
    }

    final SearchScope scope = dlg.getScope();
    settings.MATCH_RECURSIVELY = dlg.isMatchRecursively();
    settings.SEARCH_SCOPE = dlg.getScope();

    final InputExpressionDialog.Context context = dlg.getContext();
    projectComponent.addFindHistory(context.input);

    final String expression = context.input.expression;
    if (!validateExpression(project, expression)) {
      return;
    }

    final UsageViewPresentation presentation = new UsageViewPresentation();
    presentation.setTargetsNodeText(settings.MATCH_RECURSIVELY ? "Pattern" : "Expression");
    presentation.setCodeUsages(false);
    presentation.setCodeUsagesString("Result");
    presentation.setNonCodeUsagesString("Result");
    presentation.setUsagesString("XPath Result");
    presentation.setUsagesWord("match");
    presentation.setTabText("XPath");
    presentation.setScopeText(scope.getName());

    presentation.setOpenInNewTab(settings.OPEN_NEW_TAB);

    final FindUsagesProcessPresentation processPresentation = new FindUsagesProcessPresentation(new UsageViewPresentation());
    processPresentation.setProgressIndicatorFactory(new Supplier<ProgressIndicator>() {
      public ProgressIndicator get() {
        return new FindProgressIndicator(project, scope.getName());
      }
    });
    processPresentation.setShowPanelIfOnlyOneUsage(true);
    processPresentation.setShowNotFoundMessage(true);

    final XPathEvalAction.MyUsageTarget usageTarget = new XPathEvalAction.MyUsageTarget(context.input.expression, null);
    final UsageTarget[] usageTargets = new UsageTarget[]{usageTarget};

    final Supplier<UsageSearcher> searcherFactory = new Supplier<UsageSearcher>() {
      public UsageSearcher get() {
        return new XPathUsageSearcher(project, context.input, scope, settings.MATCH_RECURSIVELY);
      }
    };
    final UsageViewManager.UsageViewStateListener stateListener = new UsageViewManager.UsageViewStateListener() {
      public void usageViewCreated(@Nonnull UsageView usageView) {
        usageView.addButtonToLowerPane(new MyEditExpressionAction(project, module), "&Edit Expression");
      }

      public void findingUsagesFinished(UsageView usageView) {
      }
    };
    UsageViewManager.getInstance(project)
                    .searchAndShowUsages(usageTargets, searcherFactory, processPresentation, presentation, stateListener);
  }

  private class MyEditExpressionAction extends XPathEvalAction.EditExpressionAction {
    private final Project myProject;
    private final Module myModule;
    private final Config myConfig = XPathViewConfig.getInstance().getState();

    public MyEditExpressionAction(Project project, Module module) {
      myProject = project;
      myModule = module;
    }

    protected void execute() {
      myConfig.OPEN_NEW_TAB = false;
      executeSearch(myProject, myModule);
    }

    protected Object saveState() {
      return myConfig.OPEN_NEW_TAB;
    }

    protected void restoreState(Object o) {
      if (!myConfig.OPEN_NEW_TAB) {
        myConfig.OPEN_NEW_TAB = Boolean.TRUE.equals(o);
      }
    }
  }

  private static boolean validateExpression(Project project, String expression) {
    try {
      //noinspection unchecked
      XPathSupport.getInstance().createXPath(null, expression, Collections.<org.intellij.plugins.xpathView.util.Namespace>emptyList());
      return true;
    }
    catch (XPathSyntaxException e) {
      Messages.showErrorDialog(project, e.getMultilineMessage(), "XPath Syntax Error");
    }
    catch (JaxenException e) {
      Messages.showErrorDialog(project, e.getMessage(), "XPath Error");
    }
    return false;
  }
}
