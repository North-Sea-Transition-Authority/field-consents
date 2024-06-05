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
class RegulatorEmailMailMergeFieldTest {

  @Mock
  private CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @InjectMocks
  private RegulatorEmailMailMergeField regulatorEmailMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(regulatorEmailMailMergeField.getMnemonic()).isEqualTo("REGULATOR_EMAIL");
  }

  @Test
  void getDescription() {
    assertThat(regulatorEmailMailMergeField.getDescription()).isEqualTo(RegulatorEmailMailMergeField.DESCRIPTION);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(regulatorEmailMailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var email = "test@email.com";

    when(customerBrandingConfigurationProperties.email()).thenReturn(email);

    assertThat(regulatorEmailMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(email));
  }
}
