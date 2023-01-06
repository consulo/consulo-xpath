package consulo.xpath.view;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.template.DefaultLiveTemplatesProvider;

/**
 * @author VISTALL
 * @since 2018-08-23
 */
@ExtensionImpl
public class XPathViewDefaultLiveTemplateProvider implements DefaultLiveTemplatesProvider {
  @Override
  public String[] getDefaultLiveTemplateFiles() {
    return new String[]{"/liveTemplates/xsl.xml"};
  }
}
