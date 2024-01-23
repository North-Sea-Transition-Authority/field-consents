package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class LicenceReferenceListMailMergeFieldTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  @InjectMocks
  private LicenceReferenceListMailMergeField licenceReferenceListMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(licenceReferenceListMailMergeField.getMnemonic()).isEqualTo("LICENCE_REFERENCE_LIST");
  }

  @Test
  void getDescription() {
    assertThat(licenceReferenceListMailMergeField.getDescription())
        .isEqualTo("A list of the licence references associated with the application");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(licenceReferenceListMailMergeField.isApplicable(template)).isTrue();
  }

  @Test
  void resolve_1AssetLicence() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAssetLicence = new ApplicationAssetLicence();
    applicationAssetLicence.setCachedLicenceRef("test/ref");

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetLicenceService.getAssetLicences(applicationVersion))
        .thenReturn(List.of(applicationAssetLicence));

    assertThat(licenceReferenceListMailMergeField.resolve(documentInstanceDto))
        .isEqualTo("test/ref");
  }

  @Test
  void resolve_2AssetLicences() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAssetLicence1 = new ApplicationAssetLicence();
    var applicationAssetLicence2 = new ApplicationAssetLicence();

    applicationAssetLicence1.setCachedLicenceRef("test/ref/1");
    applicationAssetLicence2.setCachedLicenceRef("test/ref/2");

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetLicenceService.getAssetLicences(applicationVersion))
        .thenReturn(List.of(applicationAssetLicence1, applicationAssetLicence2));

    assertThat(licenceReferenceListMailMergeField.resolve(documentInstanceDto))
        .isEqualTo("test/ref/1 and test/ref/2");
  }

  @Test
  void resolve_3AssetLicences() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAssetLicence1 = new ApplicationAssetLicence();
    var applicationAssetLicence2 = new ApplicationAssetLicence();
    var applicationAssetLicence3 = new ApplicationAssetLicence();

    applicationAssetLicence1.setCachedLicenceRef("test/ref/1");
    applicationAssetLicence2.setCachedLicenceRef("test/ref/2");
    applicationAssetLicence3.setCachedLicenceRef("test/ref/3");

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetLicenceService.getAssetLicences(applicationVersion))
        .thenReturn(List.of(applicationAssetLicence1, applicationAssetLicence2, applicationAssetLicence3));

    assertThat(licenceReferenceListMailMergeField.resolve(documentInstanceDto))
        .isEqualTo("test/ref/1, test/ref/2 and test/ref/3");
  }

  @Test
  void resolve_cachedLicenceRefOnlyIncludedOnce() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAssetLicence1 = new ApplicationAssetLicence();
    var applicationAssetLicence2 = new ApplicationAssetLicence();
    var applicationAssetLicence3 = new ApplicationAssetLicence();

    applicationAssetLicence1.setCachedLicenceRef("test/ref/1");
    applicationAssetLicence2.setCachedLicenceRef("test/ref/2");
    applicationAssetLicence3.setCachedLicenceRef("test/ref/2");

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetLicenceService.getAssetLicences(applicationVersion))
        .thenReturn(List.of(applicationAssetLicence1, applicationAssetLicence2, applicationAssetLicence3));

    assertThat(licenceReferenceListMailMergeField.resolve(documentInstanceDto))
        .isEqualTo("test/ref/1 and test/ref/2");
  }
}
