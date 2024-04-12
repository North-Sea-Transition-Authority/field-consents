package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentInstanceSectionControllerHelperServiceTest {

  @Mock
  private DocumentInstanceSectionService documentInstanceSectionService;

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @InjectMocks
  private ApplicationDocumentInstanceSectionControllerHelperService applicationDocumentInstanceSectionControllerHelperService;

  @Test
  void getDocumentInstanceSectionDtoForApplicationOrThrow_applicationIdEqualsDocumentInstanceApplicationId() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);
    var documentInstanceSectionId = UUID.randomUUID();

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId))
        .thenReturn(documentInstanceSectionDto);
    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(application.getId());

    assertThat(
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        )
    ).isEqualTo(documentInstanceSectionDto);
  }

  @Test
  void getDocumentInstanceSectionDtoForApplicationOrThrow_applicationIdDoesNotEqualDocumentInstanceApplicationId() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);
    var documentInstanceSectionId = UUID.randomUUID();

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId))
        .thenReturn(documentInstanceSectionDto);
    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(1000);

    assertThatThrownBy(() ->
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        )
    ).isInstanceOf(ResponseStatusException.class);
  }
}
