package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class ConsentLengthViewTest {

  @Test
  void from_shortTermStartDateIsNotNull() {
    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(LocalDate.of(2024, 8, 12));
    consentLengthDetails.setAnnualConsentYear(2024);
    consentLengthDetails.setLongTermStartYear(2025);

    assertThat(ConsentLengthView.from(consentLengthDetails)).isEqualTo(
        new ConsentLengthView(
            ConsentLengthType.SHORT_TERM,
            "12 Aug 2024",
            2024,
            2025
        )
    );
  }

  @Test
  void from_shortTermStartDateIsNull() {
    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setAnnualConsentYear(2024);
    consentLengthDetails.setLongTermStartYear(2025);

    assertThat(ConsentLengthView.from(consentLengthDetails)).isEqualTo(
        new ConsentLengthView(
            ConsentLengthType.SHORT_TERM,
            "",
            2024,
            2025
        )
    );
  }
}
