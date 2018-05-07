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
package org.intellij.lang.xpath.psi;

import javax.annotation.Nullable;

import org.intellij.lang.xpath.context.functions.Function;

import com.intellij.psi.PsiNamedElement;

public interface XPathFunction extends XPathElement, PsiNamedElement {
    @Nullable
    Function getDeclaration();
}
