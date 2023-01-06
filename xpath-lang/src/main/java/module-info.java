/**
 * @author VISTALL
 * @since 02/01/2023
 */
open module com.intellij.xpath.lang {
  requires transitive consulo.language.api;
  requires transitive consulo.language.impl;

  requires transitive com.intellij.xml;

  // TODO [VISTALL] remove in future
  requires java.desktop;

  exports consulo.xpath.context;
  exports consulo.xpath.icon;
  exports consulo.xpath.psi;
  exports org.intellij.lang.xpath;
  exports org.intellij.lang.xpath.completion;
  exports org.intellij.lang.xpath.context;
  exports org.intellij.lang.xpath.context.functions;
  exports org.intellij.lang.xpath.psi;
  exports org.intellij.lang.xpath.psi.impl;
  exports org.intellij.lang.xpath.validation;
  exports org.intellij.lang.xpath.validation.inspections;
  exports org.intellij.lang.xpath.validation.inspections.quickfix;
}