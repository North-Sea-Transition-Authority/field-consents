package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.ReportUtil;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;

@Service
public class FlareReportService {

  private final FlareReportMonthRepository flareReportMonthRepository;

  private final FlareReportPeriodService flareReportPeriodService;

  private final ApplicationUnitService applicationUnitService;

  private final EmissionReportSummaryService emissionReportSummaryService;

  @Autowired
  FlareReportService(FlareReportMonthRepository flareReportMonthRepository,
                     FlareReportPeriodService flareReportPeriodService,
                     ApplicationUnitService applicationUnitService,
                     EmissionReportSummaryService emissionReportSummaryService) {
    this.flareReportMonthRepository = flareReportMonthRepository;
    this.flareReportPeriodService = flareReportPeriodService;
    this.applicationUnitService = applicationUnitService;
    this.emissionReportSummaryService = emissionReportSummaryService;
  }

  public List<FlareReportMonth> getFlareReportMonths(ApplicationVersion applicationVersion) {
    return flareReportMonthRepository.findAllByApplicationVersion(applicationVersion);
  }

  public boolean flareReportMonthsComplete(ApplicationVersion applicationVersion) {

    Optional<FlareReportPeriod> flareReportPeriodOptional =
        flareReportPeriodService.findFlareReportPeriod(applicationVersion);

    if (flareReportPeriodOptional.isEmpty()) {
      return false;
    }

    var flareReportPeriod = flareReportPeriodOptional.get();

    // if there is no difference between the expected year months and the existing year months of flare report data then
    // the flare report data is complete
    return CollectionUtils.disjunction(
        getExistingFlareReportYearMonths(applicationVersion),
        ReportUtil.getExpectedYearMonthsForPeriod(
            flareReportPeriod.getReportStartYearMonth(),
            flareReportPeriod.getReportEndYearMonth())
        ).isEmpty();
  }

  private List<YearMonth> getExistingFlareReportYearMonths(ApplicationVersion applicationVersion) {
    // return a list of years and months for any flare report data we have
    return getFlareReportMonths(applicationVersion)
        .stream()
        .map(flareReportMonth -> YearMonth.of(flareReportMonth.getYear(), flareReportMonth.getMonth()))
        .toList();
  }

  FlareReportForm getFlareReportForm(ApplicationVersion applicationVersion) {

    List<FlareReportMonth> previousFlareReportMonths = getFlareReportMonths(applicationVersion);

    List<FlareReportMonthForm> flareReportMonthForms = initialiseFlareReportMonthForms(applicationVersion);

    // merge DB data into month forms
    List<FlareReportMonthForm> mergedFlareReportMonthForms =
        mergeExistingMonthDetailsWithForms(flareReportMonthForms, previousFlareReportMonths);

    return new FlareReportForm(mergedFlareReportMonthForms);
  }

  private List<FlareReportMonthForm> initialiseFlareReportMonthForms(ApplicationVersion applicationVersion) {
    List<FlareReportMonthForm> flareReportMonthForms = new ArrayList<>();

    var flareReportPeriod = flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion);
    List<YearMonth> expectedYearMonths =
        ReportUtil.getExpectedYearMonthsForPeriod(
            flareReportPeriod.getReportStartYearMonth(),
            flareReportPeriod.getReportEndYearMonth());

    for (YearMonth yearMonth: expectedYearMonths) {
      flareReportMonthForms.add(FlareReportMonthForm.from(yearMonth));
    }

    return flareReportMonthForms;
  }

  private List<FlareReportMonthForm> mergeExistingMonthDetailsWithForms(
      List<FlareReportMonthForm> monthForms,
      List<FlareReportMonth> previousFlareReportMonths) {

    if (previousFlareReportMonths.isEmpty()) {
      return monthForms;
    }

    Map<YearMonth, FlareReportMonth> previousFlareReportMonthsMap = getFlareReportMonthsMap(previousFlareReportMonths);

    List<FlareReportMonthForm> mergedMonthForms = new ArrayList<>();
    for (FlareReportMonthForm flareReportMonthForm : monthForms) {
      var previousFlareReportMonth =
          previousFlareReportMonthsMap.get(flareReportMonthForm.getYearMonth());

      // if the DB data exists for the current form month then create a new month form from this
      FlareReportMonthForm mergedFlareReportMonthForm;
      if (previousFlareReportMonth != null) {
        mergedFlareReportMonthForm = FlareReportMonthForm.from(previousFlareReportMonth);
      } else {
        // copy forward the existing stub form
        mergedFlareReportMonthForm = flareReportMonthForm;
      }
      mergedMonthForms.add(mergedFlareReportMonthForm);
    }
    return mergedMonthForms;
  }

  private Map<YearMonth, FlareReportMonth> getFlareReportMonthsMap(List<FlareReportMonth> flareReportMonths) {
    return flareReportMonths.stream().collect(
        Collectors.toMap(flareReportMonth ->
            YearMonth.of(flareReportMonth.getYear(), flareReportMonth.getMonth()), Function.identity()));
  }

  @Transactional
  public void saveFlareReport(ApplicationVersion applicationVersion, FlareReportForm flareReportForm) {
    List<FlareReportMonthForm> flareReportMonthForms = flareReportForm.getFlareReportMonthForms();

    // delete old DB data for the app version
    flareReportMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    // save new form data to the DB for each month
    flareReportMonthForms.forEach(flareReportMonthForm ->
        flareReportMonthRepository.save(FlareReportMonth.from(applicationVersion, flareReportMonthForm)));
  }

  public List<SummaryCard> getFlareReportSummaryCards(ApplicationVersion applicationVersion) {

    var flareReportPeriodOptional = flareReportPeriodService.findFlareReportPeriod(applicationVersion);

    if (flareReportPeriodOptional.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    var summaryCards = new ArrayList<SummaryCard>();

    summaryCards.add(emissionReportSummaryService.getReportPeriodSummaryCard(
            flareReportPeriodOptional.get(),
            applicationVersion.getApplication().getType()));

    var flareReportMonths = getFlareReportMonths(applicationVersion);

    if (flareReportMonths.isEmpty()) {
      return summaryCards;
    }

    var categoryUnit = applicationUnitService.getFlareCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getFlareAverageUnit(applicationVersion);

    summaryCards.add(emissionReportSummaryService.getReportTableSummaryCard(flareReportMonths, categoryUnit, averageUnit));

    return summaryCards;
  }
}
