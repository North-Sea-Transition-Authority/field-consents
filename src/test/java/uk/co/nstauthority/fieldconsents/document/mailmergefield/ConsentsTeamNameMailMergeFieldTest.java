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
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class ConsentsTeamNameMailMergeFieldTest {

  @Mock
  private CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @InjectMocks
  private ConsentsTeamNameMailMergeField consentsTeamNameMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(consentsTeamNameMailMergeField.getMnemonic()).isEqualTo("CONSENTS_TEAM_NAME");
  }

  @Test
  void getDescription() {
    assertThat(consentsTeamNameMailMergeField.getDescription()).isEqualTo("The name of the consents team");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(consentsTeamNameMailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var teamName = "Test team name";

    when(customerBrandingConfigurationProperties.teamName()).thenReturn(teamName);

    assertThat(consentsTeamNameMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(teamName));
  }
}
