package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationReferenceMailMergeFieldTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private ApplicationService applicationService;

  @InjectMocks
  private ApplicationReferenceMailMergeField applicationReferenceMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(applicationReferenceMailMergeField.getMnemonic()).isEqualTo("APPLICATION_REFERENCE");
  }

  @Test
  void getDescription() {
    assertThat(applicationReferenceMailMergeField.getDescription())
        .isEqualTo("The reference assigned to the application");
  }

  @Test
  void isApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    assertThat(applicationReferenceMailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "Test/application/reference";

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    assertThat(applicationReferenceMailMergeField.resolve(documentInstanceDto)).isEqualTo(applicationReference);
  }
}
