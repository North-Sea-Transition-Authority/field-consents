package uk.co.nstauthority.fieldconsents.document.template;

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
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionUrls;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentTemplateSectionControllerHelperServiceTest {

  @Mock
  private DocumentTemplateSectionControllerHelperService documentTemplateSectionControllerHelperService;

  @InjectMocks
  private FieldConsentsDocumentTemplateSectionControllerHelperService fieldConsentsDocumentTemplateSectionControllerHelperService;

  @Captor
  private ArgumentCaptor<Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls>> urlsFunctionCaptor;

  @Test
  void getDocumentTemplateSectionSummaryViews() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSectionSummaryViews = List.of(
        new DocumentTemplateSectionSummaryView(
            "1",
            "Test title",
            "Test content",
            "TEST_CONDITION_TITLE",
            false,
            DocumentTemplateSectionUrlsTestUtil.newBuilder().build(),
            List.of()
        )
    );

    when(
        documentTemplateSectionControllerHelperService.getDocumentTemplateSectionSummaryViews(
            eq(documentTemplateDto),
            urlsFunctionCaptor.capture()
        )
    ).thenReturn(documentTemplateSectionSummaryViews);

    assertThat(
        fieldConsentsDocumentTemplateSectionControllerHelperService.getDocumentTemplateSectionSummaryViews(documentTemplateDto)
    ).isEqualTo(documentTemplateSectionSummaryViews);

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionId = documentTemplateSectionDto.id();

    assertThat(urlsFunctionCaptor.getValue())
        .isNotNull()
        .extracting(viewUrlFunction -> viewUrlFunction.apply(documentTemplateSectionDto))
        .isEqualTo(
            new DocumentTemplateSectionUrls(
                ReverseRouter.route(on(DocumentTemplateSectionController.class)
                    .getAddDocumentTemplateSectionBefore(documentTemplateSectionId)),
                ReverseRouter.route(on(DocumentTemplateSectionController.class)
                    .getAddDocumentTemplateSectionAfter(documentTemplateSectionId)),
                ReverseRouter.route(on(DocumentTemplateSectionController.class)
                    .getAddDocumentTemplateSubsection(documentTemplateSectionId)),
                ReverseRouter.route(on(DocumentTemplateSectionController.class)
                    .getEditDocumentTemplateSection(documentTemplateSectionId)),
                ReverseRouter.route(on(DocumentTemplateSectionController.class)
                    .getRemoveDocumentTemplateSection(documentTemplateSectionId))
            )
        );
  }
}
