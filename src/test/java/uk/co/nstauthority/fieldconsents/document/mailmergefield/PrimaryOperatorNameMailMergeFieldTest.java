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
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class PrimaryOperatorNameMailMergeFieldTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @InjectMocks
  private PrimaryOperatorNameMailMergeField primaryOperatorNameMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(primaryOperatorNameMailMergeField.getMnemonic()).isEqualTo("PRIMARY_OPERATOR_NAME");
  }

  @Test
  void getDescription() {
    assertThat(primaryOperatorNameMailMergeField.getDescription())
        .isEqualTo("The name of the primary operator on the application");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(primaryOperatorNameMailMergeField.isApplicable(template)).isTrue();
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var organisationUnitName = "Test organisation unit name";
    var organisationUnitJson = new OrganisationUnitJson(null, organisationUnitName);

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(organisationUnitService.getOrganisationUnitById(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit lookup for PRIMARY_OPERATOR_NAME mail merge field"
    )).thenReturn(organisationUnitJson);

    assertThat(primaryOperatorNameMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(organisationUnitName));
  }
}
