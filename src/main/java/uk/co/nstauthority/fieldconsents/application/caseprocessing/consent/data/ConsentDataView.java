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
    String emissionDailyAverage,
    Map<String, String> longTermConsentEmissionFiguresViews
) {

  public static ConsentDataView fromShortTermOrAnnualProductionApplication(ConsentData consentData) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        null,
        ConsentProductionFiguresView.fromShortTermOrAnnualConsentProductionFigures(consentData),
        null,
        null,
        null
    );
  }

  public static ConsentDataView fromLongTermProductionApplication(
      ConsentData consentData,
      Map<String, ConsentProductionFiguresView> consentDataLongTermProductionFiguresViews
  ) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        format(consentData.getLongTermProductionConsentProductionFromDate(), DateUtils.LONG_DATE),
        null,
        consentDataLongTermProductionFiguresViews,
        null,
        null
    );
  }

  public static ConsentDataView fromShortTermOrAnnualEmissionApplication(ConsentData consentData) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        null,
        null,
        null,
        bigDecimalToFormattedString(consentData.getEmissionDailyAverage()),
        null
    );
  }

  public static ConsentDataView fromLongTermEmissionApplication(
      ConsentData consentData,
      Map<String, String> consentDataLongTermEmissionFiguresViews
  ) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
        null,
        null,
        null,
        null,
        consentDataLongTermEmissionFiguresViews
    );
  }
}
