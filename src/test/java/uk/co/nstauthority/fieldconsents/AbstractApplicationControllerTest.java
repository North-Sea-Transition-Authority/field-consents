package uk.co.nstauthority.fieldconsents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTabConverter;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ActionEndPointInterceptorRule;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationAccessInterceptorRule;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationStatusInterceptorRule;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Import({
    ApplicationAccessInterceptorRule.class,
    ApplicationStatusInterceptorRule.class,
    ActionEndPointInterceptorRule.class,
    CaseProcessingTabConverter.class
})
public abstract class AbstractApplicationControllerTest extends AbstractControllerTest {

  @Autowired
  protected ApplicationHandlerInterceptor applicationHandlerInterceptor;

  @Autowired
  protected ApplicationAccessInterceptorRule applicationAccessInterceptorRule;

  @Autowired
  protected ApplicationStatusInterceptorRule applicationStatusInterceptorRule;

  @Autowired
  protected ActionEndPointInterceptorRule actionEndPointInterceptorRule;

  @MockBean
  protected ApplicationAccessService applicationAccessService;

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
    when(applicationAccessService.hasApplicationPermission(any(), any(), any(RolePermission[].class)))
        .thenReturn(true);
  }

  void setupWhenUserCanCallAllActionEndPoints() {
    when(caseProcessingActionService.getUserActionItems(any(), any())).thenReturn(Arrays.asList(CaseProcessingActionItem.values()));
  }
}
