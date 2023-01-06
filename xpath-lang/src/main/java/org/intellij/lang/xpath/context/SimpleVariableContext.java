package org.intellij.lang.xpath.context;

import consulo.language.editor.intention.IntentionAction;
import consulo.language.psi.PsiElement;
import org.intellij.lang.xpath.psi.XPathVariable;
import org.intellij.lang.xpath.psi.XPathVariableReference;

import javax.annotation.Nonnull;

/*
 * Created by IntelliJ IDEA.
 * User: sweinreuter
 * Date: 08.01.2008
 */
public abstract class SimpleVariableContext implements VariableContext<String> {

  public XPathVariable resolve(XPathVariableReference reference) {
    return null;
  }

  public boolean canResolve() {
    return false;
  }

  @Nonnull
  public IntentionAction[] getUnresolvedVariableFixes(XPathVariableReference reference) {
    return IntentionAction.EMPTY_ARRAY;
  }

  public boolean isReferenceTo(PsiElement element, XPathVariableReference reference) {
    return false;
  }
}