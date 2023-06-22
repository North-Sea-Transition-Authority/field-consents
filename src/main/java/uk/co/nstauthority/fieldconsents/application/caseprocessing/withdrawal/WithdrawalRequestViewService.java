package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class WithdrawalRequestViewService {

  private final ApplicationWithdrawalService applicationWithdrawalService;

  private final EnergyPortalUserService energyPortalUserService;

  private final ApplicationService applicationService;

  public WithdrawalRequestViewService(ApplicationWithdrawalService applicationWithdrawalService,
                                      EnergyPortalUserService energyPortalUserService,
                                      ApplicationService applicationService) {
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.energyPortalUserService = energyPortalUserService;
    this.applicationService = applicationService;
  }

  public WithdrawalRequestView getWithdrawalRequestView(ApplicationVersion applicationVersion) {
    var applicationWithdrawal = applicationWithdrawalService.getOpenApplicationWithdrawal(applicationVersion);
    return new WithdrawalRequestView(
      applicationService.generateApplicationReference(applicationVersion),
      energyPortalUserService
          .getByWuaId(WebUserAccountId.from(applicationWithdrawal.getRequestedByWuaId())).displayName(),
      DateUtils.format(applicationWithdrawal.getRequestedDateTime(), DATE_TIME),
      applicationWithdrawal.getRequestText()
    );
  }
}
