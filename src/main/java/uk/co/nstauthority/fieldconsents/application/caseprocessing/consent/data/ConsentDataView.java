package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.format;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public record ConsentDataView(
    String consentStartDate,
    String consentEndDate,
    ConsentProductionFiguresView shortTermOrAnnualConsentProductionFiguresView,
    Map<String, ConsentProductionFiguresView> longTermConsentProductionFiguresViews,
    String emissionMaxRate
) {

  public static ConsentDataView fromShortTermOrAnnualProductionApplication(
      ConsentData consentData,
      ConsentProductionFiguresDto shortTermOrAnnualConsentProductionFiguresDto
  ) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        ConsentProductionFiguresView.from(shortTermOrAnnualConsentProductionFiguresDto),
        null,
        null
    );
  }

  public static ConsentDataView fromLongTermProductionApplication(
      ConsentData consentData,
      Map<Integer, ConsentProductionFiguresDto> longTermConsentProductionFiguresDtos
  ) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        null,
        longTermConsentProductionFiguresDtos.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(
                StreamUtils.toLinkedHashMap(
                    entry -> entry.getKey().toString(),
                    entry -> ConsentProductionFiguresView.from(entry.getValue())
                )
            ),
        null
    );
  }

  public static ConsentDataView fromFlareOrVentApplication(ConsentData consentData, BigDecimal emissionMaxRate) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        null,
        null,
        bigDecimalToFormattedString(emissionMaxRate)
    );
  }
}
