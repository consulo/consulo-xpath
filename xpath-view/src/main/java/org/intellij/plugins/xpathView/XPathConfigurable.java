/*
 * Copyright 2005-2009 Sascha Weinreuter
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
package org.intellij.plugins.xpathView;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.xpath.view.XPathViewConfig;
import org.intellij.plugins.xpathView.ui.ConfigUI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public class XPathConfigurable implements SearchableConfigurable
{
	private ConfigUI configUI;

	public String getDisplayName()
	{
		return "XPath Viewer";
	}

	@Nullable
	public String getHelpTopic()
	{
		return "xpath.settings";
	}

	@Nonnull
	public String getId()
	{
		return getHelpTopic();
	}

	public Runnable enableSearch(String option)
	{
		return null;
	}

	@RequiredUIAccess
	public JComponent createComponent()
	{
		configUI = new ConfigUI(XPathViewConfig.getInstance().getState());

		return configUI;
	}

	@RequiredUIAccess
	public boolean isModified()
	{
		return configUI != null && !configUI.getConfig().equals(XPathViewConfig.getInstance().getState());
	}

	@RequiredUIAccess
	public void apply() throws ConfigurationException
	{
		if(configUI != null)
		{
			XPathViewConfig.getInstance().loadState(configUI.getConfig());
		}
	}

	@RequiredUIAccess
	public void reset()
	{
		if(configUI != null)
		{
			configUI.setConfig(XPathViewConfig.getInstance().getState());
		}
	}

	@RequiredUIAccess
	public void disposeUIResources()
	{
		configUI = null;
	}
}
