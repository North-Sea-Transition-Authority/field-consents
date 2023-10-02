package uk.co.nstauthority.fieldconsents.authorisation.rules;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;
import uk.co.nstauthority.fieldconsents.logging.LoggerUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

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

    var requiredPermissionsCsv = Arrays.stream(requiredPermissions)
        .map(RolePermission::name)
        .collect(Collectors.joining(", "));

    var errorMessage = "User %s is missing required application permissions %s to access application version %s."
        .formatted(
            user.wuaId(),
            requiredPermissionsCsv,
            applicationVersion.getId()
        );

    LoggerUtil.warn(errorMessage);
    return SecurityRuleResult.checkFailedWithStatus(HttpStatus.FORBIDDEN);
  }
}
