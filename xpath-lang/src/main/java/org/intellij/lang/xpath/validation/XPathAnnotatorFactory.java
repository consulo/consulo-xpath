package org.intellij.lang.xpath.validation;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import org.intellij.lang.xpath.XPathLanguage;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06/01/2023
 */
@ExtensionImpl
public class XPathAnnotatorFactory implements AnnotatorFactory {
  @Nullable
  @Override
  public Annotator createAnnotator() {
    return new XPathAnnotator();
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathLanguage.INSTANCE;
  }
}
