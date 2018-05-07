package consulo.xpath.context;

import org.intellij.lang.xpath.context.NamespaceContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.psi.xml.XmlElement;
import consulo.extensions.CompositeExtensionPointName;

/**
 * @author VISTALL
 * @since 21-Sep-16
 */
public interface NamespaceContextProvider
{
	CompositeExtensionPointName<NamespaceContextProvider> EP_NAME = CompositeExtensionPointName.applicationPoint("com.intellij.xpath.namespaceContextProvider", NamespaceContextProvider.class);

	@Nullable
	NamespaceContext getNamespaceContext(@Nonnull XmlElement xmlElement);
}
