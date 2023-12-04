package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentTemplateServiceTest {

  @Mock
  private DocumentTemplateService documentTemplateService;

  @InjectMocks
  private FieldConsentsDocumentTemplateService fieldConsentsDocumentTemplateService;

  @Test
  void getDocumentTemplateSummaryViews() {
    var documentTemplateDto1 = DocumentTemplateDtoTestUtil.builder()
        .withDisplayOrder(1)
        .build();
    var documentTemplateDto2 = DocumentTemplateDtoTestUtil.builder()
        .withDisplayOrder(2)
        .build();
    var documentTemplateDto3 = DocumentTemplateDtoTestUtil.builder()
        .withDisplayOrder(3)
        .build();

    when(documentTemplateService.getDocumentTemplateDtos())
        .thenReturn(List.of(documentTemplateDto2, documentTemplateDto1, documentTemplateDto3));

    assertThat(fieldConsentsDocumentTemplateService.getDocumentTemplateSummaryViews()).containsExactly(
        DocumentTemplateSummaryView.from(documentTemplateDto1),
        DocumentTemplateSummaryView.from(documentTemplateDto2),
        DocumentTemplateSummaryView.from(documentTemplateDto3)
    );
  }
}
