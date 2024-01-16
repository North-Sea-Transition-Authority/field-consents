package uk.co.nstauthority.fieldconsents.document.templatesectioncondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;

@ExtendWith(MockitoExtension.class)
class ApplicationIsLongTermConditionTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private ConsentLengthService consentLengthService;

  @InjectMocks
  private ApplicationIsLongTermCondition applicationIsLongTermCondition;

  @Test
  void getMnemonic() {
    assertThat(applicationIsLongTermCondition.getMnemonic()).isEqualTo("APPLICATION_IS_LONG_TERM");
  }

  @Test
  void getTitle() {
    assertThat(applicationIsLongTermCondition.getTitle()).isEqualTo("Application is long term");
  }

  @Test
  void evaluate_consentLengthTypeIsNotLongTerm() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThat(applicationIsLongTermCondition.evaluate(documentInstanceDto)).isFalse();
  }

  @Test
  void evaluate_consentLengthTypeIsLongTerm() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThat(applicationIsLongTermCondition.evaluate(documentInstanceDto)).isTrue();
  }
}
