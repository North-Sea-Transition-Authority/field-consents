package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;

@Order(12)
@Component
class RegulatorNameMailMergeField implements DocumentMailMergeField {

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  RegulatorNameMailMergeField(CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  @Override
  public String getMnemonic() {
    return "REGULATOR_NAME";
  }

  @Override
  public String getDescription() {
    return "The regulator's name";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    return customerBrandingConfigurationProperties.name();
  }
}
