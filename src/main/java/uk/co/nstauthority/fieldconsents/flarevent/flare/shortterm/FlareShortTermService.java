package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ShortTermUtil;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionConsentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.util.ApplicationFigureComparators;

@Service
public class FlareShortTermService {

  private final FlareShortTermMonthRepository flareShortTermMonthRepository;

  private final ConsentLengthService consentLengthService;

  private final ApplicationUnitService applicationUnitService;

  private final EmissionConsentSummaryService emissionConsentSummaryService;

  @Autowired
  public FlareShortTermService(FlareShortTermMonthRepository flareShortTermMonthRepository,
                               ConsentLengthService consentLengthService,
                               ApplicationUnitService applicationUnitService,
                               EmissionConsentSummaryService emissionConsentSummaryService) {
    this.flareShortTermMonthRepository = flareShortTermMonthRepository;
    this.consentLengthService = consentLengthService;
    this.applicationUnitService = applicationUnitService;
    this.emissionConsentSummaryService = emissionConsentSummaryService;
  }

  public List<FlareShortTermMonth> getFlareShortTermMonths(ApplicationVersion applicationVersion) {
    return flareShortTermMonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(ApplicationFigureComparators.flareVentRow())
        .toList();
  }

  public boolean flareShortTermMonthsExist(ApplicationVersion applicationVersion) {
    return flareShortTermMonthRepository.existsByApplicationVersion(applicationVersion);
  }

  public boolean flareShortTermMonthsComplete(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

    // if there is no difference between the expected date pairs and the flare data date pairs then
    // the flare data is complete
    return CollectionUtils.disjunction(getExistingFlareMonthTerms(applicationVersion),
        ShortTermUtil.getExpectedMonthTerms(consentLengthDetails.getShortTermStartDate(),
            consentLengthDetails.getShortTermEndDate())).isEmpty();
  }

  private List<Pair<LocalDate, LocalDate>> getExistingFlareMonthTerms(ApplicationVersion applicationVersion) {
    // return a list of date pairs (start and end) for any months of flare data we have
    return getFlareShortTermMonths(applicationVersion)
        .stream()
        .map(flareShortTermMonth -> Pair.of(flareShortTermMonth.getStartDate(), flareShortTermMonth.getEndDate()))
        .toList();
  }

  FlareShortTermForm getFlareShortTermForm(ApplicationVersion applicationVersion) {

    List<FlareShortTermMonth> previousFlareShortTermMonths = getFlareShortTermMonths(applicationVersion);

    List<FlareShortTermMonthForm> flareShortTermMonthForms = initialiseFlareShortTermMonthForms(applicationVersion);

    // merge DB data into month forms
    List<FlareShortTermMonthForm> mergedFlareShortTermMonthForms =
        mergeExistingMonthDetailsWithForms(flareShortTermMonthForms, previousFlareShortTermMonths);

    return new FlareShortTermForm(mergedFlareShortTermMonthForms);
  }

  private List<FlareShortTermMonthForm> initialiseFlareShortTermMonthForms(ApplicationVersion applicationVersion) {
    List<FlareShortTermMonthForm> flareShortTermMonthForms = new ArrayList<>();

    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);
    LocalDate startTermDate = consentLengthDetails.getShortTermStartDate();
    LocalDate endTermDate = consentLengthDetails.getShortTermEndDate();

    for (Pair<LocalDate, LocalDate> flareMonthTerm: ShortTermUtil.getExpectedMonthTerms(startTermDate, endTermDate)) {
      flareShortTermMonthForms.add(FlareShortTermMonthForm.from(flareMonthTerm.getLeft(), flareMonthTerm.getRight()));
    }

    return flareShortTermMonthForms;
  }

  private List<FlareShortTermMonthForm> mergeExistingMonthDetailsWithForms(
      List<FlareShortTermMonthForm> monthForms,
      List<FlareShortTermMonth> previousFlareShortTermMonths) {

    if (previousFlareShortTermMonths.isEmpty()) {
      return monthForms;
    }

    Map<Pair<LocalDate, LocalDate>, FlareShortTermMonth> previousFlareShortTermMonthsMap =
        previousFlareShortTermMonths
            .stream()
            .collect(Collectors.toMap(flareShortTermMonth ->
                Pair.of(flareShortTermMonth.getStartDate(), flareShortTermMonth.getEndDate()), Function.identity()));

    List<FlareShortTermMonthForm> mergedMonthForms = new ArrayList<>();
    for (FlareShortTermMonthForm flareShortTermMonthForm : monthForms) {
      var previousFlareShortTermMonth =
          previousFlareShortTermMonthsMap.get(flareShortTermMonthForm.getMonthTerm());

      // if the DB data exists for the current form month term then create a new month form from this
      FlareShortTermMonthForm mergedFlareShortTermMonthForm;
      if (previousFlareShortTermMonth != null) {
        mergedFlareShortTermMonthForm = FlareShortTermMonthForm.from(previousFlareShortTermMonth);
      } else {
        // copy forward the existing stub form
        mergedFlareShortTermMonthForm = flareShortTermMonthForm;
      }
      mergedMonthForms.add(mergedFlareShortTermMonthForm);
    }
    return mergedMonthForms;
  }

  @Transactional
  public void saveFlareShortTerm(ApplicationVersion applicationVersion, FlareShortTermForm flareShortTermForm) {
    List<FlareShortTermMonthForm> flareShortTermMonthForms = flareShortTermForm.getFlareShortTermMonthForms();

    // delete old DB data for the app version
    flareShortTermMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    // save new form data to the DB for each month
    flareShortTermMonthForms.forEach(flareShortTermMonthForm ->
        flareShortTermMonthRepository.save(FlareShortTermMonth.from(applicationVersion, flareShortTermMonthForm)));
  }

  public SummaryCard getFlareShortTermSummaryCard(ApplicationVersion applicationVersion) {

    var flareShortTermMonths = getFlareShortTermMonths(applicationVersion);

    if (flareShortTermMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getFlareCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getFlareAverageUnit(applicationVersion);

    return emissionConsentSummaryService.getShortTermConsentSummaryCard(flareShortTermMonths, categoryUnit, averageUnit);
  }
}
