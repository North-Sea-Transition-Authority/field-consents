package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class ApplicationUpdateRequestViewService {

  private final ApplicationUpdateService applicationUpdateService;
  private final EnergyPortalUserService energyPortalUserService;

  ApplicationUpdateRequestViewService(ApplicationUpdateService applicationUpdateService,
                                      EnergyPortalUserService energyPortalUserService) {
    this.applicationUpdateService = applicationUpdateService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public ApplicationUpdateRequestView getOpenApplicationUpdateRequestView(ApplicationVersion applicationVersion) {
    var applicationUpdate = applicationUpdateService.getOpenApplicationUpdate(applicationVersion);
    var requesterWuaId = WebUserAccountId.from(applicationUpdate.getRequestedByWuaId());
    return new ApplicationUpdateRequestView(
        energyPortalUserService.getByWuaId(requesterWuaId).displayName(),
        DateUtils.format(applicationUpdate.getRequestedDateTime(), DATE_TIME),
        applicationUpdate.getRequestText(),
        DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME)
    );
  }
}
