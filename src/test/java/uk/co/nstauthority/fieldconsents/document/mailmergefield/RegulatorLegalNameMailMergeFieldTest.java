package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;

class RegulatorLegalNameMailMergeFieldTest {

  private static final String REGULATOR_LEGAL_NAME = "Regulator legal name";
  private static final String MNEMONIC = "REGULATOR_LEGAL_NAME";
  private static final String DESCRIPTION = "The regulator's legal name";

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties = new CustomerBrandingConfigurationProperties(null, null, null, REGULATOR_LEGAL_NAME, null);
  private final RegulatorLegalNameMailMergeField mailMergeField = new RegulatorLegalNameMailMergeField(customerBrandingConfigurationProperties);

  @Test
  void getMnemonic() {
    assertThat(mailMergeField.getMnemonic()).isEqualTo(MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(mailMergeField.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  void isApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    assertThat(mailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    assertThat(mailMergeField.resolve(documentInstanceDto)).isEqualTo(REGULATOR_LEGAL_NAME);
  }
}
