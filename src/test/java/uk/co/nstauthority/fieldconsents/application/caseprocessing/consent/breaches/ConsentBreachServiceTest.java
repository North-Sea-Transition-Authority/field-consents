package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsentBreachServiceTest {

  @Mock
  private ConsentBreachRepository consentBreachRepository;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Mock
  private ConsentService consentService;

  private ConsentBreachService consentBreachService;

  private ServiceUserDetail user;

  private Application application;

  @BeforeEach
  void setUp() {
    application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    consentBreachService = spy(new ConsentBreachService(consentBreachRepository, clock));

    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void saveConsentBreach_createConsentBreach() {
    var consent = new Consent(3);
    String breachText = "Test Breach Text";

    consentBreachService.saveConsentBreach(consent, breachText, user);

    ArgumentCaptor<ConsentBreach> consentBreachArgumentCaptor =
        ArgumentCaptor.forClass(ConsentBreach.class);
    verify(consentBreachRepository).save(consentBreachArgumentCaptor.capture());

    assertThat(consentBreachArgumentCaptor.getValue()).extracting(
        ConsentBreach::getConsent,
        ConsentBreach::getAddedByWuaId,
        ConsentBreach::getBreachText,
        ConsentBreach::getAddedDateTime)
        .containsExactly(
            consent,
            user.wuaId(),
            breachText,
            clock.instant()
        );
  }

  @Test
  void saveConsentBreach_updateConsentBreach() {

    var consent = new Consent();
    var oldText = "old text";
    var newText = "new text";

    var consentBreach = new ConsentBreach(1);
    consentBreach.setConsent(consent);
    consentBreach.setAddedByWuaId(user.wuaId());
    consentBreach.setBreachText(oldText);
    consentBreach.setAddedDateTime(clock.instant());

    when(consentBreachRepository.findByConsent(consent))
        .thenReturn(Optional.of(consentBreach));

    consentBreachService.saveConsentBreach(consent, newText, user);

    ArgumentCaptor<ConsentBreach> consentBreachArgumentCaptor =
        ArgumentCaptor.forClass(ConsentBreach.class);
    verify(consentBreachRepository).save(consentBreachArgumentCaptor.capture());

    assertThat(consentBreachArgumentCaptor.getValue()).extracting(
        ConsentBreach::getConsent,
        ConsentBreach::getAddedByWuaId,
        ConsentBreach::getBreachText,
        ConsentBreach::getAddedDateTime
    ).containsExactly(
        consent,
        user.wuaId(),
        newText,
        clock.instant()
    );
  }

  @Test
  void deleteConsentBreach() {

    var breachText = "test breach text";
    var consent = new Consent(2);
    Integer consentId = 1;
    var consentBreach = new ConsentBreach(consentId);
    consentBreach.setConsent(consent);
    consentBreach.setBreachText(breachText);
    consentBreach.setAddedByWuaId(user.wuaId());
    consentBreach.setAddedDateTime(clock.instant());

    consentBreachService.deleteConsentBreach(consentBreach);

    ArgumentCaptor<ConsentBreach> consentBreachArgumentCaptor =
        ArgumentCaptor.forClass(ConsentBreach.class);
    verify(consentBreachRepository).delete(consentBreachArgumentCaptor.capture());

    assertThat(consentBreachArgumentCaptor.getValue()).extracting(
        ConsentBreach::getId,
        ConsentBreach::getConsent,
        ConsentBreach::getAddedByWuaId,
        ConsentBreach::getBreachText,
        ConsentBreach::getAddedDateTime
    ).containsExactly(
        consentId,
        consent,
        user.wuaId(),
        breachText,
        clock.instant()
    );
  }

  @Test
  void findConsentBreachByApplication() {
    var consentBreach = new ConsentBreach();
    when(consentBreachRepository.findByConsent_Application(application))
        .thenReturn(Optional.of(consentBreach));
    assertThat(consentBreachService.findConsentBreachByApplication(application))
        .contains(consentBreach);
  }

  @Test
  void findConsentBreachByConsent() {
    var consentBreach = new ConsentBreach();
    var consent = new Consent();
    when(consentBreachRepository.findByConsent(consent))
        .thenReturn(Optional.of(consentBreach));
    assertThat(consentBreachService.findConsentBreachByConsent(consent))
        .contains(consentBreach);
  }

  @Test
  void getConsentBreachByApplication() {
    var consentBreach = new ConsentBreach();
    when(consentBreachRepository.findByConsent_Application(application))
        .thenReturn(Optional.of(consentBreach));
    assertThat(consentBreachService.getConsentBreachByApplication(application))
        .isEqualTo(consentBreach);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void consentBreachExists(boolean exists) {
    when(consentBreachRepository.existsByConsent_Application(application))
        .thenReturn(exists);
    assertThat(consentBreachService.consentBreachExists(application))
        .isEqualTo(exists);
  }

  @Test
  void getConsentBreachByApplication_missingConsent_throwsException() {
    assertThrows(IllegalStateException.class,
        () -> consentBreachService.getConsentBreachByApplication(application));
  }

}
