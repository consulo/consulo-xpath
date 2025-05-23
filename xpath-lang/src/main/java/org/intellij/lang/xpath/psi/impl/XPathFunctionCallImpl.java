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
package org.intellij.lang.xpath.psi.impl;

import consulo.application.AllIcons;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.ItemPresentation;
import consulo.navigation.NavigationItem;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import org.intellij.lang.xpath.XPath2ElementTypes;
import org.intellij.lang.xpath.XPathTokenTypes;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.context.XPathVersion;
import org.intellij.lang.xpath.context.functions.Function;
import org.intellij.lang.xpath.psi.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.xml.namespace.QName;

public class XPathFunctionCallImpl extends XPathElementImpl implements XPathFunctionCall {

  public XPathFunctionCallImpl(ASTNode node) {
    super(node);
  }

  @Override
  @Nonnull
  public XPathExpression[] getArgumentList() {
    final ASTNode[] nodes = getNode().getChildren(XPath2ElementTypes.EXPRESSIONS);
    final XPathExpression[] expressions = new XPathExpression[nodes.length];
    for (int i = 0; i < expressions.length; i++) {
      expressions[i] = (XPathExpression)nodes[i].getPsi();
    }
    return expressions;
  }

  @Override
  public PsiElement add(@Nonnull PsiElement psiElement) throws IncorrectOperationException {
    if (psiElement instanceof XPathExpression) {
      if (getNode().getChildren(XPath2ElementTypes.EXPRESSIONS).length > 0) {
        final XPathExpression child = XPathChangeUtil.createExpression(this, "f(a,b)");
        final ASTNode comma = child.getNode().findChildByType(XPathTokenTypes.COMMA);
        assert comma != null;
        final PsiElement psi = comma.getPsi();
        assert psi != null;
        add(psi);
      }
    }
    final ASTNode paren = getNode().findChildByType(XPathTokenTypes.RPAREN);
    if (paren != null) {
      return super.addBefore(psiElement, paren.getPsi());
    }
    return super.add(psiElement);
  }

  @Override
  @Nonnull
  public String getFunctionName() {
    final ASTNode node = getNameNode();
    final String name = node != null ? node.getText() : null;
    assert name != null : unexpectedPsiAssertion();
    return name;
  }

  @Nullable
  protected ASTNode getNameNode() {
    return getNode().findChildByType(XPathTokenTypes.FUNCTION_NAME);
  }

  @Nullable
  protected ASTNode getPrefixNode() {
    return getNode().findChildByType(XPathTokenTypes.EXT_PREFIX);
  }

  @Override
  @Nonnull
  public PrefixedName getQName() {
    final ASTNode node = getNameNode();
    assert node != null : unexpectedPsiAssertion();
    return new PrefixedNameImpl(getPrefixNode(), node);
  }

  @Override
  @Nullable
  public XPathFunction resolve() {
    final Reference reference = getReference();
    return reference != null ? reference.resolve() : null;
  }

  @Override
  @Nullable
  public Reference getReference() {
    final ASTNode nameNode = getNameNode();
    if (nameNode != null) {
      return new Reference(nameNode);
    }
    return null;
  }

  @Override
  @Nonnull
  public PsiReference[] getReferences() {
    if (getPrefixNode() != null && getNameNode() != null) {
      return new PsiReference[]{
        getReference(),
        new PrefixReferenceImpl(this, getPrefixNode())
      };
    }
    return super.getReferences();
  }

  @Override
  @Nonnull
  public XPathType getType() {
    final XPathFunction f = resolve();
    if (f == null) {
      return XPathType.UNKNOWN;
    }
    final Function function = f.getDeclaration();
    return function != null ? function.getReturnType() : XPathType.UNKNOWN;
  }

  class Reference extends ReferenceBase {
    private volatile Pair<String, XPathFunction> myFunction;

    public Reference(ASTNode node) {
      super(XPathFunctionCallImpl.this, node);
    }

