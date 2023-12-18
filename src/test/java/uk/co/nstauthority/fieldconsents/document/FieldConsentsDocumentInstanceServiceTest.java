package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceServiceTest {

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @InjectMocks
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @Test
  void renderPdf() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    Map<String, Object> expectedTemplateModel = Map.of(
        "documentSectionSummaryViews",
        fieldConsentsDocumentInstanceSectionService.getDocumentSectionSummaryViews(documentInstanceDto)
    );

    when(documentInstanceService.renderPdf(documentInstanceDto, expectedTemplateModel))
        .thenReturn(byteArrayResource);

    assertThat(fieldConsentsDocumentInstanceService.renderPdf(documentInstanceDto)).isEqualTo(byteArrayResource);
  }
}
