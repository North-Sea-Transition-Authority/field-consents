package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class ConsentLengthServiceTest {

  @Mock
  private ConsentLengthRepository consentLengthRepository;

  @Mock
  private ApplicationService applicationService;

  private ConsentLengthService consentLengthService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthDetails consentLengthDetails;

  private ConsentLengthForm consentLengthForm;

  @BeforeEach
  void setup() {
    consentLengthService = new ConsentLengthService(consentLengthRepository);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getConsentLengthForm_withShortTermConsentLength() {
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));

    ConsentLengthForm form = consentLengthService.getConsentLengthForm(applicationVersion);

    assertThat(form.getConsentLengthType()).isEqualTo(ConsentLengthType.SHORT_TERM);
    assertThat(Integer.valueOf(form.getShortTermStartDay().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_START_DATE.getDayOfMonth());
    assertThat(Integer.valueOf(form.getShortTermStartMonth().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_START_DATE.getMonthValue());
    assertThat(Integer.valueOf(form.getShortTermStartYear().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_START_DATE.getYear());
    assertThat(Integer.valueOf(form.getShortTermEndDay().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_END_DATE.getDayOfMonth());
    assertThat(Integer.valueOf(form.getShortTermEndMonth().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.SHORT_TERM_END_DATE.getMonthValue());
    assertThat(Integer.valueOf(form.getShortTermEndYear().getInputValue()))
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
    assertThat(Integer.valueOf(form.getAnnualConsentYear().getInputValue())).isEqualTo(ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);

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
        .isEqualTo(ConsentLengthTestUtil.LONG_TERM_START_YEAR);
    assertThat(Integer.valueOf(form.getLongTermEndYear().getInputValue()))
        .isEqualTo(ConsentLengthTestUtil.LONG_TERM_END_YEAR);
  }

  private void assertNullShortTermForm(ConsentLengthForm form) {
    assertNull(form.getShortTermStartDay().getInputValue());
    assertNull(form.getShortTermStartMonth().getInputValue());
    assertNull(form.getShortTermStartYear().getInputValue());
    assertNull(form.getShortTermEndDay().getInputValue());
    assertNull(form.getShortTermEndMonth().getInputValue());
    assertNull(form.getShortTermEndYear().getInputValue());
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
    assertThat(consentLengthDetails.getAnnualConsentYear()).isEqualTo(ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);

    assertNull(consentLengthDetails.getShortTermStartDate());
    assertNull(consentLengthDetails.getShortTermEndDate());

    assertNull(consentLengthDetails.getLongTermStartYear());
    assertNull(consentLengthDetails.getLongTermEndYear());
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

    assertThat(consentLengthDetails.getLongTermStartYear()).isEqualTo(ConsentLengthTestUtil.LONG_TERM_START_YEAR);
    assertThat(consentLengthDetails.getLongTermEndYear()).isEqualTo(ConsentLengthTestUtil.LONG_TERM_END_YEAR);
  }

  private ConsentLengthDetails getEntityFromArgumentCaptor() {
    ArgumentCaptor<ConsentLengthDetails> consentLengthDetailsArgumentCaptor = ArgumentCaptor.forClass(ConsentLengthDetails.class);
    verify(consentLengthRepository, times(1)).save(consentLengthDetailsArgumentCaptor.capture());

    return consentLengthDetailsArgumentCaptor.getValue();
  }
}