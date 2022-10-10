package uk.co.nstauthority.fieldconsents.production;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

/**
 * Implements common functionalities used across all production forms: annual, short term and long term.
 */

@Service
public class ProductionRowService {

  public void updateProductionRowFromForm(ProductionRowForm productionRowForm,
                                          ProductionRow productionRow) {
    BigDecimal oilMinValue = productionRowForm.getOilMinValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal oilMaxValue = productionRowForm.getOilMaxValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal gasMinValue = productionRowForm.getGasMinValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal gasMaxValue = productionRowForm.getGasMaxValue().getInputValueAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);

    productionRow.setOilMinValue(oilMinValue);
    productionRow.setOilMinUnit(productionRowForm.getOilMinUnit());
    productionRow.setOilMaxValue(oilMaxValue);
    productionRow.setOilMaxUnit(productionRowForm.getOilMaxUnit());
    productionRow.setGasMinValue(gasMinValue);
    productionRow.setGasMinUnit(productionRowForm.getGasMinUnit());
    productionRow.setGasMaxValue(gasMaxValue);
    productionRow.setGasMaxUnit(productionRowForm.getGasMaxUnit());
  }

  public void populateFormWithPreviousProductionRow(ProductionRow previousProductionRow,
                                                    ProductionRowForm mergedProductionRowForm) {

    var oilMinInput = mergedProductionRowForm.getOilMinValue();
    oilMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMinValue()));

    mergedProductionRowForm.setOilMinUnit(previousProductionRow.getOilMinUnit());

    var oilMaxInput = mergedProductionRowForm.getOilMaxValue();
    oilMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMaxValue()));

    mergedProductionRowForm.setOilMaxUnit(previousProductionRow.getOilMaxUnit());

    var gasMinInput = mergedProductionRowForm.getGasMinValue();
    gasMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMinValue()));

    mergedProductionRowForm.setGasMinUnit(previousProductionRow.getGasMinUnit());

    var gasMaxInput = mergedProductionRowForm.getGasMaxValue();
    gasMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMaxValue()));

    mergedProductionRowForm.setGasMaxUnit(previousProductionRow.getGasMaxUnit());

  }
}
