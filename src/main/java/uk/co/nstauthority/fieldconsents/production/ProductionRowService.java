package uk.co.nstauthority.fieldconsents.production;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

/**
 * Implements common functionalities used across all production forms: annual, short term and long term.
 */

@Service
public class ProductionRowService {

  public static final String PRODUCTION_YEAR = "2022";

  public void addProductionDetailsToModelAndView(ModelAndView modelAndView) {
    modelAndView.addObject("requestYear", PRODUCTION_YEAR);
    modelAndView.addObject("oilUnit", ProductionUnit.SCM_PER_MONTH.getDisplayName());
    modelAndView.addObject("gasUnit", ProductionUnit.KSCM_PER_MONTH.getDisplayName());
  }

  public void updateProductionRowFromForm(ProductionRowForm monthProductionForm,
                                          ProductionRow productionRow) {
    BigDecimal oilMinValue = monthProductionForm.getOilMinValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal oilMaxValue = monthProductionForm.getOilMaxValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal gasMinValue = monthProductionForm.getGasMinValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal gasMaxValue = monthProductionForm.getGasMaxValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);

    productionRow.setOilMinValue(oilMinValue);
    productionRow.setOilMinUnit(ProductionUnit.SCM_PER_MONTH);
    productionRow.setOilMaxValue(oilMaxValue);
    productionRow.setOilMaxUnit(ProductionUnit.SCM_PER_MONTH);
    productionRow.setGasMinValue(gasMinValue);
    productionRow.setGasMinUnit(ProductionUnit.KSCM_PER_MONTH);
    productionRow.setGasMaxValue(gasMaxValue);
    productionRow.setGasMaxUnit(ProductionUnit.KSCM_PER_MONTH);
  }

  public void populateFormWithPreviousProductionRow(ProductionRow previousProductionRow,
                                                    ProductionRowForm mergedAnnualProductionRowForm) {

    var oilMinInput = mergedAnnualProductionRowForm.getOilMinValue();
    oilMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMinValue()));

    var oilMaxInput = mergedAnnualProductionRowForm.getOilMaxValue();
    oilMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMaxValue()));

    var gasMinInput = mergedAnnualProductionRowForm.getGasMinValue();
    gasMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMinValue()));

    var gasMaxInput = mergedAnnualProductionRowForm.getGasMaxValue();
    gasMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMaxValue()));

  }
}
