package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;

@Service
class BulkAssignCaseOfficerService {

  private final CaseAssignmentService caseAssignmentService;

  BulkAssignCaseOfficerService(CaseAssignmentService caseAssignmentService) {
    this.caseAssignmentService = caseAssignmentService;
  }

  @Transactional
  public void assignCaseOfficer(
      Collection<ApplicationVersion> applicationVersions,
      ServiceUserDetail caseOfficer,
      ServiceUserDetail user
  ) {
    for (var applicationVersion : applicationVersions) {
      if (caseOfficer.wuaId().equals(applicationVersion.getCaseOfficerWuaId())) {
        continue;
      }

      caseAssignmentService.assignCaseOfficer(applicationVersion, caseOfficer, user);
    }
  }

  List<EnergyPortalUserDto> getAvailableCaseOfficers() {
    return caseAssignmentService.getActiveCaseOfficers();
  }

  String getNotificationBannerSuccessMessage(int numberOfApplications, EnergyPortalUserDto caseOfficerUser) {
    return "%s has been assigned to %d %s".formatted(
        caseOfficerUser.displayName(),
        numberOfApplications,
        numberOfApplications > 1 ? "applications" : "application");
  }

}
