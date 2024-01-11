package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentMailMergeFieldViewTest {

  @Test
  void from() {
    var documentMailMergeField = DocumentMailMergeFieldTestUtil.builder().build();

    assertThat(DocumentMailMergeFieldView.from(documentMailMergeField)).isEqualTo(
        new DocumentMailMergeFieldView(documentMailMergeField.getMnemonic(), documentMailMergeField.getDescription())
    );
  }
}
