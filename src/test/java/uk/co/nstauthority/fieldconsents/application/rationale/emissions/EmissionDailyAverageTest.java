package uk.co.nstauthority.fieldconsents.application.rationale.emissions;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermEmissionFigures;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;

class EmissionDailyAverageTest {

  private ConsentFigureUnitView consentFigureUnitView;
  private final int currentYear = Year.now().getValue();

  @BeforeEach
  void setUp() {
    consentFigureUnitView = new ConsentFigureUnitView(
        null,
        null,
        FlareVentUnit.TONNES_PER_DAY
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void from_forShortTermOrAnnual(ApplicationType applicationType) {
    var application = new Application(1);
    application.setType(applicationType);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withApplication(application)
        .withEmissionDailyAverage(BigDecimal.ONE)
        .build();

    assertThat(EmissionDailyAverage.from(currentYear, consentData, consentFigureUnitView))
        .isEqualTo(
            new EmissionDailyAverage(
                consentData.getApplication().getType(),
                currentYear,
                consentData.getEmissionDailyAverage(),
                consentFigureUnitView.emissionUnit()
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void from_forLongTerm(ApplicationType applicationType) {
    var application = new Application(1);
    application.setType(applicationType);

    var consentDataLongTermEmissionFiguresCurrent = new ConsentDataLongTermEmissionFigures();
    consentDataLongTermEmissionFiguresCurrent.setApplication(application);
    consentDataLongTermEmissionFiguresCurrent.setYear(currentYear);
    consentDataLongTermEmissionFiguresCurrent.setDailyAverage(BigDecimal.ONE);

    assertThat(EmissionDailyAverage.from(consentDataLongTermEmissionFiguresCurrent, consentFigureUnitView))
        .isEqualTo(
            new EmissionDailyAverage(
                consentDataLongTermEmissionFiguresCurrent.getApplication().getType(),
                consentDataLongTermEmissionFiguresCurrent.getYear(),
                consentDataLongTermEmissionFiguresCurrent.getDailyAverage(),
                consentFigureUnitView.emissionUnit()
            )
        );
  }
}
