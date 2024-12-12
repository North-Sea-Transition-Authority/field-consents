package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CONSULTEE_TABS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.INDUSTRY_TABS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.REGULATOR_TABS;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class CaseProcessingTabService {

  private final FieldConsentsAccessService fieldConsentsAccessService;

  CaseProcessingTabService(FieldConsentsAccessService fieldConsentsAccessService) {
    this.fieldConsentsAccessService = fieldConsentsAccessService;
  }

  public List<CaseProcessingTab> getRegulatorTabsAvailableToUser(ServiceUserDetail user) {
    var regulatorRoles = fieldConsentsAccessService.getRegulatorRoles(user);
    return REGULATOR_TABS
        .stream()
        .filter(tab -> CollectionUtils.containsAny(regulatorRoles, tab.getRoles(TeamType.REGULATOR)))
        .toList();
  }

  public List<CaseProcessingTab> getConsulteeTabsAvailableToUser(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    var consulteeRoles = fieldConsentsAccessService.getConsulteeRoles(user, applicationVersion);
    return CONSULTEE_TABS
        .stream()
        .filter(tab -> CollectionUtils.containsAny(consulteeRoles, tab.getRoles(TeamType.CONSULTEE)))
        .toList();
  }

  public List<CaseProcessingTab> getIndustryTabsAvailableToUser(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    var industryRoles = fieldConsentsAccessService.getIndustryRoles(user, applicationVersion);
    return INDUSTRY_TABS
        .stream()
        .filter(tab -> CollectionUtils.containsAny(industryRoles, tab.getRoles(TeamType.INDUSTRY)))
        .toList();
  }

}
