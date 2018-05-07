package org.intellij.lang.xpath.context;

import javax.annotation.Nonnull;

import org.intellij.lang.xpath.XPathFile;

import javax.annotation.Nullable;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;

public abstract class ContextProviderExtension {
    public static final ExtensionPointName<ContextProviderExtension> EXTENSION_POINT_NAME =
            ExtensionPointName.create("com.intellij.xpath.contextProviderExtension");

    protected abstract boolean accepts(XPathFile file);

    @Nonnull
    protected abstract ContextProvider getContextProvider(XPathFile file);

    @Nullable
    public static ContextProvider getInstance(XPathFile file) {
        final ContextProviderExtension[] extensions = Extensions.getExtensions(EXTENSION_POINT_NAME);
        for (ContextProviderExtension extension : extensions) {
            if (extension.accepts(file)) {
                return extension.getContextProvider(file);
            }
        }
        return null;
    }
}