package consulo.xpath.psi;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.psi.PsiReference;

/**
 * @author VISTALL
 * @since 21-Sep-16
 *
 * This reference will be visited by XPathAnnotator
 */
public interface XPathReferenceWithValidation extends PsiReference, EmptyResolveMessageProvider
{
}
