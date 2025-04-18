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
package org.intellij.lang.xpath.context.functions;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.Application;
import consulo.util.lang.Pair;
import org.intellij.lang.xpath.context.ContextType;

import jakarta.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtensionAPI(ComponentScope.APPLICATION)
public abstract class XPathFunctionProvider {
    @Nonnull
    public abstract Map<QName, ? extends Function> getFunctions(ContextType contextType);

    public static List<Pair<QName, ? extends Function>> getAvailableFunctions(ContextType type) {
        final ArrayList<Pair<QName, ? extends Function>> list = new ArrayList<Pair<QName, ? extends Function>>();
        for (XPathFunctionProvider provider : Application.get().getExtensionPoint(XPathFunctionProvider.class)) {
            final Map<QName, ? extends Function> functions = provider.getFunctions(type);

            final Set<QName> names = functions.keySet();
            for (QName name : names) {
                list.add(Pair.create(name, functions.get(name)));
            }
        }
        return list;
    }
}
