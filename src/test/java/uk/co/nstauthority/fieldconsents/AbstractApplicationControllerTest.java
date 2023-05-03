package uk.co.nstauthority.fieldconsents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1WithGroupsJson;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationAccessInterceptorRule;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationStatusInterceptorRule;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;

@Import({
    ApplicationAccessInterceptorRule.class,
    ApplicationStatusInterceptorRule.class,
    ApplicationAccessService.class
})
public abstract class AbstractApplicationControllerTest extends AbstractControllerTest {

  @Autowired
  protected ApplicationHandlerInterceptor applicationHandlerInterceptor;

  @Autowired
  protected ApplicationAccessInterceptorRule applicationAccessInterceptorRule;

  @Autowired
  protected ApplicationStatusInterceptorRule applicationStatusInterceptorRule;

  @Autowired
  protected ApplicationAccessService applicationAccessService;

  @MockBean
  protected OrganisationUnitService organisationUnitService;

  @MockBean
  protected TeamService teamService;

  @MockBean
  protected PermissionService permissionService;

  protected ServiceUserDetail user;

  @BeforeEach
  void setupAbstractApplicationControllerTest(TestInfo testInfo) {
    doCallRealMethod().when(userDetailService).getUserDetail();
    user = ServiceUserDetailTestUtil.Builder().build();

    var securityTestAnnotation = testInfo
        .getTestMethod().map(method -> method.getAnnotation(SecurityTest.class));

    // if the test doesn't have the @SecurityTest annotation ensure the security passes
    if (securityTestAnnotation.isEmpty()) {
      setupWhenUserHasApplicationAccessPermission();
    }
  }

  void setupWhenUserHasApplicationAccessPermission() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    var team = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team));
    when(permissionService.hasPermissionForTeam(any(), any(), any()))
        .thenReturn(true);
  }
}
