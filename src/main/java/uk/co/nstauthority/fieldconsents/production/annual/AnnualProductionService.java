package uk.co.nstauthority.fieldconsents.production.annual;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Service
public class AnnualProductionService {

  private final AnnualProductionMonthRepository annualProductionMonthRepository;

  @Autowired
  public AnnualProductionService(AnnualProductionMonthRepository annualProductionMonthRepository) {
    this.annualProductionMonthRepository = annualProductionMonthRepository;
  }

  public AnnualProductionForm getAnnualProductionForm(ApplicationVersion applicationVersion, String year) {

    var previousProductionRows = annualProductionMonthRepository
        .findAllByApplicationVersion(applicationVersion);
    var monthForms = initializeAnnualProductionMonthForms();
    var mergedForms = mergeExistingMonthDetailsWithForms(monthForms, previousProductionRows);
    return new AnnualProductionForm(mergedForms, year);
  }

  private List<AnnualProductionMonthForm> mergeExistingMonthDetailsWithForms(List<AnnualProductionMonthForm> monthForms,
                                                                             List<AnnualProductionMonth> previousProductionRows) {

    var mapOfPreviousProductionRows = previousProductionRows.stream().collect(
        Collectors.toMap(AnnualProductionMonth::getMonth, Function.identity()));
    List<AnnualProductionMonthForm> mergedList = new ArrayList<>();
    for (var annualProductionMonthForm : monthForms) {
      var previousProductionRow = mapOfPreviousProductionRows.get(
          Month.valueOf(annualProductionMonthForm.getMonth().toUpperCase())
      );
      AnnualProductionMonthForm mergedAnnualProductionMonthForm;
      if (previousProductionRow != null) {
        mergedAnnualProductionMonthForm = new AnnualProductionMonthForm();
        mergedAnnualProductionMonthForm.setMonth(previousProductionRow.getMonth());

        var oilMinInput = mergedAnnualProductionMonthForm.getOilMinValue();
        oilMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMinValue()));

        var oilMaxInput = mergedAnnualProductionMonthForm.getOilMaxValue();
        oilMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMaxValue()));

        var gasMinInput = mergedAnnualProductionMonthForm.getGasMinValue();
        gasMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMinValue()));

        var gasMaxInput = mergedAnnualProductionMonthForm.getGasMaxValue();
        gasMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMaxValue()));
      } else {
        mergedAnnualProductionMonthForm = annualProductionMonthForm;
      }
      mergedList.add(mergedAnnualProductionMonthForm);
    }
    return mergedList;
  }

  /** Initializes a list of AnnualProductionMonthForm with months from January to December. */
  private List<AnnualProductionMonthForm> initializeAnnualProductionMonthForms() {
    List<AnnualProductionMonthForm> annualProductionMonthForms = new LinkedList<>();
    for (Month month : Month.values()) {
      AnnualProductionMonthForm productionMonthForm = new AnnualProductionMonthForm();
      productionMonthForm.setMonth(month);
      annualProductionMonthForms.add(productionMonthForm);
    }
    return annualProductionMonthForms;
  }

  @Transactional
  public void saveAnnualProductionDetails(ApplicationVersion applicationVersion, AnnualProductionForm form) {
    List<AnnualProductionMonthForm> annualProductionMonthForms = form.getAnnualProductionMonthForms();
    annualProductionMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    annualProductionMonthForms.forEach(monthProductionForm -> createAnnualProductionMonthDetails(
        applicationVersion,
        monthProductionForm,
        form.getYear()
    ));
  }

  public void createAnnualProductionMonthDetails(ApplicationVersion applicationVersion,
                                                 AnnualProductionMonthForm productionMonthForm,
                                                 String year) {

    AnnualProductionMonth annualProductionMonth = new AnnualProductionMonth();

    annualProductionMonth.setYear(Integer.parseInt(year));
    annualProductionMonth.setMonth(Month.valueOf(productionMonthForm.getMonth().toUpperCase()));
    annualProductionMonth.setApplicationVersion(applicationVersion);
    try {
      BigDecimal oilMinValue = productionMonthForm.getOilMinValue().getInputValueAsBigDecimal()
          .orElseThrow(NoSuchElementException::new);
      BigDecimal oilMaxValue = productionMonthForm.getOilMaxValue().getInputValueAsBigDecimal()
          .orElseThrow(NoSuchElementException::new);
      BigDecimal gasMinValue = productionMonthForm.getGasMinValue().getInputValueAsBigDecimal()
          .orElseThrow(NoSuchElementException::new);
      BigDecimal gasMaxValue = productionMonthForm.getGasMaxValue().getInputValueAsBigDecimal()
          .orElseThrow(NoSuchElementException::new);

      annualProductionMonth.setOilMinValue(oilMinValue);
      annualProductionMonth.setOilMinUnit(ProductionUnit.SCM_PER_MONTH);
      annualProductionMonth.setOilMaxValue(oilMaxValue);
      annualProductionMonth.setOilMaxUnit(ProductionUnit.SCM_PER_MONTH);
      annualProductionMonth.setGasMinValue(gasMinValue);
      annualProductionMonth.setGasMinUnit(ProductionUnit.KSCM_PER_MONTH);
      annualProductionMonth.setGasMaxValue(gasMaxValue);
      annualProductionMonth.setGasMaxUnit(ProductionUnit.KSCM_PER_MONTH);
    } catch (NoSuchElementException e) {
      throw new RuntimeException(e);
    }
    annualProductionMonthRepository.save(annualProductionMonth);
  }
}
