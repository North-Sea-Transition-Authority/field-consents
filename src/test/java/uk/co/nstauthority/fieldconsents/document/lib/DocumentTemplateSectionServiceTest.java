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
import org.mockito.Captor;
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

  @Captor
  private ArgumentCaptor<List<DocumentTemplateSection>> documentTemplateSectionListCaptor;

  @Test
  void createDocumentTemplateSection_nullParent() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var title = "Test title";
    var content = "Test content";
    var conditionMnemonic = "Test condition mnemonic";
    var numbered = true;
    var hasPageBreakBefore = false;
    var displayOrder = 1;

    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    var existingSibling1 = DocumentTemplateSectionTestUtil.builder()
        .withDisplayOrder(displayOrder)
        .build();
    var existingSibling2 = DocumentTemplateSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 1)
        .build();
    var existingSibling3 = DocumentTemplateSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 3)
        .build();

    when(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id())).thenReturn(documentTemplate);
    when(documentTemplateSectionRepository.findAllByParent_IdAndDisplayOrderGreaterThanEqual(null, displayOrder))
        .thenReturn(List.of(existingSibling1, existingSibling2, existingSibling3));

    var documentTemplateSectionDto = documentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        null,
        title,
        content,
        conditionMnemonic,
        numbered,
        hasPageBreakBefore,
        displayOrder
    );

    assertThat(existingSibling1.getDisplayOrder()).isEqualTo(displayOrder + 1);
    assertThat(existingSibling2.getDisplayOrder()).isEqualTo(displayOrder + 2);

    verify(documentTemplateSectionRepository).saveAll(documentTemplateSectionListCaptor.capture());

    var savedDocumentTemplateSections = documentTemplateSectionListCaptor.getValue();

    assertThat(savedDocumentTemplateSections).hasSize(3);

    var newDocumentTemplateSection = savedDocumentTemplateSections.get(0);

    assertThat(newDocumentTemplateSection)
        .extracting(
            DocumentTemplateSection::getDocumentTemplate,
            DocumentTemplateSection::getParent,
            DocumentTemplateSection::getTitle,
            DocumentTemplateSection::getContent,
            DocumentTemplateSection::getConditionMnemonic,
            DocumentTemplateSection::isNumbered,
            DocumentTemplateSection::hasPageBreakBefore,
            DocumentTemplateSection::getDisplayOrder
        )
        .containsExactly(
            documentTemplate,
            null,
            title,
            content,
            conditionMnemonic,
            numbered,
            hasPageBreakBefore,
            displayOrder
        );

    assertThat(savedDocumentTemplateSections.get(1)).isEqualTo(existingSibling1);
    assertThat(savedDocumentTemplateSections.get(2)).isEqualTo(existingSibling2);

    assertThat(documentTemplateSectionDto)
        .isEqualTo(DocumentTemplateSectionDto.from(newDocumentTemplateSection, List.of()));
  }

  @Test
  void createDocumentTemplateSection_nonNullParent() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var parentDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var parentDtoId = parentDto.id();

    var title = "Test title";
    var content = "Test content";
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";
    var numbered = true;
    var hasPageBreakBefore = false;
    var displayOrder = 1;

    var documentTemplate = DocumentTemplateTestUtil.builder().build();
    var parent = DocumentTemplateSectionTestUtil.builder().build();

    var existingSibling1 = DocumentTemplateSectionTestUtil.builder()
        .withDisplayOrder(displayOrder)
        .build();
    var existingSibling2 = DocumentTemplateSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 1)
        .build();
    var existingSibling3 = DocumentTemplateSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 3)
        .build();

    when(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id())).thenReturn(documentTemplate);
    doReturn(parent).when(documentTemplateSectionService).getDocumentTemplateSectionOrThrow(parentDtoId);
    when(documentTemplateSectionRepository.findAllByParent_IdAndDisplayOrderGreaterThanEqual(parentDtoId, displayOrder))
        .thenReturn(List.of(existingSibling1, existingSibling2, existingSibling3));

    var documentTemplateSectionDto = documentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        title,
        content,
        conditionMnemonic,
        numbered,
        hasPageBreakBefore,
        displayOrder
    );

    assertThat(existingSibling1.getDisplayOrder()).isEqualTo(displayOrder + 1);
    assertThat(existingSibling2.getDisplayOrder()).isEqualTo(displayOrder + 2);

    verify(documentTemplateSectionRepository).saveAll(documentTemplateSectionListCaptor.capture());

    var savedDocumentTemplateSections = documentTemplateSectionListCaptor.getValue();

    assertThat(savedDocumentTemplateSections).hasSize(3);

    var newDocumentTemplateSection = savedDocumentTemplateSections.get(0);

    assertThat(newDocumentTemplateSection)
        .extracting(
            DocumentTemplateSection::getDocumentTemplate,
            DocumentTemplateSection::getParent,
            DocumentTemplateSection::getTitle,
            DocumentTemplateSection::getContent,
            DocumentTemplateSection::getConditionMnemonic,
            DocumentTemplateSection::isNumbered,
            DocumentTemplateSection::hasPageBreakBefore,
            DocumentTemplateSection::getDisplayOrder
        )
        .containsExactly(
            documentTemplate,
            parent,
            title,
            content,
            conditionMnemonic,
            numbered,
            hasPageBreakBefore,
            displayOrder
        );

    assertThat(savedDocumentTemplateSections.get(1)).isEqualTo(existingSibling1);
    assertThat(savedDocumentTemplateSections.get(2)).isEqualTo(existingSibling2);

    assertThat(documentTemplateSectionDto)
        .isEqualTo(DocumentTemplateSectionDto.from(newDocumentTemplateSection, List.of()));
  }

  @Test
  void editDocumentTemplateSection() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var title = "Tedt edited title";
    var content = "Test edited content";
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";
    var numbered = true;
    var hasPageBreakBefore = false;

    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();

    doReturn(documentTemplateSection)
        .when(documentTemplateSectionService)
        .getDocumentTemplateSectionOrThrow(documentTemplateSectionDto.id());

    documentTemplateSectionService.editDocumentTemplateSection(
        documentTemplateSectionDto,
        title,
        content,
        conditionMnemonic,
        numbered,
        hasPageBreakBefore
    );

    assertThat(documentTemplateSection)
        .extracting(
            DocumentTemplateSection::getTitle,
            DocumentTemplateSection::getContent,
            DocumentTemplateSection::getConditionMnemonic,
            DocumentTemplateSection::isNumbered,
            DocumentTemplateSection::hasPageBreakBefore
        )
        .containsExactly(
            title,
            content,
            conditionMnemonic,
            numbered,
            hasPageBreakBefore
        );

    verify(documentTemplateSectionRepository).save(documentTemplateSection);
  }

  @Test
  void deleteDocumentTemplateSection() {
    var documentTemplateSectionDtoChild1Child1 = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionDtoChild1 = DocumentTemplateSectionDtoTestUtil.builder()
        .withChildren(List.of(documentTemplateSectionDtoChild1Child1))
        .build();
    var documentTemplateSectionDtoChild2 = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withChildren(List.of(documentTemplateSectionDtoChild1, documentTemplateSectionDtoChild2))
        .build();

    documentTemplateSectionService.deleteDocumentTemplateSection(documentTemplateSectionDto);

    verify(documentTemplateSectionRepository).deleteAllById(
        List.of(
            documentTemplateSectionDto.id(),
            documentTemplateSectionDtoChild1.id(),
            documentTemplateSectionDtoChild1Child1.id(),
            documentTemplateSectionDtoChild2.id()
        )
    );
  }

  @Test
  void getDocumentTemplateSectionDtoOrThrow() {
    var documentTemplateSectionId = UUID.randomUUID();

    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();

    var allDocumentTemplateSections = List.of(documentTemplateSection);

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    doReturn(documentTemplateSection)
        .when(documentTemplateSectionService)
        .getDocumentTemplateSectionOrThrow(documentTemplateSectionId);
    when(documentTemplateSectionRepository.findAllByDocumentTemplateId(
        documentTemplateSection.getDocumentTemplate().getId())
    ).thenReturn(allDocumentTemplateSections);
    doReturn(documentTemplateSectionDto)
        .when(documentTemplateSectionService)
        .getDocumentTemplateSectionDto(documentTemplateSection, allDocumentTemplateSections);

    assertThat(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId))
        .isEqualTo(documentTemplateSectionDto);
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

    var allDocumentTemplateSections = List.of(
        documentTemplateSection,
        documentTemplateSectionChild1,
        documentTemplateSectionChild1Child1,
        documentTemplateSectionChild2
    );

    assertThat(
        documentTemplateSectionService.getDocumentTemplateSectionDto(
            documentTemplateSection,
            allDocumentTemplateSections
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

  @Test
  void getDocumentTemplateSectionOrThrow_documentTemplateSectionDoesNotExist() {
    var documentTemplateSectionId = UUID.randomUUID();

    when(documentTemplateSectionRepository.findById(documentTemplateSectionId)).thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> documentTemplateSectionService.getDocumentTemplateSectionOrThrow(documentTemplateSectionId)
    ).isInstanceOf(DocumentTemplateSectionNotFoundException.class);
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
  void getTopLevelDocumentTemplateSectionDtos() {
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

    var allDocumentTemplateSections = List.of(
        documentTemplateSection1,
        documentTemplateSection2,
        documentTemplateSection3,
        documentTemplateSection4,
        documentTemplateSection5
    );

    var documentTemplateSectionDto1 = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionDto2 = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplateDto.id()))
        .thenReturn(allDocumentTemplateSections);
    doReturn(documentTemplateSectionDto1)
        .when(documentTemplateSectionService)
        .getDocumentTemplateSectionDto(documentTemplateSection1, allDocumentTemplateSections);
    doReturn(documentTemplateSectionDto2)
        .when(documentTemplateSectionService)
        .getDocumentTemplateSectionDto(documentTemplateSection2, allDocumentTemplateSections);

    assertThat(documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto))
        .containsExactly(
            documentTemplateSectionDto1,
            documentTemplateSectionDto2
        );
  }

  @Test
  void getDocumentTemplateSections() {
    var documentTemplate = DocumentTemplateTestUtil.builder().build();
    var documentTemplateSections = List.of(
        DocumentTemplateSectionTestUtil.builder().build(),
        DocumentTemplateSectionTestUtil.builder().build()
    );

    when(documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplate.getId()))
        .thenReturn(documentTemplateSections);

    assertThat(documentTemplateSectionService.getDocumentTemplateSections(documentTemplate))
        .isEqualTo(documentTemplateSections);
  }
}
