package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class ConsentBreachSummaryService {

  private final EnergyPortalUserService energyPortalUserService;

  public ConsentBreachSummaryService(EnergyPortalUserService energyPortalUserService) {
    this.energyPortalUserService = energyPortalUserService;
  }

  public ConsentBreachView getConsentBreachView(ConsentBreach consentBreach) {
    return new ConsentBreachView(
        consentBreach.getBreachText(),
        energyPortalUserService.getByWuaId(
            WebUserAccountId.from(consentBreach.getAddedByWuaId()))
            .displayName(),
        DateUtils.format(consentBreach.getAddedDateTime(), DATE_TIME)
    );
  }
}
