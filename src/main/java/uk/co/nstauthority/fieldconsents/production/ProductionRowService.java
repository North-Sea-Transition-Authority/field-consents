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
    BigDecimal oilMinValue = productionRowForm.getOilMinValue().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal oilMaxValue = productionRowForm.getOilMaxValue().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal gasMinValue = productionRowForm.getGasMinValue().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal gasMaxValue = productionRowForm.getGasMaxValue().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);

    productionRow.setOilMinValue(oilMinValue);
    productionRow.setOilMaxValue(oilMaxValue);
    productionRow.setGasMinValue(gasMinValue);
    productionRow.setGasMaxValue(gasMaxValue);
  }

  public void populateFormWithPreviousProductionRow(ProductionRow previousProductionRow,
                                                    ProductionRowForm mergedProductionRowForm) {

    var oilMinInput = mergedProductionRowForm.getOilMinValue();
    oilMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMinValue()));

    var oilMaxInput = mergedProductionRowForm.getOilMaxValue();
    oilMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getOilMaxValue()));

    var gasMinInput = mergedProductionRowForm.getGasMinValue();
    gasMinInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMinValue()));

    var gasMaxInput = mergedProductionRowForm.getGasMaxValue();
    gasMaxInput.setInputValue(DecimalFormatUtils.bigDecimalToFormattedString(previousProductionRow.getGasMaxValue()));
  }
}
