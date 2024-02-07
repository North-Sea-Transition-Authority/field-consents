package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.math.BigDecimal;

public record ConsentProductionFiguresDto(
    BigDecimal minOil,
    BigDecimal maxOil,
    BigDecimal minGas,
    BigDecimal maxGas
) {
}
