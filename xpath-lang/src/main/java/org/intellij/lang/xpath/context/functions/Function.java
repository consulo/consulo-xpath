package org.intellij.lang.xpath.context.functions;

import org.intellij.lang.xpath.psi.XPathType;
import javax.annotation.Nonnull;

/*
* Created by IntelliJ IDEA.
* User: sweinreuter
* Date: 11.01.11
*/
public interface Function {
  String getName();

  @Nonnull
  Parameter[] getParameters();

  @Nonnull
  XPathType getReturnType();

  int getMinArity();

  String buildSignature();
}