package uk.co.nstauthority.fieldconsents.production.shortterm;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Service
public class ShortTermProductionService {

  private final ProductionRowService productionRowService;

  private final ConsentLengthService consentLengthService;

  private final ShortTermProductionMonthRepository shortTermProductionMonthRepository;

  @Autowired
  public ShortTermProductionService(ProductionRowService productionRowService,
                                    ConsentLengthService consentLengthService,
                                    ShortTermProductionMonthRepository shortTermProductionMonthRepository) {
    this.productionRowService = productionRowService;
    this.consentLengthService = consentLengthService;
    this.shortTermProductionMonthRepository = shortTermProductionMonthRepository;
  }

  private List<ShortTermProductionMonth> getShortTermProductionMonths(ApplicationVersion applicationVersion) {
    return shortTermProductionMonthRepository.findAllByApplicationVersion(applicationVersion);
  }

  public boolean shortTermProductionMonthsExist(ApplicationVersion applicationVersion) {
    return shortTermProductionMonthRepository.existsByApplicationVersion(applicationVersion);
  }

  public boolean shortTermProductionMonthsComplete(ApplicationVersion applicationVersion) {
    // if there is no difference between the expected date pairs and the production data date pairs then
    // the production data is complete
    return CollectionUtils.disjunction(getExistingProductionMonthTerms(applicationVersion),
        getExpectedProductionMonthTerms(applicationVersion)).isEmpty();
  }

  private List<Pair<LocalDate, LocalDate>> getExistingProductionMonthTerms(ApplicationVersion applicationVersion) {
    // return a list of date pairs (start and end) for any months of production data we have
    return getShortTermProductionMonths(applicationVersion).stream().map(productionMonth ->
            Pair.of(productionMonth.getStartDate(), productionMonth.getEndDate())).toList();
  }

  private List<Pair<LocalDate, LocalDate>> getExpectedProductionMonthTerms(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);
    LocalDate startTermDate = consentLengthDetails.getShortTermStartDate();
    LocalDate endTermDate = consentLengthDetails.getShortTermEndDate();
    YearMonth startYearMonth = YearMonth.of(startTermDate.getYear(), startTermDate.getMonth());
    YearMonth endYearMonth = YearMonth.of(endTermDate.getYear(), endTermDate.getMonth());

