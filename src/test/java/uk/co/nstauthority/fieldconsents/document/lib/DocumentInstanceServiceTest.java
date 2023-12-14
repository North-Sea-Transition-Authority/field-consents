package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceServiceTest {

  @Mock
  private DocumentInstanceRepository documentInstanceRepository;

  @Mock
  private DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService;

  @Mock
  private DocumentTemplateService documentTemplateService;

  @InjectMocks
  @Spy
  private DocumentInstanceService documentInstanceService;

  @Test
  void createDocumentInstance() {
    var itemReference = "TEST_ITEM_REFERENCE";
    var itemType = "TEST_ITEM_TYPE";
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id())).thenReturn(documentTemplate);

    var documentInstanceDto =
        documentInstanceService.createDocumentInstance(itemReference, itemType, documentTemplateDto);

    var documentInstanceCaptor = ArgumentCaptor.forClass(DocumentInstance.class);

    verify(documentInstanceRepository).save(documentInstanceCaptor.capture());

    var documentInstance = documentInstanceCaptor.getValue();

    assertThat(documentInstance)
        .extracting(
            DocumentInstance::getItemReference,
            DocumentInstance::getItemType,
            DocumentInstance::getDocumentTemplate
        )
        .containsExactly(
            itemReference,
            itemType,
            documentTemplate
        );

    verify(documentInstanceSectionTemplateCopyingService).copyDocumentTemplateSectionsToDocumentInstance(
        documentTemplate,
        documentInstance
    );

    assertThat(documentInstanceDto).isEqualTo(DocumentInstanceDto.from(documentInstance));
  }

  @Test
  void getDocumentInstanceDtoOrThrow() {
    var documentInstanceId = UUID.randomUUID();

    var documentInstance = DocumentInstanceTestUtil.builder().build();

    doReturn(documentInstance).when(documentInstanceService).getDocumentInstanceOrThrow(documentInstanceId);

    assertThat(documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId))
        .isEqualTo(DocumentInstanceDto.from(documentInstance));
  }

  @Test
  void getDocumentInstanceOrThrow_documentInstanceDoesNotExist() {
    var documentInstanceId = UUID.randomUUID();

    when(documentInstanceRepository.findById(documentInstanceId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentInstanceService.getDocumentInstanceOrThrow(documentInstanceId))
        .isInstanceOf(DocumentInstanceNotFoundException.class);
  }

  @Test
  void getDocumentInstanceOrThrow_documentInstanceExists() {
    var documentInstanceId = UUID.randomUUID();

    var documentInstance = DocumentInstanceTestUtil.builder().build();

    when(documentInstanceRepository.findById(documentInstanceId)).thenReturn(Optional.of(documentInstance));

    assertThat(documentInstanceService.getDocumentInstanceOrThrow(documentInstanceId)).isEqualTo(documentInstance);
  }
}
