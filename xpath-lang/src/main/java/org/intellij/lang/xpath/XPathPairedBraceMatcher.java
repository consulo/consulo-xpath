package org.intellij.lang.xpath;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class XPathPairedBraceMatcher implements PairedBraceMatcher {
  private BracePair[] myBracePairs;

  public BracePair[] getPairs() {
    if (myBracePairs == null) {
      myBracePairs = new BracePair[]{
        new BracePair(XPathTokenTypes.LPAREN, XPathTokenTypes.RPAREN, false),
        new BracePair(XPathTokenTypes.LBRACKET, XPathTokenTypes.RBRACKET, false),
      };
    }
    return myBracePairs;
  }

  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }

  public boolean isPairedBracesAllowedBeforeType(@Nonnull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathFileType.XPATH.getLanguage();
  }
}
