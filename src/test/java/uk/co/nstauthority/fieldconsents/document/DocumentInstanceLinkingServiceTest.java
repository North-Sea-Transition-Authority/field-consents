package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceLinkingServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @InjectMocks
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Test
  void getApplicationVersionFromDocumentInstanceDto() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withItemReference("1")
        .build();

    var applicationVersion = new ApplicationVersion();

    when(applicationVersionService.getApplicationVersionById(Integer.parseInt(documentInstanceDto.itemReference())))
        .thenReturn(applicationVersion);

    assertThat(documentInstanceLinkingService.getApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .isEqualTo(applicationVersion);
  }
}
