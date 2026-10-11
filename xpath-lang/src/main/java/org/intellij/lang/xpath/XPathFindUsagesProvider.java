package org.intellij.lang.xpath;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.cacheBuilder.SimpleWordsScanner;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.NavigationItem;
import org.intellij.lang.xpath.psi.XPathFunction;
import org.intellij.lang.xpath.psi.XPathVariable;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class XPathFindUsagesProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new SimpleWordsScanner();
    }

    @Override
    public boolean canFindUsagesFor(@Nonnull PsiElement psiElement) {
        return psiElement instanceof XPathFunction || psiElement instanceof XPathVariable;
    }

    @Nonnull
    @Override
    public String getType(@Nonnull PsiElement element) {
        if (element instanceof XPathFunction) {
            return "function";
        }
        else if (element instanceof XPathVariable) {
            return "variable";
        }
        else {
            return "unknown";
        }
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public String getDescriptiveName(@Nonnull PsiElement element) {
        if (element instanceof PsiNamedElement namedElem) {
            String name = namedElem.getName();
            if (name != null) {
                return name;
            }
        }
        return element.toString();
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public String getNodeText(@Nonnull PsiElement element, boolean useFullName) {
        if (useFullName && element instanceof NavigationItem navItem) {
            ItemPresentation presentation = navItem.getPresentation();
            if (presentation != null) {
                LocalizeValue text = presentation.getPresentableText();
                if (text.isNotEmpty()) {
                    return text.get();
                }
            }
            String name = navItem.getName();
            if (name != null) {
                return name;
            }
        }
        if (element instanceof PsiNamedElement namedElem) {
            String name = namedElem.getName();
            if (name != null) {
                return name;
            }
        }
        return element.toString();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return XPathFileType.XPATH.getLanguage();
    }
}
