package uk.co.nstauthority.fieldconsents.document.template.sectioncondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class ApplicationIsRevisionConditionTest {

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @InjectMocks
  private ApplicationIsRevisionCondition applicationIsRevisionCondition;

  @Test
  void getMnemonic() {
    assertThat(applicationIsRevisionCondition.getMnemonic()).isEqualTo("APPLICATION_IS_REVISION");
  }

  @Test
  void getTitle() {
    assertThat(applicationIsRevisionCondition.getTitle()).isEqualTo("Application is revision");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(applicationIsRevisionCondition.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void evaluate_revisionTypeIsNewConsent() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    application.setVariationNo(0);

    when(applicationDocumentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(application);

    assertThat(applicationIsRevisionCondition.evaluate(documentInstanceDto)).isFalse();
  }

  @Test
  void evaluate_revisionTypeIsRevision() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    application.setVariationNo(1);

    when(applicationDocumentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(application);

    assertThat(applicationIsRevisionCondition.evaluate(documentInstanceDto)).isTrue();
  }
}
