package consulo.xpath.highlight;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AttributesFlyweightBuilder;
import consulo.colorScheme.EditorColorSchemeExtender;
import consulo.colorScheme.EditorColorsScheme;
import consulo.ui.color.RGBColor;
import jakarta.annotation.Nonnull;
import org.intellij.lang.xpath.XPathHighlighter;

/**
 * @author VISTALL
 * @since 2025-05-23
 */
@ExtensionImpl
public class XPathEditorColorSchemeExtender implements EditorColorSchemeExtender {
    @Override
    public void extend(Builder builder) {
        builder.add(XPathHighlighter.XPATH_EVAL_CONTEXT_HIGHLIGHT, AttributesFlyweightBuilder
            .create()
            .withBackground(new RGBColor(194, 255, 212))
            .withErrorStripeColor(new RGBColor(194, 255, 212))
            .build());

        builder.add(XPathHighlighter.XPATH_EVAL_HIGHLIGHT, AttributesFlyweightBuilder
            .create()
            .withBackground(new RGBColor(255, 213, 120))
            .withErrorStripeColor(new RGBColor(255, 213, 120))
            .build());
    }

    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DEFAULT_SCHEME_NAME;
    }
}
