package org.intellij.lang.xpath;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SingleLazyInstanceSyntaxHighlighterFactory;
import consulo.language.editor.highlight.SyntaxHighlighter;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class XPath2SyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
  @Nonnull
  protected SyntaxHighlighter createHighlighter() {
    return new XPathHighlighter(true);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathFileType.XPATH2.getLanguage();
  }
}
