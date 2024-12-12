package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class CaseProcessingTabServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final ApplicationVersion APPLICATION_VERSION =
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

  @Mock
  private FieldConsentsAccessService fieldConsentsAccessService;

  @InjectMocks
  private CaseProcessingTabService caseProcessingTabService;

  @Test
  void getRegulatorTabsAvailableToUser_andAllRegulatorTabsAllowed() {
    when(fieldConsentsAccessService.getRegulatorRoles(USER)).thenReturn(TeamType.REGULATOR.getAllowedRoles());

    var tabs = CaseProcessingTab.REGULATOR_TABS.stream().toList();
    assertThat(caseProcessingTabService.getRegulatorTabsAvailableToUser(USER)).isEqualTo(tabs);
  }

  @Test
  void getRegulatorTabsAvailableToUser_andNoRegulatorTabsAllowed() {
    assertThat(caseProcessingTabService.getRegulatorTabsAvailableToUser(USER)).isEmpty();
  }

  @Test
  void getConsulteeTabsAvailableToUser_andAllConsulteeTabsAllowed() {
    when(fieldConsentsAccessService.getConsulteeRoles(USER, APPLICATION_VERSION)).thenReturn(TeamType.CONSULTEE.getAllowedRoles());

    var tabs = CaseProcessingTab.CONSULTEE_TABS.stream().toList();
    assertThat(caseProcessingTabService.getConsulteeTabsAvailableToUser(USER, APPLICATION_VERSION)).isEqualTo(tabs);
  }

  @Test
  void getConsulteeTabsAvailableToUser_andNoConsulteeTabsAllowed() {
    assertThat(caseProcessingTabService.getConsulteeTabsAvailableToUser(USER, APPLICATION_VERSION)).isEmpty();
  }

  @Test
  void getIndustryTabsAvailableToUser_andAllIndustryTabsAllowed() {
    when(fieldConsentsAccessService.getIndustryRoles(USER, APPLICATION_VERSION)).thenReturn(TeamType.INDUSTRY.getAllowedRoles());

    var tabs = CaseProcessingTab.INDUSTRY_TABS.stream().toList();
    assertThat(caseProcessingTabService.getIndustryTabsAvailableToUser(USER, APPLICATION_VERSION)).isEqualTo(tabs);
  }

  @Test
  void getIndustryTabsAvailableToUser_andNoIndustryTabsAllowed() {
    assertThat(caseProcessingTabService.getIndustryTabsAvailableToUser(USER, APPLICATION_VERSION)).isEmpty();
  }
}
