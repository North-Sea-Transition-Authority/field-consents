package uk.co.nstauthority.fieldconsents.authorisation.rules;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;

@Component
@Order(2)
public class ApplicationAccessInterceptorRule implements ApplicationInterceptorSecurityRule {

  private final ApplicationAccessService applicationAccessService;

  @Autowired
  public ApplicationAccessInterceptorRule(ApplicationAccessService applicationAccessService) {
    this.applicationAccessService = applicationAccessService;
  }

  @Override
  public Class<? extends Annotation> supports() {
    return HasApplicationPermission.class;
  }

  @Override
  public SecurityRuleResult check(Object annotation,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServiceUserDetail user,
                                  ApplicationVersion applicationVersion) {
    var requiredPermissions = ((HasApplicationPermission) annotation).permissions();

    var hasApplicationPermission =
        applicationAccessService.hasApplicationPermission(user, applicationVersion, requiredPermissions);

    if (hasApplicationPermission) {
      return SecurityRuleResult.continueAsNormal();
    }

    return SecurityRuleResult.checkFailedWithStatus(HttpStatus.FORBIDDEN);
  }
}
