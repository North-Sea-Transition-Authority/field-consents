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
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentInstanceControllerHelperServiceTest {

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @InjectMocks
  private ApplicationDocumentInstanceControllerHelperService applicationDocumentInstanceControllerHelperService;

  @Test
  void getDocumentInstanceDtoForApplicationOrThrow_applicationIdEqualsDocumentInstanceApplicationId() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);
    var documentInstanceId = UUID.randomUUID();

    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    when(documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId)).thenReturn(documentInstanceDto);
    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(application.getId());

    assertThat(
        applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
            application,
            documentInstanceId
        )
    ).isEqualTo(documentInstanceDto);
  }

  @Test
  void getDocumentInstanceDtoForApplicationOrThrow_applicationIdDoesNotEqualDocumentInstanceApplicationId() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);
    var documentInstanceId = UUID.randomUUID();

    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    when(documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId)).thenReturn(documentInstanceDto);
    when(applicationDocumentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(1000);

    assertThatThrownBy(() ->
        applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
            application,
            documentInstanceId
        )
    ).isInstanceOf(ResponseStatusException.class);
  }
}
