package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
}
