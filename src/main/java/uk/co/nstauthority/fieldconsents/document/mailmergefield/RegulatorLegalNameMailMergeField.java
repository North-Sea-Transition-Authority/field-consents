package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;

@Order(15)
@Component
class RegulatorLegalNameMailMergeField implements DocumentMailMergeField {

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  RegulatorLegalNameMailMergeField(CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  @Override
  public String getMnemonic() {
    return "REGULATOR_LEGAL_NAME";
  }

  @Override
  public String getDescription() {
    return "The regulator's legal name";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    return customerBrandingConfigurationProperties.legalName();
  }
}
