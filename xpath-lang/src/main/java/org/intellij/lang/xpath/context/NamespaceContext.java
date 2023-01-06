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
package org.intellij.lang.xpath.context;

import consulo.language.editor.intention.IntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.xml.psi.xml.XmlElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface NamespaceContext {
  @Nullable
  String getNamespaceURI(String prefix, XmlElement context);

  @Nullable
  String getPrefixForURI(String uri, XmlElement context);

  @Nonnull
  Collection<String> getKnownPrefixes(XmlElement context);

  /**
   * resolve to NS-Attribute's name-token
   */
  @Nullable
  PsiElement resolve(String prefix, XmlElement context);

  IntentionAction[] getUnresolvedNamespaceFixes(PsiReference reference, String localName);

  @Nullable
  String getDefaultNamespace(XmlElement context);
}
