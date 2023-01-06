package consulo.xpath.psi;

import consulo.language.psi.EmptyResolveMessageProvider;
import consulo.language.psi.PsiReference;

/**
 * @author VISTALL
 * @since 21-Sep-16
 *
 * This reference will be visited by XPathAnnotator
 */
public interface XPathReferenceWithValidation extends PsiReference, EmptyResolveMessageProvider
{
}