    // create a list of expected date pairs based on the consent length details information
    List<Pair<LocalDate, LocalDate>> expectedProductionMonthTerms = new ArrayList<>();
    for (YearMonth yearMonth = startYearMonth;
         yearMonth.isBefore(endYearMonth.plusMonths(1));
         yearMonth = yearMonth.plusMonths(1)) {
      expectedProductionMonthTerms.add(Pair.of(DateUtils.max(startTermDate, yearMonth.atDay(1)),
          DateUtils.min(endTermDate, yearMonth.atEndOfMonth())));
    }
    return expectedProductionMonthTerms;
  }

  public ShortTermProductionForm getShortTermProductionForm(ApplicationVersion currentVersion) {
    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(currentVersion);

    LocalDate startTermDate = consentLengthDetails.getShortTermStartDate();
    LocalDate endTermDate = consentLengthDetails.getShortTermEndDate();

    var previousProductionRows = getShortTermProductionMonths(currentVersion);
    var monthForms = initializeShortTermProductionMonths(startTermDate, endTermDate);
    var mergedForms = mergeExistingMonthDetailsWithForms(monthForms, previousProductionRows);
    return new ShortTermProductionForm(mergedForms);
  }

  private List<ShortTermProductionMonthForm> mergeExistingMonthDetailsWithForms(List<ShortTermProductionMonthForm> monthForms,
                                                                          List<ShortTermProductionMonth> previousProductionRows) {
    Map<YearMonth, ShortTermProductionMonth> mapOfPreviousProductionRows = previousProductionRows.stream().collect(
        Collectors.toMap(productionMonth ->
                YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()),
            Function.identity())
    );
    List<ShortTermProductionMonthForm> mergedList = new ArrayList<>();
    for (var shortTermProductionMonthForm : monthForms) {
      var previousProductionRow = mapOfPreviousProductionRows.get(
          YearMonth.of(
              Integer.parseInt(shortTermProductionMonthForm.getYear()),
              Month.valueOf(shortTermProductionMonthForm.getMonth().toUpperCase())
          )
      );
      ShortTermProductionMonthForm mergedShortTermProductionMonthForm;
      if (previousProductionRow != null) {
        mergedShortTermProductionMonthForm = new ShortTermProductionMonthForm();
        mergedShortTermProductionMonthForm.setMonth(previousProductionRow.getMonth());
        mergedShortTermProductionMonthForm.setYear(previousProductionRow.getYear().toString());
        mergedShortTermProductionMonthForm.setConsentDays(shortTermProductionMonthForm.getConsentDays());
        mergedShortTermProductionMonthForm.setStartDate(previousProductionRow.getStartDate());
        mergedShortTermProductionMonthForm.setEndDate(previousProductionRow.getEndDate());

        productionRowService.populateFormWithPreviousProductionRow(previousProductionRow, mergedShortTermProductionMonthForm);
      } else {
        mergedShortTermProductionMonthForm = shortTermProductionMonthForm;
      }
      mergedList.add(mergedShortTermProductionMonthForm);
    }
    return mergedList;
  }

  private List<ShortTermProductionMonthForm> initializeShortTermProductionMonths(LocalDate startTermDate, LocalDate endTermDate) {
    List<ShortTermProductionMonthForm> shortTermProductionMonthForms = new ArrayList<>();

    YearMonth endTermYearMonth = YearMonth.of(endTermDate.getYear(), endTermDate.getMonth());

    for (YearMonth yearMonth = YearMonth.of(startTermDate.getYear(), startTermDate.getMonth());
         yearMonth.isBefore(endTermYearMonth.plusMonths(1));
         yearMonth = yearMonth.plusMonths(1)) {
      ShortTermProductionMonthForm monthForm = getShortTermMonthForm(yearMonth, startTermDate, endTermDate);
      shortTermProductionMonthForms.add(monthForm);
    }

    return shortTermProductionMonthForms;
  }

  private ShortTermProductionMonthForm getShortTermMonthForm(YearMonth yearMonth, LocalDate startTermDate,
                                                             LocalDate endTermDate) {
    ShortTermProductionMonthForm monthForm = new ShortTermProductionMonthForm();
    monthForm.setMonth(yearMonth.getMonth());
    monthForm.setYear(String.valueOf(yearMonth.getYear()));

    LocalDate monthFormStartDate = DateUtils.max(startTermDate, yearMonth.atDay(1));
    monthForm.setStartDate(monthFormStartDate);

    LocalDate monthFormEndDate = DateUtils.min(endTermDate, yearMonth.atEndOfMonth());
    monthForm.setEndDate(monthFormEndDate);

    int consentDays = Period.between(monthFormStartDate, DateUtils.min(endTermDate, monthFormEndDate)).getDays() + 1;
    monthForm.setConsentDays(consentDays);

    monthForm.setOilUnits(ProductionUnit.SCM_PER_MONTH);
    monthForm.setGasUnits(ProductionUnit.KSCM_PER_MONTH);

    return monthForm;
  }

  @Transactional
  public void saveShortTermProductionDetails(ApplicationVersion currentVersion, ShortTermProductionForm form) {
    List<ShortTermProductionMonthForm> shortTermProductionMonthForms = form.getShortTermProductionMonthForms();
    shortTermProductionMonthRepository.deleteAllByApplicationVersion(currentVersion);

    shortTermProductionMonthForms.forEach(monthProductionForm -> saveShortTermProductionMonthDetails(
        currentVersion,
        monthProductionForm
    ));
  }

  public void saveShortTermProductionMonthDetails(ApplicationVersion applicationVersion,
                                                  ShortTermProductionMonthForm monthProductionForm) {
    ShortTermProductionMonth shortTermProductionMonth = new ShortTermProductionMonth();

    shortTermProductionMonth.setApplicationVersion(applicationVersion);
    shortTermProductionMonth.setYear(Integer.parseInt(monthProductionForm.getYear()));
    shortTermProductionMonth.setMonth(Month.valueOf(monthProductionForm.getMonth().toUpperCase()));
    shortTermProductionMonth.setStartDate(monthProductionForm.getStartDate());
    shortTermProductionMonth.setEndDate(monthProductionForm.getEndDate());

    try {
      productionRowService.updateProductionRowFromForm(monthProductionForm, shortTermProductionMonth);
    } catch (NoSuchElementException  e) {
      throw new RuntimeException(e);
    }
    shortTermProductionMonthRepository.save(shortTermProductionMonth);
  }
}
