package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
class DocumentTemplateServiceTest {

  @Mock
  private DocumentTemplateRepository documentTemplateRepository;

  @InjectMocks
  @Spy
  private DocumentTemplateService documentTemplateService;

  @Test
  void createDocumentTemplate() {
    var title = "Test title";
    var description = "Test description";
    var templatePath = "test/template/path";
    var displayOrder = 1;

    var documentTemplateDto =
        documentTemplateService.createDocumentTemplate(title, description, templatePath, displayOrder);

    var documentTemplateCaptor = ArgumentCaptor.forClass(DocumentTemplate.class);

    verify(documentTemplateRepository).save(documentTemplateCaptor.capture());

    var documentTemplate = documentTemplateCaptor.getValue();

    assertThat(documentTemplate)
        .extracting(
            DocumentTemplate::getTitle,
            DocumentTemplate::getDescription,
            DocumentTemplate::getTemplatePath,
            DocumentTemplate::getDisplayOrder
        ).containsExactly(
            title,
            description,
            templatePath,
            displayOrder
        );

    assertThat(documentTemplateDto).isEqualTo(DocumentTemplateDto.from(documentTemplate));
  }

  @Test
  void getDocumentTemplateDtoOrThrow() {
    var documentTemplateId = UUID.randomUUID();

    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    doReturn(documentTemplate).when(documentTemplateService).getDocumentTemplateOrThrow(documentTemplateId);

    assertThat(documentTemplateService.getDocumentTemplateDtoOrThrow(documentTemplateId))
        .isEqualTo(DocumentTemplateDto.from(documentTemplate));
  }

  @Test
  void getDocumentTemplateOrThrow_documentTemplateDoesNotExist() {
    var documentTemplateId = UUID.randomUUID();

    when(documentTemplateRepository.findById(documentTemplateId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentTemplateService.getDocumentTemplateOrThrow(documentTemplateId))
        .isInstanceOf(DocumentTemplateNotFoundException.class);
  }

  @Test
  void getDocumentTemplateOrThrow_documentTemplateExists() {
    var documentTemplateId = UUID.randomUUID();

    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateRepository.findById(documentTemplateId)).thenReturn(Optional.of(documentTemplate));

    assertThat(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateId)).isEqualTo(documentTemplate);
  }

  @Test
  void getDocumentTemplateDtos() {
    var documentTemplate1 = DocumentTemplateTestUtil.builder().build();
    var documentTemplate2 = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateRepository.findAll()).thenReturn(List.of(documentTemplate1, documentTemplate2));

    assertThat(documentTemplateService.getDocumentTemplateDtos()).containsExactly(
        DocumentTemplateDto.from(documentTemplate1),
        DocumentTemplateDto.from(documentTemplate2)
    );
  }
}
