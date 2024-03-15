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
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionUrls;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldConsentsDocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceSectionControllerHelperServiceTest {

  @Mock
  private FieldConsentsDocumentMailMergeFieldFormatter fieldConsentsDocumentMailMergeFieldFormatter;

  @Mock
  private DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;

  @InjectMocks
  private FieldConsentsDocumentInstanceSectionControllerHelperService fieldConsentsDocumentInstanceSectionControllerHelperService;

  @Captor
  private ArgumentCaptor<Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls>> urlsFunctionCaptor;

  @Test
  void getDocumentInstanceSectionsSummaryView_useDocumentMailMergeFieldFormatterIsTrue() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstanceSectionsSummaryView = new DocumentInstanceSectionsSummaryView(List.of(), List.of());

    when(
        documentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            eq(documentInstanceDto),
            urlsFunctionCaptor.capture(),
            eq(fieldConsentsDocumentMailMergeFieldFormatter)
        )
    ).thenReturn(documentInstanceSectionsSummaryView);

    assertThat(
        fieldConsentsDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            documentInstanceDto,
            true
        )
    ).isEqualTo(documentInstanceSectionsSummaryView);

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    assertThat(urlsFunctionCaptor.getValue())
        .isNotNull()
        .extracting(viewUrlFunction -> viewUrlFunction.apply(documentInstanceSectionDto))
        .isEqualTo(
            new DocumentInstanceSectionUrls(
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionBefore(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionAfter(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSubsection(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getEditDocumentInstanceSection(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getRemoveDocumentInstanceSection(documentInstanceSectionId))
            )
        );
  }

  @Test
  void getDocumentInstanceSectionsSummaryView_useDocumentMailMergeFieldFormatterIsFalse() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstanceSectionsSummaryView = new DocumentInstanceSectionsSummaryView(List.of(), List.of());

    when(
        documentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            eq(documentInstanceDto),
            urlsFunctionCaptor.capture(),
            eq(DocumentMailMergeFieldFormatter.noOp())
        )
    ).thenReturn(documentInstanceSectionsSummaryView);

    assertThat(
        fieldConsentsDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            documentInstanceDto,
            false
        )
    ).isEqualTo(documentInstanceSectionsSummaryView);

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    assertThat(urlsFunctionCaptor.getValue())
        .isNotNull()
        .extracting(viewUrlFunction -> viewUrlFunction.apply(documentInstanceSectionDto))
        .isEqualTo(
            new DocumentInstanceSectionUrls(
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionBefore(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionAfter(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSubsection(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getEditDocumentInstanceSection(documentInstanceSectionId)),
                ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
                    .getRemoveDocumentInstanceSection(documentInstanceSectionId))
            )
        );
  }
}
