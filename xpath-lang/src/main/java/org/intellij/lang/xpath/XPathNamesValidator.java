package org.intellij.lang.xpath;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.refactoring.NamesValidator;
import consulo.language.lexer.Lexer;
import consulo.project.Project;
import org.intellij.lang.xpath.completion.CompletionLists;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class XPathNamesValidator implements NamesValidator {
  public boolean isIdentifier(String text, Project project) {
    Lexer xPathLexer = XPathLexer.create(false);
    xPathLexer.start(text);
    assert xPathLexer.getState() == 0;

    boolean b = xPathLexer.getTokenType() == XPathTokenTypes.NCNAME;
    xPathLexer.advance();

    if (xPathLexer.getTokenType() == null) {
      return b;
    }
    else if (xPathLexer.getTokenType() == XPathTokenTypes.COL) {
      xPathLexer.advance();
      b = xPathLexer.getTokenType() == XPathTokenTypes.NCNAME;
      xPathLexer.advance();
      return b && xPathLexer.getTokenType() == null;
    }

    return false;
  }

  public boolean isKeyword(String text, Project project) {
    return CompletionLists.AXIS_NAMES.contains(text) || CompletionLists.NODE_TYPE_FUNCS.contains(text) || CompletionLists.OPERATORS.contains(
      text);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XPathFileType.XPATH.getLanguage();
  }
}
