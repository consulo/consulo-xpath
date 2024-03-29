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

public class AxisLookup extends AbstractLookup implements Iconable
{
    public AxisLookup(String name) {
        super(name + "::", name);
    }

    public boolean isKeyword() {
        return true;
    }

    public Image getIcon(int flags) {
        return AllIcons.Nodes.J2eeParameter;
    }
}
