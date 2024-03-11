package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentMailMergeFieldFormatterTest {

  @InjectMocks
  private FieldConsentsDocumentMailMergeFieldFormatter formatter;

  @Test
  void formatSuccess() {
    assertThat(formatter.formatSuccess("value"))
        .isEqualTo(
            """
                <span class="govuk-tag--green">value</span>\
                """
        );
  }

  @Test
  void formatError() {
    assertThat(formatter.formatError("value"))
        .isEqualTo(
            """
                <span class="govuk-tag--red">value</span>\
                """
        );
  }
}
