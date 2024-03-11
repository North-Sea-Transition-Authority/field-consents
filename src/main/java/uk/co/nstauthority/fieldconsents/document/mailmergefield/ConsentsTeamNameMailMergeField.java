package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;

@Order(17)
@Component
class ConsentsTeamNameMailMergeField implements DocumentMailMergeField {

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  ConsentsTeamNameMailMergeField(CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  @Override
  public String getMnemonic() {
    return "CONSENTS_TEAM_NAME";
  }

  @Override
  public String getDescription() {
    return "The name of the consents team";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    return DocumentMailMergeFieldResolveResult.success(customerBrandingConfigurationProperties.teamName());
  }
}
