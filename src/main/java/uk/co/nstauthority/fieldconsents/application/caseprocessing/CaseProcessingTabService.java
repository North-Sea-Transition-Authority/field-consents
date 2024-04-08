package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import java.util.EnumSet;
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

  public List<CaseProcessingTab> getTabsAvailableToUser(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    return EnumSet.allOf(CaseProcessingTab.class)
        .stream()
        .filter(tab -> applicationAccessService.hasApplicationPermission(user, applicationVersion, tab.getRolePermissions()))
        .toList();
  }
}
