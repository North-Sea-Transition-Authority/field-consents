package uk.co.nstauthority.fieldconsents.charts;

import static uk.co.nstauthority.fieldconsents.application.ApplicationType.FLARE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationType.VENT;
import static uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType.CATEGORY_ABC;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartData.DataPoint;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartData.Series;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportService;

@Service
public class EmissionsChartDataService {

  private final ApplicationUnitService applicationUnitService;
  private final ConsentLengthService consentLengthService;

  // report services
  private final FlareReportService flareReportService;
  private final VentReportService ventReportService;

  // consent services
  private final FlareShortTermService flareShortTermService;
  private final FlareAnnualService flareAnnualService;
  private final VentShortTermService ventShortTermService;
  private final VentAnnualService ventAnnualService;

  EmissionsChartDataService(
      ApplicationUnitService applicationUnitService,
      ConsentLengthService consentLengthService,
      FlareReportService flareReportService,
      VentReportService ventReportService,
      FlareShortTermService flareShortTermService,
      FlareAnnualService flareAnnualService,
      VentShortTermService ventShortTermService,
      VentAnnualService ventAnnualService
  ) {
    this.applicationUnitService = applicationUnitService;
    this.consentLengthService = consentLengthService;
    this.flareReportService = flareReportService;
    this.ventReportService = ventReportService;
    this.flareShortTermService = flareShortTermService;
    this.flareAnnualService = flareAnnualService;
    this.ventShortTermService = ventShortTermService;
    this.ventAnnualService = ventAnnualService;
  }

  public Optional<EmissionsChartData> getReportChartData(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();
    if (applicationType != FLARE && applicationType != VENT) {
      return Optional.empty();
    }

    var emissionCategoryType = applicationUnitService.getEmissionCategoryType(applicationVersion);
    if (emissionCategoryType != CATEGORY_ABC) {
      return Optional.empty();
    }

    if (!reportDataExists(applicationVersion)) {
      return Optional.empty();
    }

    var emissionMonths = switch (applicationType) {
      case FLARE -> flareReportService.getFlareReportMonths(applicationVersion);
      case VENT -> ventReportService.getVentReportMonths(applicationVersion);
      default -> throw unsupportedApplicationTypeException(applicationType);
    };

    var emissionsChartData = getShortTermOrAnnualEmissionChartData(
        emissionMonths,
        applicationVersion,
        EmissionsChartType.REPORT
    );
    return Optional.of(emissionsChartData);
  }

  public Optional<EmissionsChartData> getConsentChartData(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();
    if (applicationType != FLARE && applicationType != VENT) {
      return Optional.empty();
    }

    var emissionCategoryType = applicationUnitService.getEmissionCategoryType(applicationVersion);
    if (emissionCategoryType != CATEGORY_ABC) {
      return Optional.empty();
    }

    var consentLength = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();
    if (consentLength != ConsentLengthType.SHORT_TERM && consentLength != ConsentLengthType.ANNUAL) {
      return Optional.empty();
    }

    if (!consentDataExists(applicationVersion, consentLength)) {
      return Optional.empty();
    }

    var emissionMonths = switch (applicationType) {
      case FLARE -> switch (consentLength) {
        case SHORT_TERM -> flareShortTermService.getFlareShortTermMonths(applicationVersion);
        case ANNUAL -> flareAnnualService.getFlareAnnualMonths(applicationVersion);
        default -> throw unsupportedConsentLength(consentLength);
      };
      case VENT -> switch (consentLength) {
        case SHORT_TERM -> ventShortTermService.getVentShortTermMonths(applicationVersion);
        case ANNUAL -> ventAnnualService.getVentAnnualMonths(applicationVersion);
        default -> throw unsupportedConsentLength(consentLength);
      };
      default -> throw unsupportedApplicationTypeException(applicationType);
    };

    var emissionsChartData = getShortTermOrAnnualEmissionChartData(
        emissionMonths,
        applicationVersion,
        EmissionsChartType.CONSENT
    );
    return Optional.of(emissionsChartData);
  }

  private EmissionsChartData getShortTermOrAnnualEmissionChartData(
      List<? extends FlareVentRow> flareVentRows,
      ApplicationVersion applicationVersion,
      EmissionsChartType chartType
  ) {
    var months = flareVentRows.stream()
        .map(FlareVentRow::getMonth)
        .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK))
        .toList();

    var emissionCategoryUnit = applicationUnitService.getEmissionCategoryUnit(applicationVersion);

    //CHECKSTYLE:OFF
    var yAxisTitle = "Volume (%s)".formatted(emissionCategoryUnit.getDisplayName());
    //CHECKSTYLE:ON

    var series = new ArrayList<Series>();

    var categoryA = flareVentRows.stream()
        .map(FlareVentRow::getCategoryA)
        .map(BigDecimal::floatValue)
        .map(catAValue -> new DataPoint(HighchartsColour.DARK_BLUE, catAValue))
        .toList();
    series.add(new Series("Category A", HighchartsColour.DARK_BLUE, categoryA));

    var categoryB = flareVentRows.stream()
        .map(FlareVentRow::getCategoryB)
        .map(BigDecimal::floatValue)
        .map(catBValue -> new DataPoint(HighchartsColour.BLUE, catBValue))
        .toList();
    series.add(new Series("Category B", HighchartsColour.BLUE, categoryB));

    var categoryC = flareVentRows.stream()
        .map(FlareVentRow::getCategoryC)
        .map(BigDecimal::floatValue)
        .map(catCValue -> new DataPoint(HighchartsColour.LIGHT_BLUE, catCValue))
        .toList();
    series.add(new Series("Category C", HighchartsColour.LIGHT_BLUE, categoryC));

    var chartHeading = chartType.getHeading(
        applicationVersion.getApplication().getType(),
        flareVentRows.getFirst().getYearMonth(),
        flareVentRows.getLast().getYearMonth()
    );

    return new EmissionsChartData(chartHeading, months, "Month", yAxisTitle, series);
  }

  private boolean reportDataExists(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();

    return switch (applicationType) {
      case FLARE -> flareReportService.flareReportMonthsComplete(applicationVersion);
      case VENT -> ventReportService.ventReportMonthsComplete(applicationVersion);
      default -> false;
    };
  }

  private boolean consentDataExists(ApplicationVersion applicationVersion, ConsentLengthType consentLength) {
    var applicationType = applicationVersion.getApplication().getType();

    return switch (applicationType) {
      case FLARE -> switch (consentLength) {
        case SHORT_TERM -> flareShortTermService.flareShortTermMonthsExist(applicationVersion);
        case ANNUAL -> flareAnnualService.flareAnnualMonthsExist(applicationVersion);
        default -> false;
      };
      case VENT -> switch (consentLength) {
        case SHORT_TERM -> ventShortTermService.ventShortTermMonthsExist(applicationVersion);
        case ANNUAL -> ventAnnualService.ventAnnualMonthsExist(applicationVersion);
        default -> false;
      };
      default -> false;
    };
  }

  private RuntimeException unsupportedApplicationTypeException(ApplicationType applicationType) {
    return new UnsupportedOperationException("Unsupported application type %s".formatted(applicationType));
  }

  private RuntimeException unsupportedConsentLength(ConsentLengthType consentLengthType) {
    return new UnsupportedOperationException("Unsupported consent length type %s".formatted(consentLengthType));
  }

}
