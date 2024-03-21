package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentInstanceLinkingServiceTest {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";

  @Mock
  private ApplicationVersionService applicationVersionService;

  @InjectMocks
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @Test
  void getApplicationVersionFromDocumentInstanceDto() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withItemType(APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE)
        .withItemReference("1")
        .build();

    var applicationVersion = new ApplicationVersion();

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(Integer.parseInt(documentInstanceDto.itemReference())))
        .thenReturn(applicationVersion);

    assertThat(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .isEqualTo(applicationVersion);
  }

  @Test
  void getApplicationVersionFromDocumentInstanceDto_wrongItemType() {
    var wrongItemType = "wrong item type";
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withItemReference("1")
        .withItemType(wrongItemType)
        .build();

    assertThatThrownBy(() -> applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Expected itemType %s but found %s".formatted(
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE, wrongItemType
        ));
  }
}
