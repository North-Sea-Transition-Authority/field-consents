package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentMailMergeValidationResultTest {

  @Test
  void valid() {
    assertThat(DocumentMailMergeValidationResult.valid())
        .isEqualTo(new DocumentMailMergeValidationResult(true, null));
  }

  @Test
  void invalid() {
    var errorMessage = "Test error message";

    assertThat(DocumentMailMergeValidationResult.invalid(errorMessage))
        .isEqualTo(new DocumentMailMergeValidationResult(false, errorMessage));
  }
}
