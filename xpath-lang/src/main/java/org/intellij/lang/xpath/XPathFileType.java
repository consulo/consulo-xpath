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
package org.intellij.lang.xpath;

import javax.annotation.Nonnull;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.ui.image.Image;
import icons.XpathIcons;

public final class XPathFileType extends LanguageFileType
{
	public static final XPathFileType XPATH = new XPathFileType(new XPathLanguage());
	public static final XPathFileType XPATH2 = new XPathFileType(new XPath2Language());

	private XPathFileType(Language language)
	{
		super(language);
	}

	@Override
	@Nonnull
	public String getId()
	{
		return getLanguage().getID();
	}

	@Override
	@Nonnull
	public String getDescription()
	{
		return "XPath";
	}

	@Override
	@Nonnull
	public String getDefaultExtension()
	{
		return getLanguage().getID().toLowerCase();
	}

	@Override
	public Image getIcon()
	{
		return XpathIcons.Xpath;
	}
}
