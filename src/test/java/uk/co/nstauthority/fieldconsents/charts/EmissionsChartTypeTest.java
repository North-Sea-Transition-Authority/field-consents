package uk.co.nstauthority.fieldconsents.charts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.YearMonth;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

class EmissionsChartTypeTest {

  private static final YearMonth START = YearMonth.of(2020, 1);
  private static final YearMonth END = YearMonth.of(2024, 12);

  @ParameterizedTest
  @MethodSource("getHeading_arguments")
  void getHeading(ApplicationType applicationType, EmissionsChartType emissionsChartType, String expectedHeading) {
    assertThat(emissionsChartType.getHeading(applicationType, START, END)).isEqualTo(expectedHeading);
  }

  private static Stream<Arguments> getHeading_arguments() {
    return Stream.of(
        arguments(ApplicationType.FLARE, EmissionsChartType.REPORT, "Flare report (Jan 2020 - Dec 2024)"),
        arguments(ApplicationType.FLARE, EmissionsChartType.CONSENT, "Flare consent (Jan 2020 - Dec 2024)"),
        arguments(ApplicationType.VENT, EmissionsChartType.REPORT, "Vent report (Jan 2020 - Dec 2024)"),
        arguments(ApplicationType.VENT, EmissionsChartType.CONSENT, "Vent consent (Jan 2020 - Dec 2024)")
    );
  }

}