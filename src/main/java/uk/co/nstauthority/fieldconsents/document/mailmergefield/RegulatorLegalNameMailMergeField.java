package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

@Order(11)
@Component
public class RegulatorLegalNameMailMergeField implements DocumentMailMergeField {

  private static final String MNEMONIC = "REGULATOR_LEGAL_NAME";
  private static final String DESCRIPTION = "The regulator's legal name";

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  RegulatorLegalNameMailMergeField(CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  @Override
  public String getMnemonic() {
    return MNEMONIC;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    return customerBrandingConfigurationProperties.regulatorLegalName();
  }
}
