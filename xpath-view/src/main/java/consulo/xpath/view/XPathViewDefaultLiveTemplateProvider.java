package consulo.xpath.view;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;

/**
 * @author VISTALL
 * @since 2018-08-23
 */
public class XPathViewDefaultLiveTemplateProvider implements DefaultLiveTemplatesProvider
{
	@Override
	public String[] getDefaultLiveTemplateFiles()
	{
		return new String[]{"/liveTemplates/xsl"};
	}

	@Override
	public String[] getHiddenLiveTemplateFiles()
	{
		return null;
	}
}
