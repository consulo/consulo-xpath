package org.intellij.lang.xpath;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.cacheBuilder.SimpleWordsScanner;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.navigation.ItemPresentation;
import consulo.navigation.NavigationItem;
import org.intellij.lang.xpath.psi.XPathFunction;
import org.intellij.lang.xpath.psi.XPathVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class XPathFindUsagesProvider implements FindUsagesProvider {
  @Nullable
  public WordsScanner getWordsScanner() {
    return new SimpleWordsScanner();
  }

  public boolean canFindUsagesFor(@Nonnull PsiElement psiElement) {
    return psiElement instanceof XPathFunction || psiElement instanceof XPathVariable;
  }

  @Nonnull
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
  public String getDescriptiveName(@Nonnull PsiElement element) {
    if (element instanceof PsiNamedElement) {
      final String name = ((PsiNamedElement)element).getName();
      if (name != null) {
        return name;
      }
    }
    return element.toString();
  }

  @Nonnull
  public String getNodeText(@Nonnull PsiElement element, boolean useFullName) {
    if (useFullName) {
      if (element instanceof NavigationItem) {
        final NavigationItem navigationItem = ((NavigationItem)element);
        final ItemPresentation presentation = navigationItem.getPresentation();
        if (presentation != null) {
          final String text = presentation.getPresentableText();
          if (text != null) {
            return text;
          }
        }
        final String name = navigationItem.getName();
        if (name != null) {
          return name;
        }
      }
    }
    if (element instanceof PsiNamedElement) {
      final String name = ((PsiNamedElement)element).getName();
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
