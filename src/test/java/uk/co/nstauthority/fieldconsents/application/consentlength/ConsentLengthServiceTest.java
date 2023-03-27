package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.LONG_TERM_END_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.LONG_TERM_START_YEAR;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.END_DATE;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.START_DATE;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class ConsentLengthServiceTest {

  private static final String CONSENT_PERIOD_PROMPT = "Consent period";

  @Mock
  private ConsentLengthRepository consentLengthRepository;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  private ConsentLengthService consentLengthService;

  private ApplicationVersion applicationVersion;

  @Captor
  private ArgumentCaptor<ConsentLengthChangeEvent> captor;

  private ConsentLengthDetails consentLengthDetails;

  private ConsentLengthForm consentLengthForm;

  @BeforeEach
  void setup() {
    consentLengthService = new ConsentLengthService(consentLengthRepository, applicationEventPublisher);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getConsentLengthForm_withShortTermConsentLength() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));

    ConsentLengthForm form = consentLengthService.getConsentLengthForm(applicationVersion);

    assertThat(form.getConsentLengthType()).isEqualTo(ConsentLengthType.SHORT_TERM);
    assertThat(Integer.valueOf(form.getShortTermStartDate().getDayInput().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_START_DATE.getDayOfMonth());
    assertThat(Integer.valueOf(form.getShortTermStartDate().getMonthInput().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_START_DATE.getMonthValue());
    assertThat(Integer.valueOf(form.getShortTermStartDate().getYearInput().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_START_DATE.getYear());
    assertThat(Integer.valueOf(form.getShortTermEndDate().getDayInput().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_END_DATE.getDayOfMonth());
    assertThat(Integer.valueOf(form.getShortTermEndDate().getMonthInput().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_END_DATE.getMonthValue());
    assertThat(Integer.valueOf(form.getShortTermEndDate().getYearInput().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_END_DATE.getYear());

    assertNull(form.getAnnualConsentYear().getInputValue());
    assertNullLongTermForm(form);
  }

  @Test
  void getConsentLengthForm_withAnnualConsentLength() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));

    ConsentLengthForm form = consentLengthService.getConsentLengthForm(applicationVersion);

    assertThat(form.getConsentLengthType()).isEqualTo(ConsentLengthType.ANNUAL);
    assertThat(Integer.valueOf(form.getAnnualConsentYear().getInputValue())).isEqualTo(ANNUAL_CONSENT_YEAR);

    assertNullShortTermForm(form);
    assertNullLongTermForm(form);
  }

  @Test
  void getConsentLengthForm_withLongTermConsentLength() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));

    ConsentLengthForm form = consentLengthService.getConsentLengthForm(applicationVersion);

    assertThat(form.getConsentLengthType()).isEqualTo(ConsentLengthType.LONG_TERM);
    assertNull(form.getAnnualConsentYear().getInputValue());

    assertNullShortTermForm(form);

    assertThat(Integer.valueOf(form.getLongTermStartYear().getInputValue()))
        .isEqualTo(LONG_TERM_START_YEAR);
    assertThat(Integer.valueOf(form.getLongTermEndYear().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.LONG_TERM_END_YEAR);
  }

  private void assertNullShortTermForm(ConsentLengthForm form) {
    assertNull(form.getShortTermStartDate().getDayInput().getInputValue());
    assertNull(form.getShortTermStartDate().getMonthInput().getInputValue());
    assertNull(form.getShortTermStartDate().getYearInput().getInputValue());
    assertNull(form.getShortTermEndDate().getDayInput().getInputValue());
    assertNull(form.getShortTermEndDate().getMonthInput().getInputValue());
    assertNull(form.getShortTermEndDate().getYearInput().getInputValue());
  }

  private void assertNullLongTermForm(ConsentLengthForm form) {
    assertNull(form.getLongTermStartYear().getInputValue());
    assertNull(form.getLongTermEndYear().getInputValue());
  }

  @Test
  void saveConsentLengthDetails_withAnnualConsentLength() {
    consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    ConsentLengthDetails consentLengthDetails = getEntityFromArgumentCaptor();

    assertThat(consentLengthDetails.getConsentLength()).isEqualTo(ConsentLengthType.ANNUAL);
    assertThat(consentLengthDetails.getAnnualConsentYear()).isEqualTo(ANNUAL_CONSENT_YEAR);

    assertNull(consentLengthDetails.getShortTermStartDate());
    assertNull(consentLengthDetails.getShortTermEndDate());

    assertNull(consentLengthDetails.getLongTermStartYear());
    assertNull(consentLengthDetails.getLongTermEndYear());

    assertConsentLengthChangeEvent();
  }

  @Test
  void saveConsentLengthDetails_withShortTermConsentLength() {
    consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthForm();
    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    ConsentLengthDetails consentLengthDetails = getEntityFromArgumentCaptor();

    assertThat(consentLengthDetails.getConsentLength()).isEqualTo(ConsentLengthType.SHORT_TERM);
    assertNull(consentLengthDetails.getAnnualConsentYear());

    assertThat(consentLengthDetails.getShortTermStartDate()).isEqualTo(ConsentLengthTestUtil.SHORT_TERM_START_DATE);
    assertThat(consentLengthDetails.getShortTermEndDate()).isEqualTo(ConsentLengthTestUtil.SHORT_TERM_END_DATE);

    assertNull(consentLengthDetails.getLongTermStartYear());
    assertNull(consentLengthDetails.getLongTermEndYear());

    assertConsentLengthChangeEvent();
  }

  @Test
  void saveConsentLengthDetails_withLongTermConsentLength() {
    consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    ConsentLengthDetails consentLengthDetails = getEntityFromArgumentCaptor();

    assertThat(consentLengthDetails.getConsentLength()).isEqualTo(ConsentLengthType.LONG_TERM);
    assertNull(consentLengthDetails.getAnnualConsentYear());

    assertNull(consentLengthDetails.getShortTermStartDate());
    assertNull(consentLengthDetails.getShortTermEndDate());

    assertThat(consentLengthDetails.getLongTermStartYear()).isEqualTo(LONG_TERM_START_YEAR);
    assertThat(consentLengthDetails.getLongTermEndYear()).isEqualTo(ConsentLengthTestUtil.LONG_TERM_END_YEAR);

    assertConsentLengthChangeEvent();
  }

  private void assertConsentLengthChangeEvent() {
    verify(applicationEventPublisher).publishEvent(captor.capture());
    assertThat(captor.getValue().getApplicationVersionId()).isEqualTo(applicationVersion.getId());
    assertThat(captor.getValue().getClass()).isEqualTo(ConsentLengthChangeEvent.class);
    assertThat(captor.getValue().getSource().getClass()).isEqualTo(ConsentLengthService.class);
  }

  private ConsentLengthDetails getEntityFromArgumentCaptor() {
    ArgumentCaptor<ConsentLengthDetails> consentLengthDetailsArgumentCaptor = ArgumentCaptor.forClass(ConsentLengthDetails.class);
    verify(consentLengthRepository, times(1)).save(consentLengthDetailsArgumentCaptor.capture());

    return consentLengthDetailsArgumentCaptor.getValue();
  }

  @Test
  void findConsentLengthDetails_whenPresent() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));

    Optional<ConsentLengthDetails> consentLengthDetailsOptional = consentLengthService.findConsentLengthDetails(applicationVersion);

    assertThat(consentLengthDetailsOptional).isPresent();
  }

  @Test
  void findConsentLengthDetails_whenNotPresent() {
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    Optional<ConsentLengthDetails> consentLengthDetailsOptional = consentLengthService.findConsentLengthDetails(applicationVersion);

    assertThat(consentLengthDetailsOptional).isEmpty();
  }

  @Test
  void getConsentLengthDetails_whenPresent() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));

    ConsentLengthDetails expectedConsentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

    assertThat(expectedConsentLengthDetails.getConsentLength()).isEqualTo(ConsentLengthType.ANNUAL);
  }

  @Test
  void getConsentLengthDetails_whenNotPresent() {
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    var exception = Assertions.assertThrows(
        EntityNotFoundException.class,
        () -> consentLengthService.getConsentLengthDetails(applicationVersion)
    );

    Assertions.assertEquals("Consent details with application version id 1 not found.", exception.getMessage());
  }

  @Test
  void getConsentLengthSummaryCard_noSavedData() {
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    var summaryCard = consentLengthService.getConsentLengthSummaryCard(applicationVersion);

    assertThat(summaryCard)
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getConsentLengthSummaryCard_shortTerm() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));

    var summaryCard = consentLengthService.getConsentLengthSummaryCard(applicationVersion);

    assertThat(summaryCard).usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue(CONSENT_PERIOD_PROMPT, ConsentLengthType.SHORT_TERM.getDisplayName()),
                new SummaryKeyValue("Start date", DateUtils.format(START_DATE, DateUtils.SHORT_DATE)),
                new SummaryKeyValue("End date", DateUtils.format(END_DATE, DateUtils.SHORT_DATE))
            ))));
  }

  @Test
  void getConsentLengthSummaryCard_annual() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));

    var summaryCard = consentLengthService.getConsentLengthSummaryCard(applicationVersion);

    assertThat(summaryCard).usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue(CONSENT_PERIOD_PROMPT, ConsentLengthType.ANNUAL.getDisplayName()),
                new SummaryKeyValue("Year", String.valueOf(ANNUAL_CONSENT_YEAR))
            ))));
  }

  @Test
  void getConsentLengthSummaryCard_longTerm() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));

    var summaryCard = consentLengthService.getConsentLengthSummaryCard(applicationVersion);

    assertThat(summaryCard).usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue(CONSENT_PERIOD_PROMPT, ConsentLengthType.LONG_TERM.getDisplayName()),
                new SummaryKeyValue("Start year", String.valueOf(LONG_TERM_START_YEAR)),
                new SummaryKeyValue("End year", String.valueOf(LONG_TERM_END_YEAR))
            ))));
  }
}