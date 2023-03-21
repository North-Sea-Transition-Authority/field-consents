package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.AnnualUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionConsentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;

@Service
public class FlareAnnualService {

  private final FlareAnnualMonthRepository flareAnnualMonthRepository;

  private final ConsentLengthService consentLengthService;

  private final ApplicationUnitService applicationUnitService;

  private final EmissionConsentSummaryService emissionConsentSummaryService;

  @Autowired
  public FlareAnnualService(FlareAnnualMonthRepository flareAnnualMonthRepository,
                            ConsentLengthService consentLengthService,
                            ApplicationUnitService applicationUnitService,
                            EmissionConsentSummaryService emissionConsentSummaryService) {
    this.flareAnnualMonthRepository = flareAnnualMonthRepository;
    this.consentLengthService = consentLengthService;
    this.applicationUnitService = applicationUnitService;
    this.emissionConsentSummaryService = emissionConsentSummaryService;
  }

  public List<FlareAnnualMonth> getFlareAnnualMonths(ApplicationVersion applicationVersion) {
    return flareAnnualMonthRepository.findAllByApplicationVersion(applicationVersion);
  }

  public boolean flareAnnualMonthsExist(ApplicationVersion applicationVersion) {
    return flareAnnualMonthRepository.existsByApplicationVersion(applicationVersion);
  }

  public boolean flareAnnualMonthsComplete(ApplicationVersion applicationVersion) {
    Integer annualConsentYear = consentLengthService.getConsentLengthDetails(applicationVersion).getAnnualConsentYear();

    // if there is no difference between the expected year months and the existing year months of flare data then
    // the flare data is complete
    return CollectionUtils.disjunction(getExistingFlareYearMonths(applicationVersion),
        AnnualUtil.getExpectedYearMonths(annualConsentYear)).isEmpty();
  }

  private List<YearMonth> getExistingFlareYearMonths(ApplicationVersion applicationVersion) {
    // return a list of years and months for any flare data we have
    return getFlareAnnualMonths(applicationVersion)
        .stream()
        .map(flareAnnualMonth -> YearMonth.of(flareAnnualMonth.getYear(), flareAnnualMonth.getMonth()))
        .toList();
  }

  FlareAnnualForm getFlareAnnualForm(ApplicationVersion applicationVersion) {

    List<FlareAnnualMonth> previousFlareAnnualMonths = getFlareAnnualMonths(applicationVersion);

    List<FlareAnnualMonthForm> flareAnnualMonthForms = initialiseFlareAnnualMonthForms(applicationVersion);

    // merge DB data into month forms
    List<FlareAnnualMonthForm> mergedFlareAnnualMonthForms =
        mergeExistingMonthDetailsWithForms(flareAnnualMonthForms, previousFlareAnnualMonths);

    return new FlareAnnualForm(mergedFlareAnnualMonthForms);
  }

  private List<FlareAnnualMonthForm> initialiseFlareAnnualMonthForms(ApplicationVersion applicationVersion) {
    List<FlareAnnualMonthForm> flareAnnualMonthForms = new ArrayList<>();

    var consentYear = consentLengthService.getConsentLengthDetails(applicationVersion).getAnnualConsentYear();

    for (Month month: Month.values()) {
      flareAnnualMonthForms.add(FlareAnnualMonthForm.from(YearMonth.of(consentYear, month)));
    }

    return flareAnnualMonthForms;
  }

  private List<FlareAnnualMonthForm> mergeExistingMonthDetailsWithForms(
      List<FlareAnnualMonthForm> monthForms,
      List<FlareAnnualMonth> previousFlareAnnualMonths) {

    if (previousFlareAnnualMonths.isEmpty()) {
      return monthForms;
    }

    Map<YearMonth, FlareAnnualMonth> previousFlareAnnualMonthsMap =
        previousFlareAnnualMonths.stream().collect(
            Collectors.toMap(flareAnnualMonth ->
                YearMonth.of(flareAnnualMonth.getYear(), flareAnnualMonth.getMonth()), Function.identity()));

    List<FlareAnnualMonthForm> mergedMonthForms = new ArrayList<>();
    for (FlareAnnualMonthForm flareAnnualMonthForm : monthForms) {
      var previousFlareAnnualMonth =
          previousFlareAnnualMonthsMap.get(flareAnnualMonthForm.getYearMonth());

      // if the DB data exists for the current form month then create a new month form from this
      FlareAnnualMonthForm mergedFlareAnnualMonthForm;
      if (previousFlareAnnualMonth != null) {
        mergedFlareAnnualMonthForm = FlareAnnualMonthForm.from(previousFlareAnnualMonth);
      } else {
        // copy forward the existing stub form
        mergedFlareAnnualMonthForm = flareAnnualMonthForm;
      }
      mergedMonthForms.add(mergedFlareAnnualMonthForm);
    }
    return mergedMonthForms;
  }

  @Transactional
  public void saveFlareAnnual(ApplicationVersion applicationVersion, FlareAnnualForm flareAnnualForm) {
    List<FlareAnnualMonthForm> flareAnnualMonthForms = flareAnnualForm.getFlareAnnualMonthForms();

    // delete old DB data for the app version
    flareAnnualMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    // save new form data to the DB for each month
    flareAnnualMonthForms.forEach(flareAnnualMonthForm ->
        flareAnnualMonthRepository.save(FlareAnnualMonth.from(applicationVersion, flareAnnualMonthForm)));
  }

  public SummaryCard getFlareAnnualSummaryCard(ApplicationVersion applicationVersion) {

    var flareAnnualMonths = getFlareAnnualMonths(applicationVersion);

    if (flareAnnualMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getFlareCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getFlareAverageUnit(applicationVersion);

    return emissionConsentSummaryService.getAnnualConsentSummaryCard(flareAnnualMonths, categoryUnit, averageUnit);
  }
}
