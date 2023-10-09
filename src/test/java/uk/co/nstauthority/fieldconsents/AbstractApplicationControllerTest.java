package uk.co.nstauthority.fieldconsents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ActionEndPointInterceptorRule;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationAccessInterceptorRule;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationStatusInterceptorRule;

@Import({
    ApplicationAccessInterceptorRule.class,
    ApplicationStatusInterceptorRule.class,
    ActionEndPointInterceptorRule.class
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
    when(applicationAccessService.hasApplicationPermission(any(), any(), any()))
        .thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(any(), any(), any(), any()))
        .thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(any(), any(), any(), any(), any()))
        .thenReturn(true);
  }

  void setupWhenUserCanCallAllActionEndPoints() {
    when(caseProcessingActionService.getUserActionItems(any(), any()))
        .thenReturn(List.of(
            CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP,
            CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP,
            CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP,
            CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP,
            CaseProcessingActionItem.CHANGE_ACE_STATUS,
            CaseProcessingActionItem.TECHNICAL_REVIEWS,
            CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST,
            CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP,
            CaseProcessingActionItem.OPERATOR_UPDATE_APPLICATION,
            CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST,
            CaseProcessingActionItem.CONSULTATION_REQUEST,
            CaseProcessingActionItem.CONSULTATION_RESPONSE
        ));
  }
}
