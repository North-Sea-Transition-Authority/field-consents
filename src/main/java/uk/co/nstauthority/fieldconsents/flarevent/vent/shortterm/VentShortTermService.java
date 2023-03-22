package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
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

@Service
public class VentShortTermService {

  private final VentShortTermMonthRepository ventShortTermMonthRepository;

  private final ConsentLengthService consentLengthService;

  private final ApplicationUnitService applicationUnitService;

  private final EmissionConsentSummaryService emissionConsentSummaryService;

  @Autowired
  public VentShortTermService(VentShortTermMonthRepository ventShortTermMonthRepository,
                              ConsentLengthService consentLengthService,
                              ApplicationUnitService applicationUnitService,
                              EmissionConsentSummaryService emissionConsentSummaryService) {
    this.ventShortTermMonthRepository = ventShortTermMonthRepository;
    this.consentLengthService = consentLengthService;
    this.applicationUnitService = applicationUnitService;
    this.emissionConsentSummaryService = emissionConsentSummaryService;
  }

  public List<VentShortTermMonth> getVentShortTermMonths(ApplicationVersion applicationVersion) {
    return ventShortTermMonthRepository.findAllByApplicationVersion(applicationVersion);
  }

  public boolean ventShortTermMonthsExist(ApplicationVersion applicationVersion) {
    return ventShortTermMonthRepository.existsByApplicationVersion(applicationVersion);
  }

  public boolean ventShortTermMonthsComplete(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

    // if there is no difference between the expected date pairs and the vent data date pairs then
    // the vent data is complete
    return CollectionUtils.disjunction(getExistingVentMonthTerms(applicationVersion),
        ShortTermUtil.getExpectedMonthTerms(consentLengthDetails.getShortTermStartDate(),
            consentLengthDetails.getShortTermEndDate())).isEmpty();
  }

  private List<Pair<LocalDate, LocalDate>> getExistingVentMonthTerms(ApplicationVersion applicationVersion) {
    // return a list of date pairs (start and end) for any months of vent data we have
    return getVentShortTermMonths(applicationVersion)
        .stream()
        .map(ventShortTermMonth -> Pair.of(ventShortTermMonth.getStartDate(), ventShortTermMonth.getEndDate()))
        .toList();
  }

  VentShortTermForm getVentShortTermForm(ApplicationVersion applicationVersion) {

    List<VentShortTermMonth> previousVentShortTermMonths = getVentShortTermMonths(applicationVersion);

    List<VentShortTermMonthForm> ventShortTermMonthForms = initialiseVentShortTermMonthForms(applicationVersion);

    // merge DB data into month forms
    List<VentShortTermMonthForm> mergedVentShortTermMonthForms =
        mergeExistingMonthDetailsWithForms(ventShortTermMonthForms, previousVentShortTermMonths);

    return new VentShortTermForm(mergedVentShortTermMonthForms);
  }

  private List<VentShortTermMonthForm> initialiseVentShortTermMonthForms(ApplicationVersion applicationVersion) {
    List<VentShortTermMonthForm> ventShortTermMonthForms = new ArrayList<>();

    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);
    LocalDate startTermDate = consentLengthDetails.getShortTermStartDate();
    LocalDate endTermDate = consentLengthDetails.getShortTermEndDate();

    for (Pair<LocalDate, LocalDate> ventMonthTerm: ShortTermUtil.getExpectedMonthTerms(startTermDate, endTermDate)) {
      ventShortTermMonthForms.add(VentShortTermMonthForm.from(ventMonthTerm.getLeft(), ventMonthTerm.getRight()));
    }

    return ventShortTermMonthForms;
  }

  private List<VentShortTermMonthForm> mergeExistingMonthDetailsWithForms(
      List<VentShortTermMonthForm> monthForms,
      List<VentShortTermMonth> previousVentShortTermMonths) {

    if (previousVentShortTermMonths.isEmpty()) {
      return monthForms;
    }

    Map<Pair<LocalDate, LocalDate>, VentShortTermMonth> previousVentShortTermMonthsMap =
        previousVentShortTermMonths
            .stream()
            .collect(Collectors.toMap(ventShortTermMonth ->
                Pair.of(ventShortTermMonth.getStartDate(), ventShortTermMonth.getEndDate()), Function.identity()));

    List<VentShortTermMonthForm> mergedMonthForms = new ArrayList<>();
    for (VentShortTermMonthForm ventShortTermMonthForm : monthForms) {
      var previousVentShortTermMonth =
          previousVentShortTermMonthsMap.get(ventShortTermMonthForm.getMonthTerm());

      // if the DB data exists for the current form month term then create a new month form from this
      VentShortTermMonthForm mergedVentShortTermMonthForm;
      if (previousVentShortTermMonth != null) {
        mergedVentShortTermMonthForm = VentShortTermMonthForm.from(previousVentShortTermMonth);
      } else {
        // copy forward the existing stub form
        mergedVentShortTermMonthForm = ventShortTermMonthForm;
      }
      mergedMonthForms.add(mergedVentShortTermMonthForm);
    }
    return mergedMonthForms;
  }

  @Transactional
  public void saveVentShortTerm(ApplicationVersion applicationVersion, VentShortTermForm ventShortTermForm) {
    List<VentShortTermMonthForm> ventShortTermMonthForms = ventShortTermForm.getVentShortTermMonthForms();

    // delete old DB data for the app version
    ventShortTermMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    // save new form data to the DB for each month
    ventShortTermMonthForms.forEach(ventShortTermMonthForm ->
        ventShortTermMonthRepository.save(VentShortTermMonth.from(applicationVersion, ventShortTermMonthForm)));
  }

  public SummaryCard getVentShortTermSummaryCard(ApplicationVersion applicationVersion) {

    var ventShortTermMonths = getVentShortTermMonths(applicationVersion);

    if (ventShortTermMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getVentCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getVentAverageUnit(applicationVersion);

    return emissionConsentSummaryService.getShortTermConsentSummaryCard(ventShortTermMonths, categoryUnit, averageUnit);
  }
}
