package consulo.xpath.context;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.xml.psi.xml.XmlElement;
import org.intellij.lang.xpath.context.NamespaceContext;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 21-Sep-16
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface NamespaceContextProvider {
  @Nullable
  NamespaceContext getNamespaceContext(@Nonnull XmlElement xmlElement);
}
