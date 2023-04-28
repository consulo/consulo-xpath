package org.intellij.lang.xpath.validation.inspections;

import consulo.language.editor.inspection.InspectionToolState;
import consulo.util.xml.serializer.XmlSerializerUtil;
import consulo.util.xml.serializer.annotation.Transient;

import javax.annotation.Nullable;
import java.util.BitSet;

/**
 * @author VISTALL
 * @since 13/04/2023
 */
public class ImplicitTypeConversionState implements InspectionToolState<ImplicitTypeConversionState> {
  public long BITS = 1720;
  public boolean FLAG_EXPLICIT_CONVERSION = true;
  public boolean IGNORE_NODESET_TO_BOOLEAN_VIA_STRING = true;

  @Transient
  public final BitSet OPTIONS = new BitSet(12);

  private void update() {
    for (int i = 0; i < 12; i++) {
      final boolean b = (BITS & (1 << i)) != 0;
      OPTIONS.set(i, b);
    }
  }

  @Nullable
  @Override
  public ImplicitTypeConversionState getState() {
    BITS = 0;
    for (int i = 11; i >= 0; i--) {
      BITS <<= 1;
      if (OPTIONS.get(i)) BITS |= 1;
    }
    return this;
  }

  @Override
  public void loadState(ImplicitTypeConversionState state) {
    XmlSerializerUtil.copyBean(state, this);

    update();
  }
}
