package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresInput;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionLongTermFigures;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public class ConsentDataForm {

  private ThreeFieldDateInput consentStartDateInput =
      new ThreeFieldDateInput("consentStartDateInput", "consent start date");
  private ThreeFieldDateInput consentEndDateInput =
      new ThreeFieldDateInput("consentEndDateInput", "consent end date");
  private ThreeFieldDateInput longTermProductionConsentScheduleStartDateInput =
      new ThreeFieldDateInput("longTermProductionConsentScheduleStartDateInput", "consent schedule start date");
  private ConsentProductionFiguresInput shortTermOrAnnualConsentProductionFiguresInput =
      new ConsentProductionFiguresInput();
  private Map<String, ConsentProductionFiguresInput> longTermConsentProductionFiguresInputs = new LinkedHashMap<>();
  private DecimalInput emissionDailyAverageInput = new DecimalInput("emissionDailyAverageInput", "daily average");

  public static ConsentDataForm fromShortTermOrAnnualProductionApplication(
      LocalDate startDate,
      LocalDate endDate,
      ConsentProductionFiguresDto shortTermOrAnnualConsentProductionFiguresDto
  ) {
    var form = new ConsentDataForm();

    form.consentStartDateInput.setDate(startDate);
    form.consentEndDateInput.setDate(endDate);
    form.shortTermOrAnnualConsentProductionFiguresInput.setInputValuesFromDto(shortTermOrAnnualConsentProductionFiguresDto);

    return form;
  }

  public static ConsentDataForm fromShortTermOrAnnualProductionApplication(ConsentData consentData) {
    var form = new ConsentDataForm();

    form.consentStartDateInput.setDate(consentData.getConsentStartDate());
    form.consentEndDateInput.setDate(consentData.getConsentEndDate());
    form.shortTermOrAnnualConsentProductionFiguresInput
        .setInputValuesFromDto(ConsentProductionFiguresDto.fromShortTermOrAnnualConsentProductionFigures(consentData));

    return form;
  }

  public static ConsentDataForm fromLongTermProductionApplication(
      LocalDate startDate,
      LocalDate endDate,
      Map<Integer, ConsentProductionFiguresDto> longTermConsentProductionFiguresDtos
  ) {
    var form = new ConsentDataForm();

    form.consentStartDateInput.setDate(startDate);
    form.consentEndDateInput.setDate(endDate);
    form.longTermProductionConsentScheduleStartDateInput.setDate(startDate);
    form.longTermConsentProductionFiguresInputs.putAll(
        longTermConsentProductionFiguresDtos.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(
                StreamUtils.toLinkedHashMap(
                    entry -> entry.getKey().toString(),
                    entry -> ConsentProductionFiguresInput.withDefaultValuesFromDto(entry.getValue())
                )
            ));

    return form;
  }

  public static ConsentDataForm fromLongTermProductionApplication(
      ConsentData consentData,
      List<ConsentProductionLongTermFigures> consentProductionLongTermFiguresList
  ) {
    var form = new ConsentDataForm();

    form.consentStartDateInput.setDate(consentData.getConsentStartDate());
    form.consentEndDateInput.setDate(consentData.getConsentEndDate());
    form.longTermProductionConsentScheduleStartDateInput.setDate(consentData.getLongTermProductionConsentScheduleStartDate());
    form.longTermConsentProductionFiguresInputs.putAll(
        consentProductionLongTermFiguresList.stream()
            .sorted(Comparator.comparing(ConsentProductionLongTermFigures::getYear))
            .collect(
                StreamUtils.toLinkedHashMap(
                    consentProductionLongTermFigures -> consentProductionLongTermFigures.getYear().toString(),
                    consentProductionLongTermFigures ->
                        ConsentProductionFiguresInput.withDefaultValuesFromDto(
                            ConsentProductionFiguresDto.fromConsentProductionLongTermFigures(consentProductionLongTermFigures))
                )
            ));

    return form;
  }

  public static ConsentDataForm fromEmissionApplication(
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal emissionDailyAverage
  ) {
    var form = new ConsentDataForm();

    form.consentStartDateInput.setDate(startDate);
    form.consentEndDateInput.setDate(endDate);
    form.emissionDailyAverageInput.setInputValue(bigDecimalToFormattedString(emissionDailyAverage));

    return form;
  }

  public static ConsentDataForm fromEmissionApplication(ConsentData consentData) {
    var form = new ConsentDataForm();

    form.consentStartDateInput.setDate(consentData.getConsentStartDate());
    form.consentEndDateInput.setDate(consentData.getConsentEndDate());
    form.emissionDailyAverageInput.setInputValue(bigDecimalToFormattedString(consentData.getEmissionDailyAverage()));

    return form;
  }

  public ThreeFieldDateInput getConsentStartDateInput() {
    return consentStartDateInput;
  }

  public void setConsentStartDateInput(ThreeFieldDateInput consentStartDateInput) {
    this.consentStartDateInput = consentStartDateInput;
  }

  public ThreeFieldDateInput getConsentEndDateInput() {
    return consentEndDateInput;
  }

  public void setConsentEndDateInput(ThreeFieldDateInput consentEndDateInput) {
    this.consentEndDateInput = consentEndDateInput;
  }

  public ThreeFieldDateInput getLongTermProductionConsentScheduleStartDateInput() {
    return longTermProductionConsentScheduleStartDateInput;
  }

  public void setLongTermProductionConsentScheduleStartDateInput(
      ThreeFieldDateInput longTermProductionConsentScheduleStartDateInput
  ) {
    this.longTermProductionConsentScheduleStartDateInput = longTermProductionConsentScheduleStartDateInput;
  }

  public ConsentProductionFiguresInput getShortTermOrAnnualConsentProductionFiguresInput() {
    return shortTermOrAnnualConsentProductionFiguresInput;
  }

  public void setShortTermOrAnnualConsentProductionFiguresInput(
      ConsentProductionFiguresInput shortTermOrAnnualConsentProductionFiguresInput
  ) {
    this.shortTermOrAnnualConsentProductionFiguresInput = shortTermOrAnnualConsentProductionFiguresInput;
  }

  public Map<String, ConsentProductionFiguresInput> getLongTermConsentProductionFiguresInputs() {
    return longTermConsentProductionFiguresInputs;
  }

  public void setLongTermConsentProductionFiguresInputs(
      Map<String, ConsentProductionFiguresInput> longTermConsentProductionFiguresInputs
  ) {
    this.longTermConsentProductionFiguresInputs = longTermConsentProductionFiguresInputs;
  }

  public DecimalInput getEmissionDailyAverageInput() {
    return emissionDailyAverageInput;
  }

  public void setEmissionDailyAverageInput(DecimalInput emissionDailyAverageInput) {
    this.emissionDailyAverageInput = emissionDailyAverageInput;
  }
}
