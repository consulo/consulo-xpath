package org.intellij.lang.xpath.completion;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import org.intellij.lang.xpath.XPath2Language;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05/01/2023
 */
@ExtensionImpl
public class XPath2CompletionContributor extends XPathCompletionContributor {
  @Nonnull
  @Override
  public Language getLanguage() {
    return XPath2Language.INSTANCE;
  }
}
