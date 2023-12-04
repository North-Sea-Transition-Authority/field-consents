package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentTemplateSectionDtoTest {

  @Test
  void from_sectionDoesNotHaveParent() {
    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();

    documentTemplateSection.setParent(null);

    var children = List.of(
        DocumentTemplateSectionDtoTestUtil.builder().build(),
        DocumentTemplateSectionDtoTestUtil.builder().build()
    );

    assertThat(DocumentTemplateSectionDto.from(documentTemplateSection, children)).isEqualTo(
        new DocumentTemplateSectionDto(
            documentTemplateSection.getId(),
            DocumentTemplateDto.from(documentTemplateSection.getDocumentTemplate()),
            null,
            documentTemplateSection.getTitle(),
            documentTemplateSection.getContent(),
            documentTemplateSection.getDisplayOrder(),
            children
        )
    );
  }

  @Test
  void from_sectionHasParent() {
    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();

    var parent = DocumentTemplateSectionTestUtil.builder().build();

    documentTemplateSection.setParent(parent);

    var children = List.of(
        DocumentTemplateSectionDtoTestUtil.builder().build(),
        DocumentTemplateSectionDtoTestUtil.builder().build()
    );

    assertThat(DocumentTemplateSectionDto.from(documentTemplateSection, children)).isEqualTo(
        new DocumentTemplateSectionDto(
            documentTemplateSection.getId(),
            DocumentTemplateDto.from(documentTemplateSection.getDocumentTemplate()),
            parent.getId(),
            documentTemplateSection.getTitle(),
            documentTemplateSection.getContent(),
            documentTemplateSection.getDisplayOrder(),
            children
        )
    );
  }
}
