package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceControllerHelperServiceTest {

  @Mock
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private DocumentInstanceControllerHelperService documentInstanceControllerHelperService;

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @InjectMocks
  private FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService;

  @Captor
  private ArgumentCaptor<Function<DocumentInstanceDto, String>> viewUrlFunctionCaptor;

  @Test
  void getDocumentInstanceDtoForApplicationOrThrow_applicationIdEqualsDocumentInstanceApplicationId() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);
    var documentInstanceId = UUID.randomUUID();

    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    when(documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId)).thenReturn(documentInstanceDto);
    when(documentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(application.getId());

    assertThat(
        fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
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
    when(documentInstanceLinkingService.getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto))
        .thenReturn(1000);

    assertThatThrownBy(() ->
        fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
            application,
            documentInstanceId
        )
    ).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void getDocumentInstanceSummaryViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);

    var documentInstanceDtos = List.of(
        DocumentInstanceDtoTestUtil.builder().build(),
        DocumentInstanceDtoTestUtil.builder().build()
    );

    var documentInstanceSummaryViews = List.of(
        DocumentInstanceSummaryViewTestUtil.newBuilder().build(),
        DocumentInstanceSummaryViewTestUtil.newBuilder().build()
    );

    when(fieldConsentsDocumentInstanceService.getDocumentInstanceDtos(application)).thenReturn(documentInstanceDtos);
    when(
        documentInstanceControllerHelperService.getDocumentInstanceSummaryViews(
            eq(documentInstanceDtos),
            viewUrlFunctionCaptor.capture()
        )
    ).thenReturn(documentInstanceSummaryViews);

    assertThat(fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceSummaryViews(application))
        .isEqualTo(documentInstanceSummaryViews);

    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    assertThat(viewUrlFunctionCaptor.getValue())
        .isNotNull()
        .extracting(viewUrlFunction -> viewUrlFunction.apply(documentInstanceDto))
        .isEqualTo(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getViewDocumentInstance(application.getId(), documentInstanceDto.id())));
  }
}
