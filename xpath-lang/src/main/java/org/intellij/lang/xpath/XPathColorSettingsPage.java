/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

/*
 * Created by IntelliJ IDEA.
 * User: sweinreuter
 * Date: 15.03.2006
 * Time: 18:26:09
 */
package org.intellij.lang.xpath;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javax.swing.*;
import java.util.Map;

public class XPathColorSettingsPage implements ColorSettingsPage {
    @Nonnull
    public String getDisplayName() {
        return "XPath";
    }

    @Nullable
    public Icon getIcon() {
        return XPathFileType.XPATH.getIcon();
    }

    @Nonnull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return new AttributesDescriptor[]{
                new AttributesDescriptor("Keyword", XPathHighlighter.XPATH_KEYWORD),
                new AttributesDescriptor("Name", XPathHighlighter.XPATH_NAME),
                new AttributesDescriptor("Number", XPathHighlighter.XPATH_NUMBER),
                new AttributesDescriptor("String", XPathHighlighter.XPATH_STRING),
                new AttributesDescriptor("Operator", XPathHighlighter.XPATH_OPERATION_SIGN),
                new AttributesDescriptor("Parentheses", XPathHighlighter.XPATH_PARENTH),
                new AttributesDescriptor("Brackets", XPathHighlighter.XPATH_BRACKET),
                new AttributesDescriptor("Function", XPathHighlighter.XPATH_FUNCTION),
                new AttributesDescriptor("Variable", XPathHighlighter.XPATH_VARIABLE),
                new AttributesDescriptor("Extension Prefix", XPathHighlighter.XPATH_PREFIX),
                new AttributesDescriptor("Other", XPathHighlighter.XPATH_TEXT),
        };
    }

    @Nonnull
    public ColorDescriptor[] getColorDescriptors() {
        return new ColorDescriptor[0];
    }

    @Nonnull
    public SyntaxHighlighter getHighlighter() {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(XPathFileType.XPATH.getLanguage(), null, null);
    }

    @NonNls
    @Nonnull
    public String getDemoText() {
        return "//prefix:*[ext:name() = 'changes']/element[(position() mod 2) = $pos + 1]/parent::*";
    }

    @Nullable
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }
}