package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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
class DocumentTemplateSectionServiceTest {

  @Mock
  private DocumentTemplateSectionRepository documentTemplateSectionRepository;

  @Mock
  private DocumentTemplateService documentTemplateService;

  @InjectMocks
  @Spy
  private DocumentTemplateSectionService documentTemplateSectionService;

  @Test
  void createDocumentTemplateSection_nullParent() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var title = "Test title";
    var content = "Test content";
    var displayOrder = 1;

    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id())).thenReturn(documentTemplate);

    var documentTemplateSectionDto = documentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        null,
        title,
        content,
        displayOrder
    );

    var documentTemplateSectionCaptor = ArgumentCaptor.forClass(DocumentTemplateSection.class);

    verify(documentTemplateSectionRepository).save(documentTemplateSectionCaptor.capture());

    var documentTemplateSection = documentTemplateSectionCaptor.getValue();

    assertThat(documentTemplateSection.getDocumentTemplate()).isEqualTo(documentTemplate);
    assertThat(documentTemplateSection.getParent()).isNull();
    assertThat(documentTemplateSection.getTitle()).isEqualTo(title);
    assertThat(documentTemplateSection.getContent()).isEqualTo(content);
    assertThat(documentTemplateSection.getDisplayOrder()).isEqualTo(displayOrder);

    assertThat(documentTemplateSectionDto)
        .isEqualTo(DocumentTemplateSectionDto.from(documentTemplateSection, List.of()));
  }

  @Test
  void createDocumentTemplateSection_nonNullParent() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var parentDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    var title = "Test title";
    var content = "Test content";
    var displayOrder = 1;

    var documentTemplate = DocumentTemplateTestUtil.builder().build();
    var parent = DocumentTemplateSectionTestUtil.builder().build();

    when(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id())).thenReturn(documentTemplate);
    doReturn(parent).when(documentTemplateSectionService).getDocumentTemplateSectionOrThrow(parentDto.id());

    var documentTemplateSectionDto = documentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        title,
        content,
        displayOrder
    );

    var documentTemplateSectionCaptor = ArgumentCaptor.forClass(DocumentTemplateSection.class);

    verify(documentTemplateSectionRepository).save(documentTemplateSectionCaptor.capture());

    var documentTemplateSection = documentTemplateSectionCaptor.getValue();

    assertThat(documentTemplateSection.getDocumentTemplate()).isEqualTo(documentTemplate);
    assertThat(documentTemplateSection.getParent()).isEqualTo(parent);
    assertThat(documentTemplateSection.getTitle()).isEqualTo(title);
    assertThat(documentTemplateSection.getContent()).isEqualTo(content);
    assertThat(documentTemplateSection.getDisplayOrder()).isEqualTo(displayOrder);

    assertThat(documentTemplateSectionDto)
        .isEqualTo(DocumentTemplateSectionDto.from(documentTemplateSection, List.of()));
  }

  @Test
  void getDocumentTemplateSectionOrThrow_documentTemplateSectionDoesNotExist() {
    var documentTemplateSectionId = UUID.randomUUID();

    when(documentTemplateSectionRepository.findById(documentTemplateSectionId)).thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> documentTemplateSectionService.getDocumentTemplateSectionOrThrow(documentTemplateSectionId)
    ).isInstanceOf(DocumentTemplateNotFoundException.class);
  }

  @Test
  void getDocumentTemplateSectionOrThrow_documentTemplateSectionExists() {
    var documentTemplateSectionId = UUID.randomUUID();

    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();

    when(documentTemplateSectionRepository.findById(documentTemplateSectionId))
        .thenReturn(Optional.of(documentTemplateSection));

    assertThat(documentTemplateSectionService.getDocumentTemplateSectionOrThrow(documentTemplateSectionId))
        .isEqualTo(documentTemplateSection);
  }

  @Test
  void getDocumentTemplateSectionDtos() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSection1 = DocumentTemplateSectionTestUtil.builder().build();
    var documentTemplateSection2 = DocumentTemplateSectionTestUtil.builder().build();
    var documentTemplateSection3 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection2)
        .build();
    var documentTemplateSection4 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection3)
        .build();
    var documentTemplateSection5 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection2)
        .build();

    ListMultimap<UUID, DocumentTemplateSection> documentTemplateSectionsByParentId = ArrayListMultimap.create();
    documentTemplateSectionsByParentId.put(null, documentTemplateSection1);
    documentTemplateSectionsByParentId.put(null, documentTemplateSection2);
    documentTemplateSectionsByParentId.put(documentTemplateSection2.getId(), documentTemplateSection3);
    documentTemplateSectionsByParentId.put(documentTemplateSection3.getId(), documentTemplateSection4);
    documentTemplateSectionsByParentId.put(documentTemplateSection2.getId(), documentTemplateSection5);

    var documentTemplateSectionDto1 = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionDto2 = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplateDto.id())).thenReturn(
        List.of(
            documentTemplateSection1,
            documentTemplateSection2,
            documentTemplateSection3,
            documentTemplateSection4,
            documentTemplateSection5
        )
    );

    doReturn(documentTemplateSectionDto1)
        .when(documentTemplateSectionService)
        .getDocumentTemplateSectionDto(documentTemplateSection1, documentTemplateSectionsByParentId);
    doReturn(documentTemplateSectionDto2)
        .when(documentTemplateSectionService)
        .getDocumentTemplateSectionDto(documentTemplateSection2, documentTemplateSectionsByParentId);

    assertThat(documentTemplateSectionService.getDocumentTemplateSectionDtos(documentTemplateDto))
        .containsExactly(
            documentTemplateSectionDto1,
            documentTemplateSectionDto2
        );
  }

  @Test
  void getDocumentTemplateSectionDto() {
    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();

    var documentTemplateSectionChild1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();
    var documentTemplateSectionChild1Child1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSectionChild1)
        .build();
    var documentTemplateSectionChild2 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();

    ListMultimap<UUID, DocumentTemplateSection> documentTemplateSectionsByParentId = ArrayListMultimap.create();
    documentTemplateSectionsByParentId.put(null, documentTemplateSection);
    documentTemplateSectionsByParentId.put(documentTemplateSection.getId(), documentTemplateSectionChild1);
    documentTemplateSectionsByParentId.put(documentTemplateSectionChild1.getId(), documentTemplateSectionChild1Child1);
    documentTemplateSectionsByParentId.put(documentTemplateSection.getId(), documentTemplateSectionChild2);

    assertThat(
        documentTemplateSectionService.getDocumentTemplateSectionDto(
            documentTemplateSection,
            documentTemplateSectionsByParentId
        )
    ).isEqualTo(
        DocumentTemplateSectionDto.from(
            documentTemplateSection,
            List.of(
                DocumentTemplateSectionDto.from(
                    documentTemplateSectionChild1,
                    List.of(DocumentTemplateSectionDto.from(documentTemplateSectionChild1Child1, List.of()))
                ),
                DocumentTemplateSectionDto.from(documentTemplateSectionChild2, List.of())
            )
        )
    );
  }
}
