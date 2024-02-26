package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceControllerHelperServiceTest {

  @InjectMocks
  private DocumentInstanceControllerHelperService documentInstanceControllerHelperService;

  @Test
  void getDocumentInstanceSummaryViews() {
    var documentTemplateDto1 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(1).build();
    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto1).build();

    var documentTemplateDto2 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(2).build();
    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto2).build();

    var documentTemplateDto3 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(3).build();
    var documentInstanceDto3 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto3).build();

    var documentInstanceDtos = List.of(documentInstanceDto2, documentInstanceDto1, documentInstanceDto3);

    assertThat(
        documentInstanceControllerHelperService.getDocumentInstanceSummaryViews(
            documentInstanceDtos,
            TestDocumentInstanceController.class
        )
    ).containsExactly(
        DocumentInstanceSummaryView.from(documentInstanceDto1, TestDocumentInstanceController.class),
        DocumentInstanceSummaryView.from(documentInstanceDto2, TestDocumentInstanceController.class),
        DocumentInstanceSummaryView.from(documentInstanceDto3, TestDocumentInstanceController.class)
    );
  }
}
