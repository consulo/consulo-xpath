package org.intellij.lang.xpath;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class XPath2Commenter implements Commenter {
  @Override
  public String getLineCommentPrefix() {
    return null;
  }

  @Override
  public String getBlockCommentPrefix() {
    return "(:";
  }

  @Override
  public String getBlockCommentSuffix() {
    return ":)";
  }

  @Override
  public String getCommentedBlockCommentPrefix() {
    return getBlockCommentPrefix();
  }

  @Override
  public String getCommentedBlockCommentSuffix() {
    return getBlockCommentSuffix();
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathFileType.XPATH2.getLanguage();
  }
}
