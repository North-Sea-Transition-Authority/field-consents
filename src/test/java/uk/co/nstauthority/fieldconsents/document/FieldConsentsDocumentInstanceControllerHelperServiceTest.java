package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceControllerHelperService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceControllerHelperServiceTest {

  @Mock
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @Mock
  private DocumentInstanceControllerHelperService documentInstanceControllerHelperService;

  @InjectMocks
  private FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService;

  @Test
  void getDocumentInstanceSummaryViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);

    var documentInstanceDtos = List.of(
        DocumentInstanceDtoTestUtil.builder().build(),
        DocumentInstanceDtoTestUtil.builder().build()
    );

    var documentInstanceSummaryViews = List.of(
        DocumentInstanceSummaryViewTestUtil.newBuilder().build(),
        DocumentInstanceSummaryViewTestUtil.newBuilder().build()
    );

    when(fieldConsentsDocumentInstanceService.getDocumentInstanceDtos(application)).thenReturn(documentInstanceDtos);
    when(
        documentInstanceControllerHelperService.getDocumentInstanceSummaryViews(
            documentInstanceDtos,
            FieldConsentsDocumentInstanceController.class
        )
    ).thenReturn(documentInstanceSummaryViews);

    assertThat(fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceSummaryViews(application))
        .isEqualTo(documentInstanceSummaryViews);
  }
}
