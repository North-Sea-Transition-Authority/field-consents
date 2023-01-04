package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.ReportUtil;

@Service
public class VentReportService {

  private final VentReportMonthRepository ventReportMonthRepository;

  private final VentReportPeriodService ventReportPeriodService;

  @Autowired
  VentReportService(VentReportMonthRepository ventReportMonthRepository,
                    VentReportPeriodService ventReportPeriodService) {
    this.ventReportMonthRepository = ventReportMonthRepository;
    this.ventReportPeriodService = ventReportPeriodService;
  }

  public boolean ventReportMonthsComplete(ApplicationVersion applicationVersion) {

    Optional<VentReportPeriod> ventReportPeriodOptional =
        ventReportPeriodService.findVentReportPeriod(applicationVersion);

    if (ventReportPeriodOptional.isEmpty()) {
      return false;
    }

    var ventReportPeriod = ventReportPeriodOptional.get();

    // if there is no difference between the expected year months and the existing year months of vent report data then
    // the vent report data is complete
    return CollectionUtils.disjunction(
        getExistingVentReportYearMonths(applicationVersion),
        ReportUtil.getExpectedYearMonthsForPeriod(
            ventReportPeriod.getReportStartYearMonth(),
            ventReportPeriod.getReportEndYearMonth())
        ).isEmpty();
  }

  private List<YearMonth> getExistingVentReportYearMonths(ApplicationVersion applicationVersion) {
    // return a list of years and months for any vent report data we have
    return ventReportMonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .map(ventReportMonth -> YearMonth.of(ventReportMonth.getYear(), ventReportMonth.getMonth()))
        .toList();
  }

  VentReportForm getVentReportForm(ApplicationVersion applicationVersion) {

    List<VentReportMonth> previousVentReportMonths = ventReportMonthRepository
        .findAllByApplicationVersion(applicationVersion);

    List<VentReportMonthForm> ventReportMonthForms = initialiseVentReportMonthForms(applicationVersion);

    // merge DB data into month forms
    List<VentReportMonthForm> mergedVentReportMonthForms =
        mergeExistingMonthDetailsWithForms(ventReportMonthForms, previousVentReportMonths);

    return new VentReportForm(mergedVentReportMonthForms);
  }

  private List<VentReportMonthForm> initialiseVentReportMonthForms(ApplicationVersion applicationVersion) {
    List<VentReportMonthForm> ventReportMonthForms = new ArrayList<>();

    var ventReportPeriod = ventReportPeriodService.getVentReportPeriodOrError(applicationVersion);
    List<YearMonth> expectedYearMonths =
        ReportUtil.getExpectedYearMonthsForPeriod(
            ventReportPeriod.getReportStartYearMonth(),
            ventReportPeriod.getReportEndYearMonth());

    for (YearMonth yearMonth: expectedYearMonths) {
      ventReportMonthForms.add(VentReportMonthForm.from(yearMonth));
    }

    return ventReportMonthForms;
  }

  private List<VentReportMonthForm> mergeExistingMonthDetailsWithForms(
      List<VentReportMonthForm> monthForms,
      List<VentReportMonth> previousVentReportMonths) {

    if (previousVentReportMonths.isEmpty()) {
      return monthForms;
    }

    Map<YearMonth, VentReportMonth> previousVentReportMonthsMap = getVentReportMonthsMap(previousVentReportMonths);

    List<VentReportMonthForm> mergedMonthForms = new ArrayList<>();
    for (VentReportMonthForm ventReportMonthForm : monthForms) {
      var previousVentReportMonth =
          previousVentReportMonthsMap.get(ventReportMonthForm.getYearMonth());

      // if the DB data exists for the current form month then create a new month form from this
      VentReportMonthForm mergedVentReportMonthForm;
      if (previousVentReportMonth != null) {
        mergedVentReportMonthForm = VentReportMonthForm.from(previousVentReportMonth);
      } else {
        // copy forward the existing stub form
        mergedVentReportMonthForm = ventReportMonthForm;
      }
      mergedMonthForms.add(mergedVentReportMonthForm);
    }
    return mergedMonthForms;
  }

  private Map<YearMonth, VentReportMonth> getVentReportMonthsMap(List<VentReportMonth> ventReportMonths) {
    return ventReportMonths.stream().collect(
        Collectors.toMap(ventReportMonth ->
            YearMonth.of(ventReportMonth.getYear(), ventReportMonth.getMonth()), Function.identity()));
  }

  @Transactional
  public void saveVentReport(ApplicationVersion applicationVersion, VentReportForm ventReportForm) {
    List<VentReportMonthForm> ventReportMonthForms = ventReportForm.getVentReportMonthForms();

    // delete old DB data for the app version
    ventReportMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    // save new form data to the DB for each month
    ventReportMonthForms.forEach(ventReportMonthForm ->
        ventReportMonthRepository.save(VentReportMonth.from(applicationVersion, ventReportMonthForm)));
  }

}
