package uk.co.nstauthority.fieldconsents.application.otherlegacydata;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@Service
public class OtherLegacyDataSummaryService {

  static final String INCREASE_PROMPT = "Is this application for an increase in production?";
  static final String ES_REFERENCE_PROMPT = "Associated PON15D or PETS Production Operation ES reference number";
  static final String UPLIFT_PROMPT = "Uplift percentage requested (applies to maximum production figures only)";
  static final String FIELD_LOCATION_PROMPT = "Where are the field(s) located?";
  static final String PREVIOUS_CONSENT_PROMPT = "Previous year (%s) consent (%s)";
  static final String PREVIOUS_RATE_ACTUAL_PROMPT = "Previous year (%s) rate actual (%s)";
  static final String TERMINAL_NAME_PROMPT = "Terminal name";
  static final String TERMINAL_LOCATION_PROMPT = "Terminal location (county)";
  static final String EIA_PROJECT_PROMPT = "Is this a \"project\" for the purposes of EIA Regulations 2020?";

  private final OtherLegacyDataRepository otherLegacyDataRepository;
  private final ConsentLengthService consentLengthService;

  @Autowired
  OtherLegacyDataSummaryService(OtherLegacyDataRepository otherLegacyDataRepository,
                                ConsentLengthService consentLengthService) {
    this.otherLegacyDataRepository = otherLegacyDataRepository;
    this.consentLengthService = consentLengthService;
  }

  public Optional<OtherLegacyData> findOtherLegacyData(ApplicationVersion applicationVersion) {
    return otherLegacyDataRepository.findByApplicationVersion(applicationVersion);
  }

  public SummaryCard getOtherLegacyDataSummaryCard(OtherLegacyData otherLegacyData) {
    var summaryDataView = new SummaryDataView(new ArrayList<>());

    if (otherLegacyData.getIncreaseInProduction() != null) {
      summaryDataView.addKeyValue(INCREASE_PROMPT, otherLegacyData.getIncreaseInProduction());
    }

    if (otherLegacyData.getEsReference() != null) {
      summaryDataView.addKeyValue(ES_REFERENCE_PROMPT, otherLegacyData.getEsReference());
    }

    if (otherLegacyData.getUpliftPercentage() != null) {
      summaryDataView.addKeyValue(UPLIFT_PROMPT, otherLegacyData.getUpliftPercentage()
      );
    }

    if (otherLegacyData.getFieldLocation() != null) {
      summaryDataView.addKeyValue(FIELD_LOCATION_PROMPT, otherLegacyData.getFieldLocation());
    }

    if (otherLegacyData.getPreviousYearConsentHistory() != null
        || otherLegacyData.getPreviousYearActuals() != null) {
      var consentLengthDetails = consentLengthService.getConsentLengthDetails(otherLegacyData.getApplicationVersion());
      var previousYear = consentLengthService.getProposedConsentStartDate(consentLengthDetails).getYear() - 1;
      var unitDisplayName = FlareVentUnit.TONNES_PER_DAY.getDisplayName(); // this legacy data always used this unit

      if (otherLegacyData.getPreviousYearConsentHistory() != null) {
        summaryDataView.addKeyValue(PREVIOUS_CONSENT_PROMPT.formatted(previousYear, unitDisplayName),
            otherLegacyData.getPreviousYearConsentHistory());
      }

      if (otherLegacyData.getPreviousYearActuals() != null) {
        summaryDataView.addKeyValue(PREVIOUS_RATE_ACTUAL_PROMPT.formatted(previousYear, unitDisplayName),
            otherLegacyData.getPreviousYearActuals());
      }
    }

    if (otherLegacyData.getTerminalName() != null) {
      summaryDataView.addKeyValue(TERMINAL_NAME_PROMPT, otherLegacyData.getTerminalName());
    }

    if (otherLegacyData.getTerminalLocation() != null) {
      summaryDataView.addKeyValue(TERMINAL_LOCATION_PROMPT, otherLegacyData.getTerminalLocation());
    }

    if (otherLegacyData.getProjectUnderEiaRegs() != null) {
      summaryDataView.addKeyValue(EIA_PROJECT_PROMPT, otherLegacyData.getProjectUnderEiaRegs());
    }

    if (summaryDataView.keyValues().isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    return SummaryCard.simpleSummaryCard(summaryDataView);
  }
}
