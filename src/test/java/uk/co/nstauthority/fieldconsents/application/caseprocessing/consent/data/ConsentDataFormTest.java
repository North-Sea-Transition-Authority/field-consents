package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ConsentDataFormTest {

  @Test
  void from() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    assertThat(ConsentDataForm.from(startDate, endDate))
        .extracting(
            form -> form.consentStartDate().getAsLocalDate().orElseThrow(),
            form -> form.consentEndDate().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            startDate,
            endDate
        );
  }
}
