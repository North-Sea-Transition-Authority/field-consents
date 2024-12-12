package uk.co.nstauthority.fieldconsents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.context.annotation.Import;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTabConverter;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ActionEndPointInterceptorRule;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationRoleAccessInterceptorRule;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationStatusInterceptorRule;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Import({
    ApplicationStatusInterceptorRule.class,
    ApplicationRoleAccessInterceptorRule.class,
    ActionEndPointInterceptorRule.class,
    CaseProcessingTabConverter.class,
})
public abstract class AbstractApplicationControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setupAbstractApplicationControllerTest(TestInfo testInfo) {
    var securityTestAnnotation = testInfo
        .getTestMethod().map(method -> method.getAnnotation(SecurityTest.class));
    var parameterizedSecurityTestAnnotation = testInfo
        .getTestMethod().map(method -> method.getAnnotation(ParameterizedSecurityTest.class));

    // if the test doesn't have the @SecurityTest annotation ensure the security passes
    if (securityTestAnnotation.isEmpty() && parameterizedSecurityTestAnnotation.isEmpty()) {
      setupWhenUserHasApplicationAccessPermission();
      setupWhenUserCanCallAllActionEndPoints();
    }
  }

  void setupWhenUserHasApplicationAccessPermission() {
    when(fieldConsentsAccessService.userHasAnyRegulatorRole(eq(user), any())).thenReturn(true);
    when(fieldConsentsAccessService.userHasAnyConsulteeRole(eq(user), any(), any())).thenReturn(true);
    when(fieldConsentsAccessService.userHasAnyIndustryRole(eq(user), any(ApplicationVersion.class), any())).thenReturn(true);

    when(teamQueryService.userHasStaticRole(eq(user), any(), any())).thenReturn(true);
    when(teamQueryService.userHasAtLeastOneStaticRole(eq(user), any(), any())).thenReturn(true);

    var allTeamRoles =  EnumSet.allOf(Role.class)
        .stream()
        .flatMap(role -> EnumSet.allOf(TeamType.class)
            .stream()
            .map(teamType -> TeamRoleTestUtil.newBuilder()
                .withRole(role)
                .withTeam(TeamTestUtil.newBuilder()
                    .withTeamType(teamType)
                    .build())
                .build())
        )
        .toList();
    when(teamQueryService.getTeamRoles(user)).thenReturn(allTeamRoles);

    when(fieldConsentsAccessService.getApplicationRolesForUser(any(), eq(user))).thenReturn(EnumSet.allOf(Role.class));
  }

  void setupWhenUserCanCallAllActionEndPoints() {
    when(caseProcessingActionService.userHasAnyAction(any(), any(), any(CaseProcessingActionItem[].class)))
        .thenReturn(true);
  }
}
