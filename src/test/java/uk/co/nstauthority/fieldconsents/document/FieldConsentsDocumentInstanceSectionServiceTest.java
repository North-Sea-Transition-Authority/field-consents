package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceSectionServiceTest {

  @Mock
  private DocumentInstanceSectionService documentInstanceSectionService;

  @Mock
  private DocumentSectionService documentSectionService;

  @InjectMocks
  private FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @Test
  void getDocumentInstanceSectionSummaryViews() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var topLevelDocumentInstanceSectionDtos = List.of(DocumentInstanceSectionDtoTestUtil.builder().build());

    var sectionSummaryViewsForSectionSiblings =
        List.of(new DocumentSectionSummaryView(null, null, null, null, null, null, null));

    when(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto))
        .thenReturn(topLevelDocumentInstanceSectionDtos);

    doReturn(sectionSummaryViewsForSectionSiblings)
        .when(documentSectionService)
        .getSectionSummaryViewsForSectionSiblings(null, topLevelDocumentInstanceSectionDtos);

    assertThat(fieldConsentsDocumentInstanceSectionService.getDocumentSectionSummaryViews(documentInstanceDto))
        .isEqualTo(sectionSummaryViewsForSectionSiblings);
  }
}
