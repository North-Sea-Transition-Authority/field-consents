package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@Order(DocumentMailMergeFieldDisplayOrders.DIGITAL_SIGNATURE)
@Component
public class DigitalSignatureMailMergeField implements DocumentMailMergeField {

  public static final String SIGNATURE_PLACEHOLDER_TEXT = "((DIGITAL_SIGNATURE))";

  @Override
  public String getMnemonic() {
    return "DIGITAL_SIGNATURE";
  }

  @Override
  public String getDescription() {
    return "The digital signature that will be applied to the document and visible at this location. " +
        "Consent documents must have exactly one digital signature.";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic()).isConsent();
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    // This resolves to a placeholder which is later picked up by the DocumentSigningService
    return DocumentMailMergeFieldResolveResult.success(SIGNATURE_PLACEHOLDER_TEXT);
  }
}
