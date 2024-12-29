package org.intellij.lang.xpath;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileType;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class XPath2QuoteHandler extends XPathQuoteHandler {
  @Nonnull
  @Override
  public FileType getFileType() {
    return XPathFileType.XPATH2;
  }
}
