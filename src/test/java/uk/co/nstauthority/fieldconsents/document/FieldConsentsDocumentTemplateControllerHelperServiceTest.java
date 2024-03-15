package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSummaryView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentTemplateControllerHelperServiceTest {

  @Mock
  private DocumentTemplateControllerHelperService documentTemplateControllerHelperService;

  @InjectMocks
  private FieldConsentsDocumentTemplateControllerHelperService fieldConsentsDocumentTemplateControllerHelperService;

  @Captor
  private ArgumentCaptor<Function<DocumentTemplateDto, String>> viewUrlFunctionCaptor;

  @Test
  void getDocumentTemplateSummaryViews() {
    var documentTemplateSummaryViews = List.of(
        new DocumentTemplateSummaryView("Test title 1", "Test description 1", "test-view-url-1"),
        new DocumentTemplateSummaryView("Test title 2", "Test description 2", "test-view-url-2")
    );

    when(documentTemplateControllerHelperService.getDocumentTemplateSummaryViews(viewUrlFunctionCaptor.capture()))
        .thenReturn(documentTemplateSummaryViews);

    assertThat(fieldConsentsDocumentTemplateControllerHelperService.getDocumentTemplateSummaryViews())
        .isEqualTo(documentTemplateSummaryViews);

    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    assertThat(viewUrlFunctionCaptor.getValue())
        .isNotNull()
        .extracting(viewUrlFunction -> viewUrlFunction.apply(documentTemplateDto))
        .isEqualTo(ReverseRouter.route(on(FieldConsentsDocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id())));
  }
}
