package uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.IS_ACE_APPLICATION;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;

@ExtendWith(MockitoExtension.class)
class AceFlagServiceTest {

  private static final Instant CURRENT_INSTANT = Instant.now();

  private static final Year CURRENT_YEAR = Year.now();

  private static final LocalDate CURRENT_DATE = LocalDate.now();

  @Mock
  private ApplicationFlagService applicationFlagService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private Clock clock;

  @InjectMocks
  private AceFlagService aceFlagService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getAceFlagForm_whenFlagNotPresent_thenNew() {
    when(applicationFlagService.findFlagValue(applicationVersion, IS_ACE_APPLICATION))
        .thenReturn(Optional.empty());

    var aceFlagForm = aceFlagService.getAceFlagForm(applicationVersion);

    assertThat(aceFlagForm)
        .usingRecursiveComparison()
        .isEqualTo(new AceFlagForm());
  }

  @Test
  void getAceFlagForm_whenFlagPresent_thenFormWithValueSet() {
    var expectedAceFlagForm = new AceFlagForm();
    expectedAceFlagForm.setAceFlag(Boolean.TRUE);

    when(applicationFlagService.findFlagValue(applicationVersion, IS_ACE_APPLICATION))
        .thenReturn(Optional.of(Boolean.TRUE));

    var aceFlagForm = aceFlagService.getAceFlagForm(applicationVersion);

    assertThat(aceFlagForm)
        .usingRecursiveComparison()
        .isEqualTo(expectedAceFlagForm);
  }

  @Test
  void autoSetAceFlag_whenAnnualForFollowingYear_thenTrue() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    int followingYear = CURRENT_YEAR.getValue() + 1;
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion, followingYear));

    aceFlagService.autoSetAceFlag(applicationVersion);

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, IS_ACE_APPLICATION, true);
  }

  @ParameterizedTest
  @MethodSource("getYearsExcludingFollowing")
  void autoSetAceFlag_whenAnnualForNotFollowingYear_thenFalse(int annualYear) {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion, annualYear));

    aceFlagService.autoSetAceFlag(applicationVersion);

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, IS_ACE_APPLICATION, false);
  }

  @Test
  void autoSetAceFlag_whenLongTermForFollowingYear_thenTrue() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    int followingYear = CURRENT_YEAR.getValue() + 1;
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil
            .getConsentLengthDetailsForLongTerm(applicationVersion, followingYear, followingYear + 1));

    aceFlagService.autoSetAceFlag(applicationVersion);

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, IS_ACE_APPLICATION, true);
  }

  @ParameterizedTest
  @MethodSource("getYearsExcludingFollowing")
  void autoSetAceFlag_whenLongTermForNotFollowingYear_thenFalse(int longTermStartYear) {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil
            .getConsentLengthDetailsForLongTerm(applicationVersion, longTermStartYear, longTermStartYear + 1));

    aceFlagService.autoSetAceFlag(applicationVersion);

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, IS_ACE_APPLICATION, false);
  }

  @ParameterizedTest
  @MethodSource("getShortTermStartEndDates")
  void autoSetAceFlag_whenShortTerm_thenFalse(LocalDate startDate, LocalDate endDate) {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion, startDate, endDate));

    aceFlagService.autoSetAceFlag(applicationVersion);

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, IS_ACE_APPLICATION, false);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void setAceFlag(boolean aceFlagValue) {
    aceFlagService.setAceFlag(applicationVersion, aceFlagValue);

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, IS_ACE_APPLICATION, aceFlagValue);
  }

  private static Stream<Arguments> getYearsExcludingFollowing() {
    return Stream.of(
        Arguments.of(CURRENT_YEAR.getValue() - 1),
        Arguments.of(CURRENT_YEAR.getValue()),
        Arguments.of(CURRENT_YEAR.getValue() + 2),
        Arguments.of(CURRENT_YEAR.getValue() + 3)
    );
  }

  private static Stream<Arguments> getShortTermStartEndDates() {
    return Stream.of(
        Arguments.of(CURRENT_DATE.minusYears(2),
            CURRENT_DATE.minusYears(2).plusMonths(3)),
        Arguments.of(CURRENT_DATE.minusYears(1),
            CURRENT_DATE.minusYears(1).plusMonths(3)),
        Arguments.of(CURRENT_DATE, CURRENT_DATE.plusMonths(3)),
        Arguments.of(CURRENT_DATE.plusYears(1),
            CURRENT_DATE.plusYears(1).plusMonths(3)),
        Arguments.of(CURRENT_DATE.plusYears(2),
            CURRENT_DATE.plusYears(2).plusMonths(3))
    );
  }

  @Test
  void isAceApplication_whenFlagFound_thenReturnAceFlagValue() {
    when(applicationFlagService.findFlagValue(applicationVersion, IS_ACE_APPLICATION))
        .thenReturn(Optional.of(true));

    assertThat(aceFlagService.isAceApplication(applicationVersion)).isTrue();
  }

  @Test
  void isAceApplication_whenFlagNotFound_thenThrowException() {
    when(applicationFlagService.findFlagValue(applicationVersion, IS_ACE_APPLICATION))
        .thenReturn(Optional.empty());

    var exception = Assertions.assertThrows(
        EntityNotFoundException.class,
        () -> aceFlagService.isAceApplication(applicationVersion)
    );

    Assertions.assertEquals("IS_ACE_APPLICATION flag not found for application version with id 1",
        exception.getMessage());
  }
}
