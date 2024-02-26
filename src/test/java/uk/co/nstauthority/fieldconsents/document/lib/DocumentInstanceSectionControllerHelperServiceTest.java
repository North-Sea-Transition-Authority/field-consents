package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionControllerHelperServiceTest {

  @Mock
  private DocumentInstanceSectionService documentInstanceSectionService;

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  @Spy
  private DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;

  @Test
  void getDocumentInstanceSectionSummaryViews() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var topLevelDocumentInstanceSectionDtos = List.of(DocumentInstanceSectionDtoTestUtil.builder().build());

    var documentInstanceSectionSummaryViewsForSectionSiblings = List.of(mock(DocumentInstanceSectionSummaryView.class));

    when(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto))
        .thenReturn(topLevelDocumentInstanceSectionDtos);

    doReturn(documentInstanceSectionSummaryViewsForSectionSiblings)
        .when(documentInstanceSectionControllerHelperService)
        .getDocumentInstanceSectionSummaryViewsForSectionSiblings(
            null,
            topLevelDocumentInstanceSectionDtos,
            TestDocumentInstanceSectionController.class
        );

    assertThat(
        documentInstanceSectionControllerHelperService.getDocumentInstanceSectionSummaryViews(
            documentInstanceDto,
            TestDocumentInstanceSectionController.class
        )
    ).isEqualTo(documentInstanceSectionSummaryViewsForSectionSiblings);
  }

  @Test
  void getDocumentInstanceSectionSummaryViewsForSectionSiblings() {
    var parentSectionNumberString = "1";

    var siblingDocumentInstanceSectionDto1 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(1)
            .build();

    var siblingDocumentInstanceSectionDto2Child1Child1 = DocumentInstanceSectionDtoTestUtil.builder().build();

    var siblingDocumentInstanceSectionDto2Child1 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(1)
            .withChildren(List.of(siblingDocumentInstanceSectionDto2Child1Child1))
            .build();
    var siblingDocumentInstanceSectionDto2Child2 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(2)
            .build();
    var siblingDocumentInstanceSectionDto2Child3 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(3)
            .build();

    var siblingDocumentInstanceSectionDto2 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(2)
            .withChildren(
                List.of(
                    siblingDocumentInstanceSectionDto2Child2,
                    siblingDocumentInstanceSectionDto2Child3,
                    siblingDocumentInstanceSectionDto2Child1
                )
            )
            .build();

    var siblingDocumentInstanceSectionDtos =
        List.of(siblingDocumentInstanceSectionDto1, siblingDocumentInstanceSectionDto2);

    var siblingDocumentInstanceSectionDto1ResolvedContent = "Test content 1";
    var siblingDocumentInstanceSectionDto2ResolvedContent = "Test content 2";
    var siblingDocumentInstanceSectionDto2Child1ResolvedContent = "Test content 3";
    var siblingDocumentInstanceSectionDto2Child1Child1ResolvedContent = "Test content 4";
    var siblingDocumentInstanceSectionDto2Child2ResolvedContent = "Test content 5";
    var siblingDocumentInstanceSectionDto2Child3ResolvedContent = "Test content 6";

    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto1))
        .thenReturn(siblingDocumentInstanceSectionDto1ResolvedContent);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2))
        .thenReturn(siblingDocumentInstanceSectionDto2ResolvedContent);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child1))
        .thenReturn(siblingDocumentInstanceSectionDto2Child1ResolvedContent);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child1Child1))
        .thenReturn(siblingDocumentInstanceSectionDto2Child1Child1ResolvedContent);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child2))
        .thenReturn(siblingDocumentInstanceSectionDto2Child2ResolvedContent);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child3))
        .thenReturn(siblingDocumentInstanceSectionDto2Child3ResolvedContent);

    assertThat(
        documentInstanceSectionControllerHelperService.getDocumentInstanceSectionSummaryViewsForSectionSiblings(
            parentSectionNumberString,
            siblingDocumentInstanceSectionDtos,
            TestDocumentInstanceSectionController.class
        )
    ).containsExactly(
        DocumentInstanceSectionSummaryView.from(
            null,
            siblingDocumentInstanceSectionDto1,
            siblingDocumentInstanceSectionDto1ResolvedContent,
            TestDocumentInstanceSectionController.class
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.1",
            siblingDocumentInstanceSectionDto2,
            siblingDocumentInstanceSectionDto2ResolvedContent,
            TestDocumentInstanceSectionController.class
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.1.1",
            siblingDocumentInstanceSectionDto2Child1,
            siblingDocumentInstanceSectionDto2Child1ResolvedContent,
            TestDocumentInstanceSectionController.class
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.1.1.1",
            siblingDocumentInstanceSectionDto2Child1Child1,
            siblingDocumentInstanceSectionDto2Child1Child1ResolvedContent,
            TestDocumentInstanceSectionController.class
        ),
        DocumentInstanceSectionSummaryView.from(
            null,
            siblingDocumentInstanceSectionDto2Child2,
            siblingDocumentInstanceSectionDto2Child2ResolvedContent,
            TestDocumentInstanceSectionController.class
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.1.2",
            siblingDocumentInstanceSectionDto2Child3,
            siblingDocumentInstanceSectionDto2Child3ResolvedContent,
            TestDocumentInstanceSectionController.class
        )
    );
  }

  @Test
  void createDocumentInstanceSection() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var parentDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();
    int displayOrder = 1;

    documentInstanceSectionControllerHelperService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form,
        displayOrder
    );

    verify(documentInstanceSectionService).createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore(),
        displayOrder
    );
  }

  @Test
  void editDocumentInstanceSection() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();

    documentInstanceSectionControllerHelperService.editDocumentInstanceSection(documentInstanceSectionDto, form);

    verify(documentInstanceSectionService).editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore()
    );
  }
}
