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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.NavigationItem;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import org.intellij.lang.xpath.XPath2ElementTypes;
import org.intellij.lang.xpath.XPathTokenTypes;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.context.XPathVersion;
import org.intellij.lang.xpath.context.functions.Function;
import org.intellij.lang.xpath.psi.*;

import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Objects;

public class XPathFunctionCallImpl extends XPathElementImpl implements XPathFunctionCall {
    public XPathFunctionCallImpl(ASTNode node) {
        super(node);
    }

    @Override
    @Nonnull
    public XPathExpression[] getArgumentList() {
        ASTNode[] nodes = getNode().getChildren(XPath2ElementTypes.EXPRESSIONS);
        XPathExpression[] expressions = new XPathExpression[nodes.length];
        for (int i = 0; i < expressions.length; i++) {
            expressions[i] = (XPathExpression) nodes[i].getPsi();
        }
        return expressions;
    }

    @Override
    @RequiredWriteAction
    public PsiElement add(@Nonnull PsiElement psiElement) throws IncorrectOperationException {
        if (psiElement instanceof XPathExpression) {
            if (getNode().getChildren(XPath2ElementTypes.EXPRESSIONS).length > 0) {
                XPathExpression child = XPathChangeUtil.createExpression(this, "f(a,b)");
                ASTNode comma = child.getNode().findChildByType(XPathTokenTypes.COMMA);
                assert comma != null;
                PsiElement psi = comma.getPsi();
                assert psi != null;
                add(psi);
            }
        }
        ASTNode paren = getNode().findChildByType(XPathTokenTypes.RPAREN);
        if (paren != null) {
            return super.addBefore(psiElement, paren.getPsi());
        }
        return super.add(psiElement);
    }

    @Override
    @Nonnull
    public String getFunctionName() {
        ASTNode node = getNameNode();
        String name = node != null ? node.getText() : null;
        assert name != null : unexpectedPsiAssertion();
        return name;
    }

    protected @Nullable ASTNode getNameNode() {
        return getNode().findChildByType(XPathTokenTypes.FUNCTION_NAME);
    }

    protected @Nullable ASTNode getPrefixNode() {
        return getNode().findChildByType(XPathTokenTypes.EXT_PREFIX);
    }

    @Override
    @Nonnull
    public PrefixedName getQName() {
        ASTNode node = getNameNode();
        assert node != null : unexpectedPsiAssertion();
        return new PrefixedNameImpl(getPrefixNode(), node);
    }

    @Override
    @RequiredReadAction
    public @Nullable XPathFunction resolve() {
        Reference reference = getReference();
        return reference != null ? reference.resolve() : null;
    }

    @Override
    public @Nullable Reference getReference() {
        ASTNode nameNode = getNameNode();
        return nameNode != null ? new Reference(nameNode) : null;
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
    @RequiredReadAction
    public XPathType getType() {
        XPathFunction f = resolve();
        if (f == null) {
            return XPathType.UNKNOWN;
        }
        Function function = f.getDeclaration();
        return function != null ? function.getReturnType() : XPathType.UNKNOWN;
    }

    class Reference extends ReferenceBase {
        private volatile Pair<String, XPathFunction> myFunction;

        public Reference(ASTNode node) {
            super(XPathFunctionCallImpl.this, node);
        }

        @Override
        @RequiredReadAction
        public @Nullable XPathFunction resolve() {
            if (myFunction != null && myFunction.first.equals(getQName().toString())) {
                return myFunction.second;
            }
            else {
                XPathFunctionCallImpl call = XPathFunctionCallImpl.this;
                ContextProvider contextProvider = call.getXPathContext();
                QName name = contextProvider.getQName(call);
                if (name == null) {
                    return null;
                }

                Function functionDecl = contextProvider.getFunctionContext().resolve(name, getArgumentList().length);
                XPathFunction impl = functionDecl == null ? null
                    : functionDecl instanceof XPathFunction function ? function
                    : new FunctionImpl(functionDecl);
                return (myFunction = Pair.create(getQName().toString(), impl)).second;
            }
        }

        @Override
        @Nonnull
        public Object[] getVariants() {
            return EMPTY_ARRAY;
        }

        @Override
        @RequiredWriteAction
        public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
            XPathFunctionCall child = (XPathFunctionCall) XPathChangeUtil.createExpression(getElement(), newElementName + "()");

            PrefixedNameImpl newName = ((PrefixedNameImpl) child.getQName());
            PrefixedNameImpl oldName = ((PrefixedNameImpl) getQName());

            ASTNode localNode = newName.getLocalNode();
            getNode().replaceChild(oldName.getLocalNode(), localNode);

            PsiElement psi = getNode().getPsi();
            assert psi != null;
            return psi;
        }

        class FunctionImpl extends LightElement implements XPathFunction, ItemPresentation, NavigationItem {
            private final Function myFunctionDecl;

            @RequiredReadAction
            public FunctionImpl(Function functionDecl) {
                super(getElement().getManager(), getElement().getContainingFile().getLanguage());
                myFunctionDecl = functionDecl;
            }

            @Override
            public PsiElement getContext() {
                return XPathFunctionCallImpl.this;
            }

            @Override
            @RequiredReadAction
            public String getName() {
                return myFunctionDecl != null ? myFunctionDecl.getName() : getFunctionName();
            }

            @Override
            @RequiredReadAction
            public String toString() {
                return "Function: " + getName();
            }

            @Override
            @RequiredReadAction
            @SuppressWarnings({"ConstantConditions"})
            public String getText() {
                return getName();
            }

            @Override
            public ItemPresentation getPresentation() {
                return this;
            }

            @Override
            public @Nullable Image getIcon() {
                return PlatformIconGroup.nodesFunction();
            }

            @Override
            public @Nullable String getLocationString() {
                return null;
            }

            @Override
            public LocalizeValue getPresentableText() {
                return myFunctionDecl != null
                    ? LocalizeValue.of(myFunctionDecl.buildSignature() + ": " + myFunctionDecl.getReturnType().getName())
                    : LocalizeValue.empty();
            }

            @Override
            public void accept(@Nonnull PsiElementVisitor visitor) {
            }

            @Override
            public PsiElement copy() {
                return this;
            }

            @Override
            @RequiredWriteAction
            public PsiElement setName(@Nonnull String name) throws IncorrectOperationException {
                throw new IncorrectOperationException();
            }

            @Override
            @RequiredReadAction
            public boolean isValid() {
                return true;
            }

            @Override
            @RequiredReadAction
            public int hashCode() {
                String name = getName();
                return name != null ? name.hashCode() : 0;
            }

            @Override
            @RequiredReadAction
            public boolean equals(@Nullable Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != getClass()) {
                    return false;
                }
                FunctionImpl that = (FunctionImpl) obj;
                return Objects.equals(getName(), that.getName());
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
