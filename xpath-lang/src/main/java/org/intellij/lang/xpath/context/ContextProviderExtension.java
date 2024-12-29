package org.intellij.lang.xpath.context;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import org.intellij.lang.xpath.XPathFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionAPI(ComponentScope.APPLICATION)
public abstract class ContextProviderExtension {
  protected abstract boolean accepts(XPathFile file);

  @Nonnull
  protected abstract ContextProvider getContextProvider(XPathFile file);

  @Nullable
  public static ContextProvider getInstance(XPathFile file) {
    for (ContextProviderExtension extension : file.getProject().getApplication().getExtensionPoint(ContextProviderExtension.class)) {
      if (extension.accepts(file)) {
        return extension.getContextProvider(file);
      }
    }
    return null;
  }
}