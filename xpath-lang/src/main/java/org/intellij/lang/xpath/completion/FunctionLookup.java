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
package org.intellij.lang.xpath.completion;

import consulo.application.AllIcons;
import consulo.component.util.Iconable;
import consulo.ui.image.Image;
import consulo.util.lang.Comparing;
import org.intellij.lang.xpath.context.functions.Function;

public class FunctionLookup extends AbstractLookup implements Iconable {
  private final String type;
  private final boolean hasParameters;

  FunctionLookup(String name, String presentation) {
    this(name, presentation, null, false);
  }

  FunctionLookup(String name, String presentation, String type, boolean hasParams) {
    super(name, presentation);
    this.type = type;
    this.hasParameters = hasParams;
  }

  @Override
  public String getTypeHint() {
    return type == null ? "" : type;
  }

  @Override
  public boolean isFunction() {
    return true;
  }

  @Override
  public boolean hasParameters() {
    return hasParameters;
  }

  @Override
  public boolean isKeyword() {
    return type == null;
  }

  @Override
  public Image getIcon(int flags) {
    return AllIcons.Nodes.Function;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final FunctionLookup that = (FunctionLookup)o;

    if (!Comparing.equal(myPresentation, that.myPresentation)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  public static Lookup newFunctionLookup(String name, Function functionDecl) {
    final String presentation = functionDecl.buildSignature();
    final String returnType = functionDecl.getReturnType().getName();
    final boolean hasParams = functionDecl.getParameters().length > 0;
    return new FunctionLookup(name, presentation, returnType, hasParams);
  }
}
