package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentInstanceSectionDtoTest {

  @Test
  void from_sectionDoesNotHaveCreatedFromDocumentTemplateSectionOrParent() {
    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    documentInstanceSection.setCreatedFromDocumentTemplateSection(null);
    documentInstanceSection.setParent(null);

    var children = List.of(
        DocumentInstanceSectionDtoTestUtil.builder().build(),
        DocumentInstanceSectionDtoTestUtil.builder().build()
    );

    assertThat(DocumentInstanceSectionDto.from(documentInstanceSection, children)).isEqualTo(
        new DocumentInstanceSectionDto(
            documentInstanceSection.getId(),
            DocumentInstanceDto.from(documentInstanceSection.getDocumentInstance()),
            null,
            null,
            documentInstanceSection.getTitle(),
            documentInstanceSection.getContent(),
            documentInstanceSection.getDisplayOrder(),
            children
        )
    );
  }

  @Test
  void from_sectionHasCreatedFromDocumentTemplateSection() {
    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    var createdFromDocumentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();

    documentInstanceSection.setCreatedFromDocumentTemplateSection(createdFromDocumentTemplateSection);
    documentInstanceSection.setParent(null);

    var children = List.of(
        DocumentInstanceSectionDtoTestUtil.builder().build(),
        DocumentInstanceSectionDtoTestUtil.builder().build()
    );

    assertThat(DocumentInstanceSectionDto.from(documentInstanceSection, children)).isEqualTo(
        new DocumentInstanceSectionDto(
            documentInstanceSection.getId(),
            DocumentInstanceDto.from(documentInstanceSection.getDocumentInstance()),
            documentInstanceSection.getCreatedFromDocumentTemplateSection().getId(),
            null,
            documentInstanceSection.getTitle(),
            documentInstanceSection.getContent(),
            documentInstanceSection.getDisplayOrder(),
            children
        )
    );
  }

  @Test
  void from_sectionHasParent() {
    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    var parent = DocumentInstanceSectionTestUtil.builder().build();

    documentInstanceSection.setCreatedFromDocumentTemplateSection(null);
    documentInstanceSection.setParent(parent);

    var children = List.of(
        DocumentInstanceSectionDtoTestUtil.builder().build(),
        DocumentInstanceSectionDtoTestUtil.builder().build()
    );

    assertThat(DocumentInstanceSectionDto.from(documentInstanceSection, children)).isEqualTo(
        new DocumentInstanceSectionDto(
            documentInstanceSection.getId(),
            DocumentInstanceDto.from(documentInstanceSection.getDocumentInstance()),
            null,
            parent.getId(),
            documentInstanceSection.getTitle(),
            documentInstanceSection.getContent(),
            documentInstanceSection.getDisplayOrder(),
            children
        )
    );
  }
}
