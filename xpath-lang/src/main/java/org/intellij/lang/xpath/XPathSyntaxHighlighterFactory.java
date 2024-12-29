package org.intellij.lang.xpath;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SingleLazyInstanceSyntaxHighlighterFactory;
import consulo.language.editor.highlight.SyntaxHighlighter;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class XPathSyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
  @Nonnull
  protected SyntaxHighlighter createHighlighter() {
    return new XPathHighlighter(false);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathFileType.XPATH.getLanguage();
  }
}
