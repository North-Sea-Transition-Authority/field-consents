package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CONSULTEE_TABS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.INDUSTRY_TABS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.REGULATOR_TABS;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;

@Service
public class CaseProcessingTabService {

  private final ApplicationAccessService applicationAccessService;

  CaseProcessingTabService(ApplicationAccessService applicationAccessService) {
    this.applicationAccessService = applicationAccessService;
  }

  public List<CaseProcessingTab> getRegulatorTabsAvailableToUser(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    return REGULATOR_TABS
        .stream()
        .filter(tab -> applicationAccessService.hasApplicationPermission(user, applicationVersion, tab.getRolePermissions()))
        .toList();
  }

  public List<CaseProcessingTab> getConsulteeTabsAvailableToUser(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    return CONSULTEE_TABS
        .stream()
        .filter(tab -> applicationAccessService.hasApplicationPermission(user, applicationVersion, tab.getRolePermissions()))
        .toList();
  }

  public List<CaseProcessingTab> getIndustryTabsAvailableToUser(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    return INDUSTRY_TABS
        .stream()
        .filter(tab -> applicationAccessService.hasApplicationPermission(user, applicationVersion, tab.getRolePermissions()))
        .toList();
  }
}
