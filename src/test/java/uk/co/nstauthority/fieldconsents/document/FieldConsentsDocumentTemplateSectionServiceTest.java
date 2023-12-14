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
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentTemplateSectionServiceTest {

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @Mock
  private DocumentSectionService documentSectionService;

  @InjectMocks
  private FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService;

  @Test
  void getDocumentTemplateSectionSummaryViews() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var topLevelDocumentTemplateSectionDtos = List.of(DocumentTemplateSectionDtoTestUtil.builder().build());

    var sectionSummaryViewsForSectionSiblings =
        List.of(new DocumentSectionSummaryView(null, null, null, null, null, null, null));

    when(documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto))
        .thenReturn(topLevelDocumentTemplateSectionDtos);

    doReturn(sectionSummaryViewsForSectionSiblings)
        .when(documentSectionService)
        .getSectionSummaryViewsForSectionSiblings(null, topLevelDocumentTemplateSectionDtos);

    assertThat(fieldConsentsDocumentTemplateSectionService.getDocumentSectionSummaryViews(documentTemplateDto))
        .isEqualTo(sectionSummaryViewsForSectionSiblings);
  }

  @Test
  void createDocumentTemplateSection() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var parentDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var form = DocumentSectionFormTestUtil.builder().build();
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
    var form = DocumentSectionFormTestUtil.builder().build();

    fieldConsentsDocumentTemplateSectionService.editDocumentTemplateSection(documentTemplateSectionDto, form);

    verify(documentTemplateSectionService).editDocumentTemplateSection(
        documentTemplateSectionDto,
        form.title(),
        form.content()
    );
  }
}
