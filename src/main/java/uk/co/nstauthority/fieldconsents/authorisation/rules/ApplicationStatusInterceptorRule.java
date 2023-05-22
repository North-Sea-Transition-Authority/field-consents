package uk.co.nstauthority.fieldconsents.authorisation.rules;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;

@Component
@Order(1)
public class ApplicationStatusInterceptorRule implements ApplicationInterceptorSecurityRule {

  @Override
  public Class<? extends Annotation> supports() {
    return HasApplicationStatus.class;
  }

  @Override
  public SecurityRuleResult check(Object annotation,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServiceUserDetail user,
                                  ApplicationVersion applicationVersion) {
    var expectedStatuses = ((HasApplicationStatus) annotation).statuses();
    var expectedStatusList = CollectionUtils.arrayToList(expectedStatuses);
    var applicationVersionStatus = applicationVersion.getStatus();

    if (expectedStatusList.contains(applicationVersionStatus)) {
      return SecurityRuleResult.continueAsNormal();
    }

    var errorMessage = "User %s attempted to access application version %s with status %s. Expected status(s) %s"
        .formatted(
            user.wuaId(),
            applicationVersion.getId(),
            applicationVersionStatus,
            Arrays.stream(expectedStatuses)
                .map(ApplicationVersionStatus::name)
                .collect(Collectors.joining(","))
        );

    return SecurityRuleResult.checkFailedWithStatusAndMessage(HttpStatus.FORBIDDEN, errorMessage);
  }
}
