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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class SupersededConsentIssueDateMailMergeFieldTest {

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @Mock
  private ConsentService consentService;

  @InjectMocks
  private SupersededConsentIssueDateMailMergeField supersededConsentIssueDateMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(supersededConsentIssueDateMailMergeField.getMnemonic())
        .isEqualTo("SUPERSEDED_CONSENT_ISSUE_DATE");
  }

  @Test
  void getDescription() {
    assertThat(supersededConsentIssueDateMailMergeField.getDescription())
        .isEqualTo("The date the superseded consent was issued (revisions only)");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(supersededConsentIssueDateMailMergeField.isApplicable(documentTemplateDto)).isEqualTo(documentTemplateType.isConsent());
  }

  @Test
  void resolve_previousConsentExists() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationId = 7;
    var previousConsent = ConsentTestUtil.newBuilder().build();

    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(applicationId);
    when(consentService.findPreviousConsentByApplicationId(applicationId)).thenReturn(Optional.of(previousConsent));

    assertThat(supersededConsentIssueDateMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(DateUtils.format(previousConsent.getIssuedInstant(), DateUtils.LONG_DATE)));
  }


  @Test
  void resolve_previousConsentDoesNotExist() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationId = 7;

    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(applicationId);
    when(consentService.findPreviousConsentByApplicationId(applicationId)).thenReturn(Optional.empty());

    assertThat(supersededConsentIssueDateMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.error("Mail merge field SUPERSEDED_CONSENT_ISSUE_DATE is not valid. Application is not a revision"));
  }
}
