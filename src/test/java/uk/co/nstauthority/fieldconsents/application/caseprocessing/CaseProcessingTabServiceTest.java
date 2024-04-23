package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;

@ExtendWith(MockitoExtension.class)
class CaseProcessingTabServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final ApplicationVersion APPLICATION_VERSION =
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

  @Mock
  private ApplicationAccessService applicationAccessService;

  @InjectMocks
  private CaseProcessingTabService caseProcessingTabService;

  @Test
  void getRegulatorTabsAvailableToUser_andAllRegulatorTabsAllowed() {
    when(applicationAccessService.hasApplicationPermission(eq(USER), eq(APPLICATION_VERSION), anySet())).thenReturn(true);

    var tabs = CaseProcessingTab.REGULATOR_TABS.stream().toList();
    assertThat(caseProcessingTabService.getRegulatorTabsAvailableToUser(USER, APPLICATION_VERSION)).isEqualTo(tabs);
  }

  @Test
  void getRegulatorTabsAvailableToUser_andNoRegulatorTabsAllowed() {
    when(applicationAccessService.hasApplicationPermission(eq(USER), eq(APPLICATION_VERSION), anySet())).thenReturn(false);
    assertThat(caseProcessingTabService.getRegulatorTabsAvailableToUser(USER, APPLICATION_VERSION)).isEmpty();
  }

  @Test
  void getConsulteeTabsAvailableToUser_andAllConsulteeTabsAllowed() {
    when(applicationAccessService.hasApplicationPermission(eq(USER), eq(APPLICATION_VERSION), anySet())).thenReturn(true);

    var tabs = CaseProcessingTab.CONSULTEE_TABS.stream().toList();
    assertThat(caseProcessingTabService.getConsulteeTabsAvailableToUser(USER, APPLICATION_VERSION)).isEqualTo(tabs);
  }

  @Test
  void getConsulteeTabsAvailableToUser_andNoConsulteeTabsAllowed() {
    when(applicationAccessService.hasApplicationPermission(eq(USER), eq(APPLICATION_VERSION), anySet())).thenReturn(false);
    assertThat(caseProcessingTabService.getConsulteeTabsAvailableToUser(USER, APPLICATION_VERSION)).isEmpty();
  }

  @Test
  void getIndustryTabsAvailableToUser_andAllIndustryTabsAllowed() {
    when(applicationAccessService.hasApplicationPermission(eq(USER), eq(APPLICATION_VERSION), anySet())).thenReturn(true);

    var tabs = CaseProcessingTab.INDUSTRY_TABS.stream().toList();
    assertThat(caseProcessingTabService.getIndustryTabsAvailableToUser(USER, APPLICATION_VERSION)).isEqualTo(tabs);
  }

  @Test
  void getIndustryTabsAvailableToUser_andNoIndustryTabsAllowed() {
    when(applicationAccessService.hasApplicationPermission(eq(USER), eq(APPLICATION_VERSION), anySet())).thenReturn(false);
    assertThat(caseProcessingTabService.getIndustryTabsAvailableToUser(USER, APPLICATION_VERSION)).isEmpty();
  }
}
