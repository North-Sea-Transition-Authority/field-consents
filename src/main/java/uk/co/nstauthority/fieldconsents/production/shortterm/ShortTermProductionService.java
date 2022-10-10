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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;

@Service
public class ShortTermProductionService {

  private final ProductionRowService productionMonthService;
  private final ShortTermProductionMonthRepository shortTermProductionMonthRepository;

  @Autowired
  public ShortTermProductionService(ProductionRowService productionRowService,
                                    ShortTermProductionMonthRepository shortTermProductionMonthRepository) {
    this.productionMonthService = productionRowService;
    this.shortTermProductionMonthRepository = shortTermProductionMonthRepository;
  }

  public ShortTermProductionForm getShortTermProductionForm(ApplicationVersion currentVersion,
                                                            LocalDate startTermDate,
                                                            LocalDate endTermDate) {
    var previousProductionRows = shortTermProductionMonthRepository
        .findAllByApplicationVersion(currentVersion);
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

        productionMonthService.populateFormWithPreviousProductionRow(previousProductionRow, mergedShortTermProductionMonthForm);
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
         yearMonth.isBefore(endTermYearMonth)
             || yearMonth.atDay(startTermDate.getDayOfMonth()).isBefore(endTermDate);
         yearMonth = yearMonth.plusMonths(1)) {
      ShortTermProductionMonthForm monthForm = getShortTermMonthForm(yearMonth, startTermDate, endTermDate);
      shortTermProductionMonthForms.add(monthForm);
    }

    // Add the last month of the term unless the term is as short as 1 month, otherwise the same month would be added twice
    if (!DateUtils.isSameMonth(startTermDate, endTermDate)) {
      ShortTermProductionMonthForm monthForm = getShortTermMonthForm(endTermYearMonth, startTermDate, endTermDate);
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
      productionMonthService.updateProductionRowFromForm(monthProductionForm, shortTermProductionMonth);
    } catch (NoSuchElementException  e) {
      throw new RuntimeException(e);
    }
    shortTermProductionMonthRepository.save(shortTermProductionMonth);
  }
}
