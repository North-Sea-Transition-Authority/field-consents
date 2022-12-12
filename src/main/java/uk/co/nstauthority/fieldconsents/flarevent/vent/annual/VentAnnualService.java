package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

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

@Service
public class VentAnnualService {

  private final VentAnnualMonthRepository ventAnnualMonthRepository;

  private final ConsentLengthService consentLengthService;

  @Autowired
  public VentAnnualService(VentAnnualMonthRepository ventAnnualMonthRepository,
                           ConsentLengthService consentLengthService) {
    this.ventAnnualMonthRepository = ventAnnualMonthRepository;
    this.consentLengthService = consentLengthService;
  }

  public List<VentAnnualMonth> getVentAnnualMonths(ApplicationVersion applicationVersion) {
    return ventAnnualMonthRepository.findAllByApplicationVersion(applicationVersion);
  }

  public boolean ventAnnualMonthsExist(ApplicationVersion applicationVersion) {
    return ventAnnualMonthRepository.existsByApplicationVersion(applicationVersion);
  }

  public boolean ventAnnualMonthsComplete(ApplicationVersion applicationVersion) {
    Integer annualConsentYear = consentLengthService.getConsentLengthDetails(applicationVersion).getAnnualConsentYear();

    // if there is no difference between the expected year months and the existing year months of vent data then
    // the vent data is complete
    return CollectionUtils.disjunction(getExistingVentYearMonths(applicationVersion),
        AnnualUtil.getExpectedYearMonths(annualConsentYear)).isEmpty();
  }

  private List<YearMonth> getExistingVentYearMonths(ApplicationVersion applicationVersion) {
    // return a list of years and months for any vent data we have
    return getVentAnnualMonths(applicationVersion)
        .stream()
        .map(ventAnnualMonth -> YearMonth.of(ventAnnualMonth.getYear(), ventAnnualMonth.getMonth()))
        .toList();
  }

  VentAnnualForm getVentAnnualForm(ApplicationVersion applicationVersion) {

    List<VentAnnualMonth> previousVentAnnualMonths = getVentAnnualMonths(applicationVersion);

    List<VentAnnualMonthForm> ventAnnualMonthForms = initialiseVentAnnualMonthForms(applicationVersion);

    // merge DB data into month forms
    List<VentAnnualMonthForm> mergedVentAnnualMonthForms =
        mergeExistingMonthDetailsWithForms(ventAnnualMonthForms, previousVentAnnualMonths);

    return new VentAnnualForm(mergedVentAnnualMonthForms);
  }

  private List<VentAnnualMonthForm> initialiseVentAnnualMonthForms(ApplicationVersion applicationVersion) {
    List<VentAnnualMonthForm> ventAnnualMonthForms = new ArrayList<>();

    var consentYear = consentLengthService.getConsentLengthDetails(applicationVersion).getAnnualConsentYear();

    for (Month month: Month.values()) {
      ventAnnualMonthForms.add(VentAnnualMonthForm.from(YearMonth.of(consentYear, month)));
    }

    return ventAnnualMonthForms;
  }

  private List<VentAnnualMonthForm> mergeExistingMonthDetailsWithForms(
      List<VentAnnualMonthForm> monthForms,
      List<VentAnnualMonth> previousVentAnnualMonths) {

    if (previousVentAnnualMonths.isEmpty()) {
      return monthForms;
    }

    Map<YearMonth, VentAnnualMonth> previousVentAnnualMonthsMap =
        previousVentAnnualMonths.stream().collect(
            Collectors.toMap(ventAnnualMonth ->
                YearMonth.of(ventAnnualMonth.getYear(), ventAnnualMonth.getMonth()), Function.identity()));

    List<VentAnnualMonthForm> mergedMonthForms = new ArrayList<>();
    for (VentAnnualMonthForm ventAnnualMonthForm : monthForms) {
      var previousVentAnnualMonth =
          previousVentAnnualMonthsMap.get(ventAnnualMonthForm.getYearMonth());

      // if the DB data exists for the current form month then create a new month form from this
      VentAnnualMonthForm mergedVentAnnualMonthForm;
      if (previousVentAnnualMonth != null) {
        mergedVentAnnualMonthForm = VentAnnualMonthForm.from(previousVentAnnualMonth);
      } else {
        // copy forward the existing stub form
        mergedVentAnnualMonthForm = ventAnnualMonthForm;
      }
      mergedMonthForms.add(mergedVentAnnualMonthForm);
    }
    return mergedMonthForms;
  }

  @Transactional
  public void saveVentAnnual(ApplicationVersion applicationVersion, VentAnnualForm ventAnnualForm) {
    List<VentAnnualMonthForm> ventAnnualMonthForms = ventAnnualForm.getVentAnnualMonthForms();

    // delete old DB data for the app version
    ventAnnualMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    // save new form data to the DB for each month
    ventAnnualMonthForms.forEach(ventAnnualMonthForm ->
        ventAnnualMonthRepository.save(VentAnnualMonth.from(applicationVersion, ventAnnualMonthForm)));
  }

}
