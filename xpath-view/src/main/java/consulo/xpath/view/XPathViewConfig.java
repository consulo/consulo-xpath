package consulo.xpath.view;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.ide.ServiceManager;
import jakarta.inject.Singleton;
import org.intellij.plugins.xpathView.Config;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2018-08-23
 */
@Singleton
@State(name = "XPathView.XPathViewPlugin", storages = @Storage("xpath.xml"))
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class XPathViewConfig implements PersistentStateComponent<Config> {
  @Nonnull
  public static XPathViewConfig getInstance() {
    return ServiceManager.getService(XPathViewConfig.class);
  }

  private Config myConfig = new Config();

  @Nonnull
  @Override
  public Config getState() {
    return myConfig;
  }

  @Override
  public void loadState(Config config) {
    myConfig = config;
  }
}
