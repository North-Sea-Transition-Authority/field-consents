package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class SupersededConsentReferenceMailMergeFieldTest {

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentService consentService;

  @InjectMocks
  private SupersededConsentReferenceMailMergeField supersededConsentReferenceMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(supersededConsentReferenceMailMergeField.getMnemonic())
        .isEqualTo("SUPERSEDED_CONSENT_REFERENCE");
  }

  @Test
  void getDescription() {
    assertThat(supersededConsentReferenceMailMergeField.getDescription())
        .isEqualTo("The superseded consent's reference (revisions only)");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(supersededConsentReferenceMailMergeField.isApplicable(documentTemplateDto)).isEqualTo(documentTemplateType.isConsent());
  }

  @Test
  void resolve_previousConsentExists() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationId = 7;
    var previousConsent = ConsentTestUtil.newBuilder().build();
    var previousConsentLatestApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var previousConsentApplicationReference = "Test/application/reference";

    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(applicationId);
    when(consentService.findPreviousConsentByApplicationId(applicationId)).thenReturn(Optional.of(previousConsent));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(previousConsent.getApplication().getId()))
        .thenReturn(previousConsentLatestApplicationVersion);
    when(applicationService.generateApplicationReference(previousConsentLatestApplicationVersion)).thenReturn(previousConsentApplicationReference);

    assertThat(supersededConsentReferenceMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(previousConsentApplicationReference));
  }


  @Test
  void resolve_previousConsentDoesNotExist() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationId = 7;

    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(applicationId);
    when(consentService.findPreviousConsentByApplicationId(applicationId)).thenReturn(Optional.empty());

    assertThat(supersededConsentReferenceMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.error("Mail merge field SUPERSEDED_CONSENT_REFERENCE is not valid. Application is not a revision"));
  }
}
