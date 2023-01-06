/*
 * Copyright 2005-2008 Sascha Weinreuter
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

package org.intellij.lang.xpath.completion;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupItem;
import consulo.language.pattern.PatternCondition;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ContainerUtil;
import org.intellij.lang.xpath.XPathLanguage;
import org.intellij.lang.xpath.context.NamespaceContext;
import org.intellij.lang.xpath.psi.*;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.function.Function;

import static consulo.language.pattern.PlatformPatterns.psiElement;

/*
 * Created by IntelliJ IDEA.
 * User: sweinreuter
 */
@ExtensionImpl
public class XPathCompletionContributor extends CompletionContributor {
  public static final XPathInsertHandler INSERT_HANDLER = new XPathInsertHandler();

  public XPathCompletionContributor() {
    extend(CompletionType.BASIC, psiElement().withParent(XPathNodeTest.class), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        final XPathNodeTest nodeTest = (XPathNodeTest)parameters.getPosition().getParent();
        addResult(result, CompletionLists.getNodeTestCompletions(nodeTest), parameters);
      }
    });
    extend(CompletionType.BASIC, psiElement().withParent(psiElement(XPathNodeTest.class).with(prefix())), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        final XPathNodeTest nodeTest = (XPathNodeTest)parameters.getPosition().getParent();
        addResult(result, CompletionLists.getFunctionCompletions(nodeTest), parameters);
      }
    });

    extend(CompletionType.BASIC, psiElement().withParent(XPathAxisSpecifier.class), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        addResult(result, CompletionLists.getAxisCompletions(), parameters);
      }
    });

    extend(CompletionType.BASIC, psiElement().withParent(XPathFunctionCall.class), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        final XPathFunctionCall call = (XPathFunctionCall)parameters.getPosition().getParent();
        addResult(result, CompletionLists.getFunctionCompletions(call), parameters);
      }
    });
    extend(CompletionType.BASIC, psiElement().withParent(psiElement(XPathFunctionCall.class).without(prefix())), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        final XPathFunctionCall call = (XPathFunctionCall)parameters.getPosition().getParent();
        addResult(result, CompletionLists.getNodeTypeCompletions(call), parameters);
      }
    });

    extend(CompletionType.BASIC, psiElement().withParent(XPathVariableReference.class), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        addResult(result,
                  CompletionLists.getVariableCompletions((XPathVariableReference)parameters.getPosition().getParent()),
                  parameters);
      }
    });

    extend(CompletionType.BASIC, psiElement().withParent(psiElement(XPath2TypeElement.class).without(prefix())), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        final XPathElement parent = PsiTreeUtil.getParentOfType(parameters.getPosition(), XPathElement.class);
        assert parent != null;

        if (parent.getParent() instanceof XPath2TreatAs || parent.getParent() instanceof XPath2InstanceOf) {
          addResult(result, CompletionLists.getNodeTypeCompletions(parent), parameters);
        }

        final NamespaceContext namespaceContext = parent.getXPathContext().getNamespaceContext();
        if (namespaceContext != null) {
          final String prefixForURI =
            namespaceContext.getPrefixForURI(XPath2Type.XMLSCHEMA_NS, parent.getXPathContext().getContextElement());
          if (prefixForURI != null && prefixForURI.length() > 0) {
            addResult(result,
                      ContainerUtil.map(XPath2Type.SchemaType.listSchemaTypes(),
                                        (Function<XPath2Type, Lookup>)type -> new MyLookup(prefixForURI + ":" + type.getQName()
                                                                                                                    .getLocalPart())),
                      parameters);
          }
        }
      }
    });
    extend(CompletionType.BASIC, psiElement().withParent(psiElement(XPath2TypeElement.class).with(prefix())), new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
        final XPath2TypeElement parent = PsiTreeUtil.getParentOfType(parameters.getPosition(), XPath2TypeElement.class);
        assert parent != null;

        final QName qName = parent.getXPathContext().getQName(parent);
        if (qName != null && qName.getNamespaceURI().equals(XPath2Type.XMLSCHEMA_NS)) {
          addResult(result,
                    ContainerUtil.map(XPath2Type.SchemaType.listSchemaTypes(),
                                      (Function<XPath2Type, Lookup>)type -> new MyLookup(type.getQName().getLocalPart())),
                    parameters);
        }
      }
    });
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathLanguage.INSTANCE;
  }

  private static PatternCondition<QNameElement> prefix() {
    return new PatternCondition<QNameElement>("hasPrefix") {
      @Override
      public boolean accepts(@Nonnull QNameElement qnameElement, ProcessingContext context) {
        final PrefixedName qname = qnameElement.getQName();
        return qname != null && qname.getPrefix() != null;
      }
    };
  }

  private static void addResult(CompletionResultSet result, Collection<Lookup> collection, CompletionParameters parameters) {
    result = result.withPrefixMatcher(findPrefixStatic(parameters));

    for (Lookup lookup : collection) {
      final LookupItem<Lookup> item = new LookupItem<Lookup>(lookup, lookup.toString());
      item.setInsertHandler(INSERT_HANDLER);
      if (lookup.isKeyword()) {
        item.setBold();
      }
      result.addElement(item);
    }
  }

  private static String findPrefixStatic(CompletionParameters parameters) {
    String prefix = CompletionUtilCore.findReferencePrefix(parameters);

    PsiElement element = parameters.getPosition();
    if (element.getParent() instanceof XPathVariableReference) {
      prefix = "$" + prefix;
    }

    if (element.getParent() instanceof XPathNodeTest) {
      final XPathNodeTest nodeTest = ((XPathNodeTest)element.getParent());
      if (nodeTest.isNameTest()) {
        final PrefixedName prefixedName = nodeTest.getQName();
        assert prefixedName != null;
        final String p = prefixedName.getPrefix();

        int endIndex = prefixedName.getLocalName().indexOf(CompletionLists.INTELLIJ_IDEA_RULEZ);
        if (endIndex != -1) {
          prefix = prefixedName.getLocalName().substring(0, endIndex);
        }
        else if (p != null) {
          endIndex = p.indexOf(CompletionLists.INTELLIJ_IDEA_RULEZ);
          if (endIndex != -1) {
            prefix = p.substring(0, endIndex);
          }
        }
      }
    }

    return prefix;
  }

  private static class MyLookup extends AbstractLookup {
    public MyLookup(String name) {
      super(name, name);
    }
  }
}
