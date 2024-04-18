package uk.co.nstauthority.fieldconsents.authorisation.rules;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;

@Component
@Order(3)
public class ActionEndPointInterceptorRule implements ApplicationInterceptorSecurityRule {

  private final CaseProcessingActionService caseProcessingActionService;

  @Autowired
  public ActionEndPointInterceptorRule(CaseProcessingActionService caseProcessingActionService) {
    this.caseProcessingActionService = caseProcessingActionService;
  }

  @Override
  public Class<? extends Annotation> supports() {
    return ActionEndPoint.class;
  }

  @Override
  public SecurityRuleResult check(Object annotation,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServiceUserDetail user,
                                  ApplicationVersion applicationVersion) {
    var expectedActionItems = ((ActionEndPoint) annotation).value();
    var userActionItems = caseProcessingActionService.getUserActionItems(applicationVersion, user);

    if (CollectionUtils.containsAny(userActionItems, Set.of(expectedActionItems))) {
      return SecurityRuleResult.continueAsNormal();
    }

    var errorMessage =
        "User %s attempted to use action item(s) %s on application version %s"
        .formatted(
            user.wuaId(),
            Arrays.stream(expectedActionItems)
                .map(CaseProcessingActionItem::name)
                .collect(Collectors.joining(",")),
            applicationVersion.getId()
        );

    return SecurityRuleResult.checkFailedWithStatusAndMessage(HttpStatus.FORBIDDEN, errorMessage);
  }
}