    @Override
    @Nullable
    public XPathFunction resolve() {
      if (myFunction != null && myFunction.first.equals(getQName().toString())) {
        return myFunction.second;
      }
      else {
        final XPathFunctionCallImpl call = XPathFunctionCallImpl.this;
        final ContextProvider contextProvider = call.getXPathContext();
        final QName name = contextProvider.getQName(call);
        if (name == null) {
          return null;
        }

        final Function functionDecl = contextProvider.getFunctionContext().resolve(name, getArgumentList().length);
        final XPathFunction impl = functionDecl == null ? null : functionDecl instanceof XPathFunction ? (XPathFunction)functionDecl : new
          FunctionImpl(functionDecl);
        return (myFunction = Pair.create(getQName().toString(), impl)).second;
      }
    }

    @Override
    @Nonnull
    public Object[] getVariants() {
      return EMPTY_ARRAY;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      final XPathFunctionCall child = (XPathFunctionCall)XPathChangeUtil.createExpression(getElement(), newElementName + "()");

      final PrefixedNameImpl newName = ((PrefixedNameImpl)child.getQName());
      final PrefixedNameImpl oldName = ((PrefixedNameImpl)getQName());

      final ASTNode localNode = newName.getLocalNode();
      getNode().replaceChild(oldName.getLocalNode(), localNode);

      final PsiElement psi = getNode().getPsi();
      assert psi != null;
      return psi;
    }

    class FunctionImpl extends LightElement implements XPathFunction, ItemPresentation, NavigationItem {
      private final Function myFunctionDecl;

      public FunctionImpl(Function functionDecl) {
        super(getElement().getManager(), getElement().getContainingFile().getLanguage());
        myFunctionDecl = functionDecl;
      }

      @Override
      public PsiElement getContext() {
        return XPathFunctionCallImpl.this;
      }

      @Override
      public String getName() {
        return myFunctionDecl != null ? myFunctionDecl.getName() : getFunctionName();
      }

      @Override
      public String toString() {
        return "Function: " + getName();
      }

      @Override
      @SuppressWarnings({"ConstantConditions"})
      public String getText() {
        return getName();
      }

      @Override
      public ItemPresentation getPresentation() {
        return this;
      }

      @Override
      @Nullable
      public Image getIcon() {
        return AllIcons.Nodes.Function;
      }

      @Override
      @Nullable
      public String getLocationString() {
        return null;
      }

      @Override
      @Nullable
      public String getPresentableText() {
        return myFunctionDecl != null ? myFunctionDecl.buildSignature() +
          ": " + myFunctionDecl.getReturnType().getName() : null;
      }

      @Override
      public void accept(@Nonnull PsiElementVisitor visitor) {
      }

      @Override
      public PsiElement copy() {
        return this;
      }

      @Override
      public PsiElement setName(@Nonnull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
      }

      @Override
      public boolean isValid() {
        return true;
      }

      @Override
      public int hashCode() {
        final String name = getName();
        return name != null ? name.hashCode() : 0;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
          return false;
        }
        final String name = ((FunctionImpl)obj).getName();
        return name != null && name.equals(getName()) || getName() == null;
      }

      @Override
      public Function getDeclaration() {
        return myFunctionDecl;
      }

      @Override
      public boolean isWritable() {
        return false;
      }

      @Override
      public boolean isPhysical() {
        // hack
        // required to prevent renaming of functions. Shouldn't IDEA check for isWritable()?
        // com.intellij.refactoring.rename.PsiElementRenameHandler:
        // if (!PsiManager.getInstance(project).isInProject(element) && element.isPhysical()) { ... }
        return true;
      }

      @Override
      public ContextProvider getXPathContext() {
        return ContextProvider.getContextProvider(getElement());
      }

      @Override
      public XPathVersion getXPathVersion() {
        return getElement().getXPathVersion();
      }

      @Override
      public void accept(XPathElementVisitor visitor) {
        visitor.visitXPathFunction(this);
      }
    }
  }

  @Override
  public void accept(XPathElementVisitor visitor) {
    visitor.visitXPathFunctionCall(this);
  }
}
