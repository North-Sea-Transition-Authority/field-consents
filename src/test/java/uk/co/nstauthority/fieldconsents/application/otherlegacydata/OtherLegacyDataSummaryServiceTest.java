package uk.co.nstauthority.fieldconsents.application.otherlegacydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.EIA_PROJECT_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.ES_REFERENCE_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.FIELD_LOCATION_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.INCREASE_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.PREVIOUS_CONSENT_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.PREVIOUS_RATE_ACTUAL_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.TERMINAL_LOCATION_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.TERMINAL_NAME_PROMPT;
import static uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService.UPLIFT_PROMPT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@ExtendWith(MockitoExtension.class)
class OtherLegacyDataSummaryServiceTest {

  @Mock
  private OtherLegacyDataRepository otherLegacyDataRepository;

  @Mock
  private ConsentLengthService consentLengthService;

  @InjectMocks
  private OtherLegacyDataSummaryService otherLegacyDataSummaryService;

  private ApplicationVersion applicationVersion;

  private OtherLegacyData otherLegacyData;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    otherLegacyData = new OtherLegacyData();
    otherLegacyData.setApplicationVersion(applicationVersion);
  }

  @Test
  void findOtherLegacyData() {
    var otherLegacyData = getCompleteOtherLegacyData(applicationVersion);
    when(otherLegacyDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(otherLegacyData));

    assertThat(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .isEqualTo(Optional.of(otherLegacyData));
  }

  @Test
  void findOtherLegacyData_whenNotExists() {
    when(otherLegacyDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .isEmpty();
  }

  @Test
  void getOtherLegacyDataSummaryCard_noLegacyDataFieldsExists() {
    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getOtherLegacyDataSummaryCard_allLegacyDataFieldsExists() {
    var otherLegacyData = getCompleteOtherLegacyData(applicationVersion);
    var previousYear = getPreviousYear();
    var unitDisplayName = FlareVentUnit.TONNES_PER_DAY.getDisplayName();

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(INCREASE_PROMPT, otherLegacyData.getIncreaseInProduction())
                    .addKeyValue(ES_REFERENCE_PROMPT, otherLegacyData.getEsReference())
                    .addKeyValue(UPLIFT_PROMPT, otherLegacyData.getUpliftPercentage())
                    .addKeyValue(FIELD_LOCATION_PROMPT, otherLegacyData.getFieldLocation())
                    .addKeyValue(PREVIOUS_CONSENT_PROMPT.formatted(previousYear, unitDisplayName),
                        otherLegacyData.getPreviousYearConsentHistory())
                    .addKeyValue(PREVIOUS_RATE_ACTUAL_PROMPT.formatted(previousYear, unitDisplayName),
                        otherLegacyData.getPreviousYearActuals())
                    .addKeyValue(TERMINAL_NAME_PROMPT, otherLegacyData.getTerminalName())
                    .addKeyValue(TERMINAL_LOCATION_PROMPT, otherLegacyData.getTerminalLocation())
                    .addKeyValue(EIA_PROJECT_PROMPT, otherLegacyData.getProjectUnderEiaRegs())
            )
        );
  }

  private int getPreviousYear() {
    var consentLengthDetails = new ConsentLengthDetails();
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails))
        .thenReturn(LocalDate.now());
    return LocalDate.now().getYear() - 1;
  }

  @Test
  void getOtherLegacyDataSummaryCard_increaseInProductionExists() {
    otherLegacyData.setIncreaseInProduction(false);

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(INCREASE_PROMPT, otherLegacyData.getIncreaseInProduction())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_esReferenceExists() {
    otherLegacyData.setEsReference("TEST");

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(ES_REFERENCE_PROMPT, otherLegacyData.getEsReference())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_upliftPercentageExists() {
    otherLegacyData.setUpliftPercentage(1);

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(UPLIFT_PROMPT, otherLegacyData.getUpliftPercentage())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_fieldLocationExists() {
    otherLegacyData.setFieldLocation("TEST");

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(FIELD_LOCATION_PROMPT, otherLegacyData.getFieldLocation())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_previousYearConsentHistoryExists() {
    otherLegacyData.setPreviousYearConsentHistory(BigDecimal.ONE);
    var previousYear = getPreviousYear();
    var unitDisplayName = FlareVentUnit.TONNES_PER_DAY.getDisplayName();

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(PREVIOUS_CONSENT_PROMPT.formatted(previousYear, unitDisplayName),
                        otherLegacyData.getPreviousYearConsentHistory())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_previousYearActualsExists() {
    otherLegacyData.setPreviousYearActuals(BigDecimal.ONE);
    var previousYear = getPreviousYear();
    var unitDisplayName = FlareVentUnit.TONNES_PER_DAY.getDisplayName();

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(PREVIOUS_RATE_ACTUAL_PROMPT.formatted(previousYear, unitDisplayName),
                        otherLegacyData.getPreviousYearActuals())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_terminalNameExists() {
    otherLegacyData.setTerminalName("TEST");

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(TERMINAL_NAME_PROMPT, otherLegacyData.getTerminalName())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_terminalLocationExists() {
    otherLegacyData.setTerminalLocation("TEST");

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(TERMINAL_LOCATION_PROMPT, otherLegacyData.getTerminalLocation())
            )
        );
  }

  @Test
  void getOtherLegacyDataSummaryCard_projectUnderEiaRegsExists() {
    otherLegacyData.setProjectUnderEiaRegs(false);

    assertThat(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .isEqualTo(
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(EIA_PROJECT_PROMPT, otherLegacyData.getProjectUnderEiaRegs())
            )
        );
  }

  private OtherLegacyData getCompleteOtherLegacyData(ApplicationVersion applicationVersion) {
    var otherLegacyData = new OtherLegacyData();
    otherLegacyData.setApplicationVersion(applicationVersion);
    otherLegacyData.setIncreaseInProduction(true);
    otherLegacyData.setEsReference("TEST_ES_REFERENCE");
    otherLegacyData.setUpliftPercentage(1);
    otherLegacyData.setFieldLocation("Test field location");
    otherLegacyData.setPreviousYearConsentHistory(BigDecimal.ONE);
    otherLegacyData.setPreviousYearActuals(BigDecimal.TEN);
    otherLegacyData.setTerminalName("Test terminal name");
    otherLegacyData.setTerminalLocation("Test location");
    otherLegacyData.setProjectUnderEiaRegs(true);
    return otherLegacyData;
  }
}
