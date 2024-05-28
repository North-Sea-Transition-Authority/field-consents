package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentStatus.ACTIVE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentStatus.EXPIRED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentStatus.ISSUED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentStatus.SUPERSEDED;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConsentStatusTest {

  private static final Clock clock = Clock.fixed(Instant.parse("2024-05-24T17:00:00Z"), ZoneId.systemDefault());

  @ParameterizedTest
  @MethodSource("getFromArguments")
  void from(LocalDate consentStartDate, LocalDate consentEndDate, boolean consentSuperseded, ConsentStatus expectedStatus) {
    assertThat(ConsentStatus.from(consentStartDate, consentEndDate, consentSuperseded, clock)).isEqualTo(expectedStatus);
  }

  private static Stream<Arguments> getFromArguments() {
    var today = LocalDate.now(clock);

    return Stream.of(
        arguments(today.plusDays(1), today.plusDays(2), false, ISSUED),
        arguments(today.plusDays(1), today.plusDays(1), false, ISSUED),
        arguments(today, today.plusDays(1), false, ACTIVE),
        arguments(today, today, false, ACTIVE),
        arguments(today.minusDays(1), today, false, ACTIVE),
        arguments(today.minusDays(1), today.plusDays(1), false, ACTIVE),
        arguments(today.minusDays(1), today.minusDays(1), false, EXPIRED),
        arguments(today.minusDays(2), today.minusDays(1), false, EXPIRED),

        arguments(today.plusDays(1), today.plusDays(2), true, SUPERSEDED),
        arguments(today.plusDays(1), today.plusDays(1), true, SUPERSEDED),
        arguments(today, today.plusDays(1), true, SUPERSEDED),
        arguments(today, today, true, SUPERSEDED),
        arguments(today.minusDays(1), today, true, SUPERSEDED),
        arguments(today.minusDays(1), today.plusDays(1), true, SUPERSEDED),
        arguments(today.minusDays(1), today.minusDays(1), true, SUPERSEDED),
        arguments(today.minusDays(2), today.minusDays(1), true, SUPERSEDED)
    );
  }

  @Test
  void from_consentStartDateIsBeforeConsentEndDate() {
    var consentStartDate = LocalDate.now(clock);
    var consentEndDate = consentStartDate.minusDays(1);

    assertThatThrownBy(() -> ConsentStatus.from(consentStartDate, consentEndDate, false, clock))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Consent start date 2024-05-24 is before consent end date 2024-05-23");
  }
}
