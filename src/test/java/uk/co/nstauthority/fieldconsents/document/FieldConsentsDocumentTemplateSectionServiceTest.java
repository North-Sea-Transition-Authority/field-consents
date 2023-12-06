package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentTemplateSectionServiceTest {

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @InjectMocks
  @Spy
  private FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService;

  @Test
  void getDocumentTemplateSectionSummaryViews() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSectionDtos = List.of(DocumentTemplateSectionDtoTestUtil.builder().build());

    var sectionSummaryViewsForSectionSiblings =
        List.of(new DocumentTemplateSectionSummaryView(null, null, null, null, null, null, null));

    when(documentTemplateSectionService.getDocumentTemplateSectionDtos(documentTemplateDto))
        .thenReturn(documentTemplateSectionDtos);

    doReturn(sectionSummaryViewsForSectionSiblings)
        .when(fieldConsentsDocumentTemplateSectionService)
        .getSectionSummaryViewsForSectionSiblings(null, documentTemplateSectionDtos);

    assertThat(fieldConsentsDocumentTemplateSectionService.getDocumentTemplateSectionSummaryViews(documentTemplateDto))
        .isEqualTo(sectionSummaryViewsForSectionSiblings);
  }

  @Test
  void getSectionSummaryViewsForSectionSiblings() {
    var parentSectionNumberString = "1";

    var siblingDocumentTemplateSectionDto1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDisplayOrder(1)
            .build();

    var siblingDocumentTemplateSectionDto2Child1Child1 =
        DocumentTemplateSectionDtoTestUtil.builder().build();
    var siblingDocumentTemplateSectionDto2Child1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withChildren(List.of(siblingDocumentTemplateSectionDto2Child1Child1))
            .withDisplayOrder(1)
            .build();
    var siblingDocumentTemplateSectionDto2Child2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDisplayOrder(2)
            .build();
    var siblingDocumentTemplateSectionDto2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withChildren(
                List.of(
                    siblingDocumentTemplateSectionDto2Child2,
                    siblingDocumentTemplateSectionDto2Child1
                )
            )
            .withDisplayOrder(2)
            .build();

    var siblingDocumentTemplateSectionDtos =
        List.of(siblingDocumentTemplateSectionDto1, siblingDocumentTemplateSectionDto2);

    assertThat(
        fieldConsentsDocumentTemplateSectionService.getSectionSummaryViewsForSectionSiblings(
            parentSectionNumberString,
            siblingDocumentTemplateSectionDtos
        )
    ).containsExactly(
        DocumentTemplateSectionSummaryView.from(
            "1.1",
            siblingDocumentTemplateSectionDto1
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.2",
            siblingDocumentTemplateSectionDto2
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.2.1",
            siblingDocumentTemplateSectionDto2Child1
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.2.1.1",
            siblingDocumentTemplateSectionDto2Child1Child1
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.2.2",
            siblingDocumentTemplateSectionDto2Child2
        )
    );
  }

  @Test
  void createDocumentTemplateSection() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var parentDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var form = DocumentTemplateSectionFormTestUtil.builder().build();
    int displayOrder = 1;

    fieldConsentsDocumentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form,
        displayOrder
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form.title(),
        form.content(),
        displayOrder
    );
  }

  @Test
  void editDocumentTemplateSection() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var form = DocumentTemplateSectionFormTestUtil.builder().build();

    fieldConsentsDocumentTemplateSectionService.editDocumentTemplateSection(documentTemplateSectionDto, form);

    verify(documentTemplateSectionService).editDocumentTemplateSection(
        documentTemplateSectionDto,
        form.title(),
        form.content()
    );
  }
}
