package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceControllerHelperServiceTest {

  @Mock
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @Mock
  private DocumentInstanceControllerHelperService documentInstanceControllerHelperService;

  @InjectMocks
  private FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService;

  @Captor
  private ArgumentCaptor<Function<DocumentInstanceDto, String>> viewUrlFunctionCaptor;

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
            .getViewDocumentInstance(documentInstanceDto.id())));
  }
}
