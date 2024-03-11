package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class ConsentIssuingApprovalService {

  private final ConsentIssuingApprovalRepository consentIssuingApprovalRepository;
  private final EnergyPortalUserService energyPortalUserService;
  private final Clock clock;

  ConsentIssuingApprovalService(
      ConsentIssuingApprovalRepository consentIssuingApprovalRepository,
      EnergyPortalUserService energyPortalUserService,
      Clock clock
  ) {
    this.consentIssuingApprovalRepository = consentIssuingApprovalRepository;
    this.energyPortalUserService = energyPortalUserService;
    this.clock = clock;
  }

  @Transactional
  public void approveApplicationForConsentIssuing(Application application, ServiceUserDetail user) {
    var consentIssuingApproval = new ConsentIssuingApproval();

    consentIssuingApproval.setApplication(application);
    consentIssuingApproval.setApprovedByWuaId(user.wuaId());
    consentIssuingApproval.setApprovedInstant(clock.instant());

    consentIssuingApprovalRepository.save(consentIssuingApproval);
  }

  public boolean isApplicationApprovedForConsentIssuing(Application application) {
    return consentIssuingApprovalRepository.existsByApplicationId(application.getId());
  }

  public Optional<ConsentIssuingApprovalSummaryView> getConsentIssuingApprovalSummaryView(Application application) {
    return consentIssuingApprovalRepository.findByApplicationId(application.getId()).map(consentIssuingApproval -> {
      var approvedByUserEnergyPortalUserDto =
          energyPortalUserService.getByWuaId(WebUserAccountId.from(consentIssuingApproval.getApprovedByWuaId()));
      var approvedByUser = ServiceUserDetail.from(approvedByUserEnergyPortalUserDto);

      return ConsentIssuingApprovalSummaryView.from(consentIssuingApproval, approvedByUser);
    });
  }
}
