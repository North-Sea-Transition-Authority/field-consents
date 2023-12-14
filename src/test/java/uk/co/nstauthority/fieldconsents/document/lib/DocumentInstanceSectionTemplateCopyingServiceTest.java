package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionTemplateCopyingServiceTest {

  @Mock
  private DocumentInstanceSectionRepository documentInstanceSectionRepository;

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @InjectMocks
  private DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService;

  @Captor
  private ArgumentCaptor<Collection<DocumentInstanceSection>> documentInstanceSectionCollectionCaptor;

  @Test
  void copyDocumentTemplateSectionsToDocumentInstance() {
    var documentTemplate = DocumentTemplateTestUtil.builder().build();
    var documentInstance = DocumentInstanceTestUtil.builder().build();

    var documentTemplateSection1 = DocumentTemplateSectionTestUtil.builder()
        .withTitle("Test title 1")
        .withContent("Test content 1")
        .withDisplayOrder(1)
        .build();
    var documentTemplateSection2 = DocumentTemplateSectionTestUtil.builder()
        .withTitle("Test title 2")
        .withContent("Test content 2")
        .withDisplayOrder(2)
        .build();
    var documentTemplateSection3 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection2)
        .withTitle("Test title 3")
        .withContent("Test content 3")
        .withDisplayOrder(3)
        .build();
    var documentTemplateSection4 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection3)
        .withTitle("Test title 4")
        .withContent("Test content 4")
        .withDisplayOrder(4)
        .build();
    var documentTemplateSection5 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection2)
        .withTitle("Test title 5")
        .withContent("Test content 5")
        .withDisplayOrder(5)
        .build();

    var documentTemplateSections = List.of(
        documentTemplateSection1,
        documentTemplateSection2,
        documentTemplateSection3,
        documentTemplateSection4,
        documentTemplateSection5
    );

    when(documentTemplateSectionService.getDocumentTemplateSections(documentTemplate))
        .thenReturn(documentTemplateSections);

    documentInstanceSectionTemplateCopyingService
        .copyDocumentTemplateSectionsToDocumentInstance(documentTemplate, documentInstance);

    verify(documentInstanceSectionRepository).saveAll(documentInstanceSectionCollectionCaptor.capture());

    var documentInstanceSections = documentInstanceSectionCollectionCaptor.getValue();

    var documentInstanceSectionsByTemplateSection = documentInstanceSections.stream()
        .collect(Collectors.toMap(DocumentInstanceSection::getCreatedFromDocumentTemplateSection, Function.identity()));

    assertThat(documentInstanceSections)
        .extracting(
            DocumentInstanceSection::getDocumentInstance,
            DocumentInstanceSection::getCreatedFromDocumentTemplateSection,
            DocumentInstanceSection::getParent,
            DocumentInstanceSection::getTitle,
            DocumentInstanceSection::getContent,
            DocumentInstanceSection::getDisplayOrder
        )
        .containsExactlyInAnyOrder(
            tuple(
                documentInstance,
                documentTemplateSection1,
                null,
                documentTemplateSection1.getTitle(),
                documentTemplateSection1.getContent(),
                documentTemplateSection1.getDisplayOrder()
            ),
            tuple(
                documentInstance,
                documentTemplateSection2,
                null,
                documentTemplateSection2.getTitle(),
                documentTemplateSection2.getContent(),
                documentTemplateSection2.getDisplayOrder()
            ),
            tuple(
                documentInstance,
                documentTemplateSection3,
                documentInstanceSectionsByTemplateSection.get(documentTemplateSection3.getParent()),
                documentTemplateSection3.getTitle(),
                documentTemplateSection3.getContent(),
                documentTemplateSection3.getDisplayOrder()
            ),
            tuple(
                documentInstance,
                documentTemplateSection4,
                documentInstanceSectionsByTemplateSection.get(documentTemplateSection4.getParent()),
                documentTemplateSection4.getTitle(),
                documentTemplateSection4.getContent(),
                documentTemplateSection4.getDisplayOrder()
            ),
            tuple(
                documentInstance,
                documentTemplateSection5,
                documentInstanceSectionsByTemplateSection.get(documentTemplateSection5.getParent()),
                documentTemplateSection5.getTitle(),
                documentTemplateSection5.getContent(),
                documentTemplateSection5.getDisplayOrder()
            )
        );
  }
}
