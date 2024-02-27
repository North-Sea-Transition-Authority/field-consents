package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.format;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record ConsentDataView(
    String consentStartDate,
    String consentEndDate,
    String longTermProductionConsentProductionFromDate,
    ConsentProductionFiguresView shortTermOrAnnualConsentProductionFiguresView,
    Map<String, ConsentProductionFiguresView> longTermConsentProductionFiguresViews,
    String emissionDailyAverage
) {

  public static ConsentDataView fromShortTermOrAnnualProductionApplication(ConsentData consentData) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        null,
        ConsentProductionFiguresView.fromShortTermOrAnnualConsentProductionFigures(consentData),
        null,
        null
    );
  }

  public static ConsentDataView fromLongTermProductionApplication(
      ConsentData consentData,
      Map<String, ConsentProductionFiguresView> consentProductionLongTermFiguresViews
  ) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        format(consentData.getLongTermProductionConsentProductionFromDate(), DateUtils.LONG_DATE),
        null,
        consentProductionLongTermFiguresViews,
        null
    );
  }

  public static ConsentDataView fromEmissionApplication(ConsentData consentData) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        null,
        null,
        null,
        bigDecimalToFormattedString(consentData.getEmissionDailyAverage())
    );
  }
}
