package consulo.xpath.view;

import javax.annotation.Nonnull;
import jakarta.inject.Singleton;

import org.intellij.plugins.xpathView.Config;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

/**
 * @author VISTALL
 * @since 2018-08-23
 */
@Singleton
@State(name = "XPathView.XPathViewPlugin", storages = @Storage("xpath.xml"))
public class XPathViewConfig implements PersistentStateComponent<Config>
{
	@Nonnull
	public static XPathViewConfig getInstance()
	{
		return ServiceManager.getService(XPathViewConfig.class);
	}

	private Config myConfig = new Config();

	@Nonnull
	@Override
	public Config getState()
	{
		return myConfig;
	}

	@Override
	public void loadState(Config config)
	{
		myConfig = config;
	}
}
