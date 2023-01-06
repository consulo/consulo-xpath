/**
 * @author VISTALL
 * @since 02/01/2023
 */
open module com.intellij.xpath.view {
  requires transitive com.intellij.xpath.lang;

  requires consulo.ide.api;

  requires jaxen;
  
  // TODO [VISTALL] drop in future
  requires java.desktop;
  // TODO [VISTALL] remove in future
  requires consulo.ide.impl;

  exports consulo.xpath.view;
  exports org.intellij.plugins.xpathView;
  exports org.intellij.plugins.xpathView.eval;
  exports org.intellij.plugins.xpathView.search;
  exports org.intellij.plugins.xpathView.support;
  exports org.intellij.plugins.xpathView.support.jaxen;
  exports org.intellij.plugins.xpathView.support.jaxen.extensions;
  exports org.intellij.plugins.xpathView.ui;
  exports org.intellij.plugins.xpathView.util;
}