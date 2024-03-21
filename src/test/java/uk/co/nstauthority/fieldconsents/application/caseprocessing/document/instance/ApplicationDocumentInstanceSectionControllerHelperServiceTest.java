package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

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
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionUrls;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldConsentsDocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentInstanceSectionControllerHelperServiceTest {

  @Mock
  private FieldConsentsDocumentMailMergeFieldFormatter fieldConsentsDocumentMailMergeFieldFormatter;

  @Mock
  private DocumentInstanceSectionService documentInstanceSectionService;

  @Mock
  private DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @InjectMocks
  private ApplicationDocumentInstanceSectionControllerHelperService applicationDocumentInstanceSectionControllerHelperService;

  @Captor
  private ArgumentCaptor<Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls>> urlsFunctionCaptor;

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

  @Test
  void getDocumentInstanceSectionsSummaryView_useDocumentMailMergeFieldFormatterIsTrue() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
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
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            application,
            documentInstanceDto,
            true
        )
    ).isEqualTo(documentInstanceSectionsSummaryView);

    var applicationId = application.getId();
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    assertThat(urlsFunctionCaptor.getValue())
        .isNotNull()
        .extracting(viewUrlFunction -> viewUrlFunction.apply(documentInstanceSectionDto))
        .isEqualTo(
            new DocumentInstanceSectionUrls(
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionBefore(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionAfter(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSubsection(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getEditDocumentInstanceSection(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getRemoveDocumentInstanceSection(applicationId, documentInstanceSectionId))
            )
        );
  }

  @Test
  void getDocumentInstanceSectionsSummaryView_useDocumentMailMergeFieldFormatterIsFalse() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
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
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            application,
            documentInstanceDto,
            false
        )
    ).isEqualTo(documentInstanceSectionsSummaryView);

    var applicationId = application.getId();
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    assertThat(urlsFunctionCaptor.getValue())
        .isNotNull()
        .extracting(viewUrlFunction -> viewUrlFunction.apply(documentInstanceSectionDto))
        .isEqualTo(
            new DocumentInstanceSectionUrls(
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionBefore(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionAfter(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSubsection(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getEditDocumentInstanceSection(applicationId, documentInstanceSectionId)),
                ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
                    .getRemoveDocumentInstanceSection(applicationId, documentInstanceSectionId))
            )
        );
  }
}
