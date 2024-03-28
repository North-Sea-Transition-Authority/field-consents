package uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag;

import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.IS_ACE_APPLICATION;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;

@Service
public class AceFlagService {

  private final ApplicationFlagService applicationFlagService;

  private final ConsentLengthService consentLengthService;

  private final Clock clock;

  @Autowired
  AceFlagService(ApplicationFlagService applicationFlagService,
                 ConsentLengthService consentLengthService,
                 Clock clock) {
    this.applicationFlagService = applicationFlagService;
    this.consentLengthService = consentLengthService;
    this.clock = clock;
  }

  public AceFlagForm getAceFlagForm(ApplicationVersion applicationVersion) {
    AceFlagForm aceFlagForm = new AceFlagForm();
    applicationFlagService.findFlagValue(applicationVersion, IS_ACE_APPLICATION)
        .ifPresent(aceFlagForm::setAceFlag);

    return aceFlagForm;
  }

  public void autoSetAceFlag(ApplicationVersion applicationVersion) {
    var consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);
    var followingYear = ZonedDateTime.now(clock).getYear() + 1;
    var annualForFollowingYear = ConsentLengthType.ANNUAL.equals(consentLengthDetails.getConsentLength())
        && consentLengthDetails.getAnnualConsentYear().equals(followingYear);
    var longTermForFollowingYear = ConsentLengthType.LONG_TERM.equals(consentLengthDetails.getConsentLength())
        && consentLengthDetails.getLongTermStartYear().equals(followingYear);

    setAceFlag(applicationVersion, annualForFollowingYear || longTermForFollowingYear);
  }

  public void setAceFlag(ApplicationVersion applicationVersion, boolean aceFlag) {
    applicationFlagService.addOrUpdateApplicationFlag(applicationVersion, IS_ACE_APPLICATION, aceFlag);
  }

  public boolean isAceApplication(ApplicationVersion applicationVersion) {
    return applicationFlagService.findFlagValue(applicationVersion, IS_ACE_APPLICATION)
        .orElseThrow(() -> new EntityNotFoundException("IS_ACE_APPLICATION flag not found for application version with id %s"
            .formatted(applicationVersion.getId())));
  }
}
