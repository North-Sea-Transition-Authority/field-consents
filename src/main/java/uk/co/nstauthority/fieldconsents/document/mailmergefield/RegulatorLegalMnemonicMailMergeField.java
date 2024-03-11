package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;

@Order(16)
@Component
class RegulatorLegalMnemonicMailMergeField implements DocumentMailMergeField {

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  RegulatorLegalMnemonicMailMergeField(CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  @Override
  public String getMnemonic() {
    return "REGULATOR_LEGAL_MNEMONIC";
  }

  @Override
  public String getDescription() {
    return "The regulator's legal mnemonic";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    return DocumentMailMergeFieldResolveResult.success(customerBrandingConfigurationProperties.legalMnemonic());
  }
}
