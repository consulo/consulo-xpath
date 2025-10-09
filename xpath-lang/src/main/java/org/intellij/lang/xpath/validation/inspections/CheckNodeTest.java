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
package org.intellij.lang.xpath.validation.inspections;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.localize.LocalizeValue;
import consulo.xml.psi.xml.XmlElement;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.lang.xpath.context.ContextProvider;
import org.intellij.lang.xpath.context.NamespaceContext;
import org.intellij.lang.xpath.psi.PrefixedName;
import org.intellij.lang.xpath.psi.XPathNodeTest;

import javax.xml.namespace.QName;
import java.text.MessageFormat;
import java.util.Set;

@ExtensionImpl
public class CheckNodeTest extends XPathInspection<Object> {
    private static final String SHORT_NAME = "CheckNodeTest";

    @Override
    protected Visitor createVisitor(InspectionManager manager, ProblemsHolder holder, boolean isOnTheFly, Object state) {
        return new MyVisitor(manager, isOnTheFly, holder, state);
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Check Node Test");
    }

    @Nonnull
    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    final static class MyVisitor extends Visitor<Object> {
        MyVisitor(InspectionManager manager, boolean isOnTheFly, ProblemsHolder holder, Object state) {
            super(manager, holder, isOnTheFly, state);
        }

        @Override
        @RequiredReadAction
        protected void checkNodeTest(XPathNodeTest nodeTest) {
            ContextProvider contextProvider = ContextProvider.getContextProvider(nodeTest.getContainingFile());
            XmlElement contextNode = contextProvider.getContextElement();
            NamespaceContext namespaceContext = contextProvider.getNamespaceContext();
            if (namespaceContext == null) {
                return;
            }

            if (nodeTest.isNameTest() && contextNode != null) {
                PrefixedName prefixedName = nodeTest.getQName();
                assert prefixedName != null;
                if (!"*".equals(prefixedName.getLocalName()) && !"*".equals(prefixedName.getPrefix())) {
                    boolean found;

                    if (nodeTest.getPrincipalType() == XPathNodeTest.PrincipalType.ELEMENT) {
                        Set<QName> elementNames = contextProvider.getElements(true);
                        if (elementNames != null) {
                            found = false;
                            for (QName pair : elementNames) {
                                if (matches(nodeTest.getQName(), pair, namespaceContext, contextNode, true)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                registerProblem(contextProvider, prefixedName, nodeTest, "element");
                            }
                        }
                    }
                    else if (nodeTest.getPrincipalType() == XPathNodeTest.PrincipalType.ATTRIBUTE) {
                        Set<QName> attributeNames = contextProvider.getAttributes(true);
                        if (attributeNames != null) {
                            found = false;
                            for (QName pair : attributeNames) {
                                if (matches(nodeTest.getQName(), pair, namespaceContext, contextNode, false)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                registerProblem(contextProvider, prefixedName, nodeTest, "attribute");
                            }
                        }
                    }
                }
            }
        }

        @RequiredReadAction
        private void registerProblem(ContextProvider contextProvider, PrefixedName prefixedName, XPathNodeTest nodeTest, String type) {
            QName qName = contextProvider.getQName(prefixedName, nodeTest);
            String name;
            if (qName != null) {
                String pattern = qName.getNamespaceURI().isEmpty() ? "''<b>{0}</b>''" : "''<b>{0}</b>'' (<i>{1}</i>)";
                name = MessageFormat.format(pattern, qName.getLocalPart(), qName.getNamespaceURI());
            }
            else {
                name = MessageFormat.format("''<b>{0}</b>''", prefixedName.getLocalName());
            }

            LocalQuickFix[] fixes = contextProvider.getQuickFixFactory().createUnknownNodeTestFixes(nodeTest);
            addProblem(myManager.createProblemDescriptor(
                nodeTest,
                "<html>Unknown " + type + " name " + name + "</html>",
                myOnTheFly,
                fixes,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            ));
        }

        private static boolean matches(
            @Nullable PrefixedName prefixedName,
            QName element,
            NamespaceContext namespaceContext,
            XmlElement context,
            boolean allowDefaultNamespace
        ) {
            if (prefixedName == null) {
                return false;
            }

            boolean b = prefixedName.getLocalName().equals(element.getLocalPart()) || "*".equals(element.getLocalPart());

            String prefix = prefixedName.getPrefix();
            if (prefix != null) {
                if (!"*".equals(prefix)) {
                    String namespaceURI = namespaceContext.getNamespaceURI(prefix, context);
                    b = b && element.getNamespaceURI().equals(namespaceURI);
                }
            }
            else if (allowDefaultNamespace) {
                String namespaceURI = namespaceContext.getDefaultNamespace(context);
                b = b && (element.getNamespaceURI().equals(namespaceURI) || element.getNamespaceURI().isEmpty() && namespaceURI == null);
            }
            else {
                b = b && element.getNamespaceURI().isEmpty();
            }
            return b;
        }
    }
}
