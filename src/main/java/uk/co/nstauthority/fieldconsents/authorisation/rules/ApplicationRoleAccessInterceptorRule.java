package uk.co.nstauthority.fieldconsents.authorisation.rules;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationOrRegulatorRole;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;

@Component
@Order(2)
public class ApplicationRoleAccessInterceptorRule implements ApplicationInterceptorSecurityRule {

  private final FieldConsentsAccessService fieldConsentsAccessService;

  ApplicationRoleAccessInterceptorRule(FieldConsentsAccessService fieldConsentsAccessService) {
    this.fieldConsentsAccessService = fieldConsentsAccessService;
  }

  @Override
  public Class<? extends Annotation> supports() {
    return HasApplicationOrRegulatorRole.class;
  }

  @Override
  public SecurityRuleResult check(
      Object annotation,
      HttpServletRequest request,
      HttpServletResponse response,
      ServiceUserDetail user,
      ApplicationVersion applicationVersion
  ) {
    var hasApplicationOrRegulatorRole = (HasApplicationOrRegulatorRole) annotation;

    var regulatorRoles = Arrays.stream(hasApplicationOrRegulatorRole.regulatorRoles()).collect(Collectors.toSet());
    var consulteeRoles = Arrays.stream(hasApplicationOrRegulatorRole.consulteeRoles()).collect(Collectors.toSet());
    var industryRoles = Arrays.stream(hasApplicationOrRegulatorRole.industryRoles()).collect(Collectors.toSet());

    if (regulatorRoles.isEmpty() && consulteeRoles.isEmpty() && industryRoles.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No roles provided to security annotation");
    }

    if (fieldConsentsAccessService.userHasAnyRegulatorRole(user, regulatorRoles)) {
      return SecurityRuleResult.continueAsNormal();
    }

    if (fieldConsentsAccessService.userHasAnyConsulteeRole(user, applicationVersion, consulteeRoles)) {
      return SecurityRuleResult.continueAsNormal();
    }

    if (fieldConsentsAccessService.userHasAnyIndustryRole(user, applicationVersion, industryRoles)) {
      return SecurityRuleResult.continueAsNormal();
    }

    return SecurityRuleResult.checkFailedWithStatusAndMessage(
        HttpStatus.FORBIDDEN,
        "User does not have one of the required roles to make this request"
    );
  }

}