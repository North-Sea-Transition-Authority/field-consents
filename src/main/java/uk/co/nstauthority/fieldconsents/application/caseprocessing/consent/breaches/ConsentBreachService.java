package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class ConsentBreachService {
  private final ConsentBreachRepository consentBreachRepository;
  private final Clock clock;

  public ConsentBreachService(ConsentBreachRepository consentBreachRepository,
                              Clock clock) {
    this.consentBreachRepository = consentBreachRepository;
    this.clock = clock;
  }

  @Transactional
  public void saveConsentBreach(
      Consent consent,
      String breachText,
      ServiceUserDetail user
  ) {
    findConsentBreachByConsent(consent).ifPresentOrElse(
        consentBreach -> updateConsentBreach(consentBreach, breachText),
        () -> createConsentBreach(consent, breachText, user));
  }

  private void createConsentBreach(
      Consent consent,
      String breachText,
      ServiceUserDetail user
  ) {
    var consentBreach = new ConsentBreach();
    consentBreach.setConsent(consent);
    consentBreach.setBreachText(breachText);
    consentBreach.setAddedByWuaId(user.wuaId());
    consentBreach.setAddedDateTime(clock.instant());
    consentBreachRepository.save(consentBreach);
  }

  private void updateConsentBreach(
      ConsentBreach consentBreach,
      String breachText
  ) {
    consentBreach.setBreachText(breachText);
    consentBreachRepository.save(consentBreach);
  }

  @Transactional
  public void deleteConsentBreach(ConsentBreach consentBreach) {
    consentBreachRepository.delete(consentBreach);
  }

  public Optional<ConsentBreach> findConsentBreachByApplication(Application application) {
    return consentBreachRepository.findByConsent_Application(application);
  }

  public Optional<ConsentBreach> findConsentBreachByConsent(Consent consent) {
    return consentBreachRepository.findByConsent(consent);
  }

  public boolean consentBreachExists(Application application) {
    return consentBreachRepository.existsByConsent_Application(application);
  }

  public ConsentBreach getConsentBreachByApplication(Application application) {
    return consentBreachRepository.findByConsent_Application(application)
        .orElseThrow(() ->
            new IllegalStateException("Consent breach for application with id %s not found".formatted(application.getId()))
        );
  }
}
