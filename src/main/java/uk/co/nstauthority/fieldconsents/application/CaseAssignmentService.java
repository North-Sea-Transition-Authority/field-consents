package uk.co.nstauthority.fieldconsents.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Service
public class CaseAssignmentService {

  private final ApplicationVersionRepository applicationVersionRepository;

  private final RegulatorTeamService regulatorTeamService;

  @Autowired
  public CaseAssignmentService(ApplicationVersionRepository applicationVersionRepository,
                               RegulatorTeamService regulatorTeamService) {
    this.applicationVersionRepository = applicationVersionRepository;
    this.regulatorTeamService = regulatorTeamService;
  }

  public void assignCaseOfficer(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    if (!regulatorTeamService.isCaseOfficer(user)) {
      throw new IllegalStateException(
          "Cannot assign case officer as user with wua id %s is not in a regulator case officer role"
              .formatted(user.wuaId()));
    }
    applicationVersion.setCaseOfficerWuaId(user.wuaId());
    applicationVersionRepository.save(applicationVersion);
  }

  public void unassignCaseOfficer(ApplicationVersion applicationVersion) {
    applicationVersion.setCaseOfficerWuaId(null);
    applicationVersionRepository.save(applicationVersion);
  }
}
