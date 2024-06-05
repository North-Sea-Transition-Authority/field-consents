package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class RegulatorLegalNameMailMergeFieldTest {

  @Mock
  private CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @InjectMocks
  private RegulatorLegalNameMailMergeField regulatorLegalNameMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(regulatorLegalNameMailMergeField.getMnemonic()).isEqualTo("REGULATOR_LEGAL_NAME");
  }

  @Test
  void getDescription() {
    assertThat(regulatorLegalNameMailMergeField.getDescription()).isEqualTo(RegulatorLegalNameMailMergeField.DESCRIPTION);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(regulatorLegalNameMailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var legalName = "Test legal name";

    when(customerBrandingConfigurationProperties.legalName()).thenReturn(legalName);

    assertThat(regulatorLegalNameMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(legalName));
  }
}
