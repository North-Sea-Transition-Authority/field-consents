package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionServiceTest {

  @Mock
  private DocumentInstanceSectionRepository documentInstanceSectionRepository;

  @InjectMocks
  @Spy
  private DocumentInstanceSectionService documentInstanceSectionService;

  @Test
  void getDocumentInstanceSectionDto() {
    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    var documentInstanceSectionChild1 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection)
        .build();
    var documentInstanceSectionChild1Child1 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSectionChild1)
        .build();
    var documentInstanceSectionChild2 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection)
        .build();

    var allDocumentInstanceSections = List.of(
        documentInstanceSection,
        documentInstanceSectionChild1,
        documentInstanceSectionChild1Child1,
        documentInstanceSectionChild2
    );

    assertThat(
        documentInstanceSectionService.getDocumentInstanceSectionDto(
            documentInstanceSection,
            allDocumentInstanceSections
        )
    ).isEqualTo(
        DocumentInstanceSectionDto.from(
            documentInstanceSection,
            List.of(
                DocumentInstanceSectionDto.from(
                    documentInstanceSectionChild1,
                    List.of(DocumentInstanceSectionDto.from(documentInstanceSectionChild1Child1, List.of()))
                ),
                DocumentInstanceSectionDto.from(documentInstanceSectionChild2, List.of())
            )
        )
    );
  }

  @Test
  void getTopLevelDocumentInstanceSectionDtos() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstanceSection1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection2 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection3 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection2)
        .build();
    var documentInstanceSection4 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection3)
        .build();
    var documentInstanceSection5 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection2)
        .build();

    var allDocumentInstanceSections = List.of(
        documentInstanceSection1,
        documentInstanceSection2,
        documentInstanceSection3,
        documentInstanceSection4,
        documentInstanceSection5
    );

    var documentInstanceSectionDto1 = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionDto2 = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(documentInstanceSectionRepository.findAllByDocumentInstanceId(documentInstanceDto.id()))
        .thenReturn(allDocumentInstanceSections);
    doReturn(documentInstanceSectionDto1)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionDto(documentInstanceSection1, allDocumentInstanceSections);
    doReturn(documentInstanceSectionDto2)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionDto(documentInstanceSection2, allDocumentInstanceSections);

    assertThat(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto))
        .containsExactly(
            documentInstanceSectionDto1,
            documentInstanceSectionDto2
        );
  }
}
