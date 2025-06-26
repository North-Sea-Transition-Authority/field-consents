package uk.co.nstauthority.fieldconsents.authorisation.role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.HandlerInterceptorUtil;
import uk.co.nstauthority.fieldconsents.authorisation.Security;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserIsRegulatorCaseProcessor;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Component
public class StaticRoleHandlerInterceptor implements HandlerInterceptor {

  private final UserDetailService userDetailService;
  private final TeamQueryService teamQueryService;

  StaticRoleHandlerInterceptor(
      UserDetailService userDetailService,
      TeamQueryService teamQueryService
  ) {
    this.userDetailService = userDetailService;
    this.teamQueryService = teamQueryService;
  }

  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler
  ) {
    if (handler instanceof HandlerMethod handlerMethod) {
      if (HandlerInterceptorUtil.findAnnotation(handlerMethod, Security.class).isEmpty()) {
        // Don't call getUserDetail() if the endpoint is unauthenticated to allow for unauthenticated access.
        return true;
      }

      var userDetail = userDetailService.getUserDetail();

      HandlerInterceptorUtil.findAnnotation(handlerMethod, HasRegulatorRole.class)
          .map(HasRegulatorRole::value)
          .ifPresent(requiredRole -> hasStaticRole(userDetail, TeamType.REGULATOR, requiredRole));

      HandlerInterceptorUtil.findAnnotation(handlerMethod, HasAnyRegulatorRole.class)
          .map(HasAnyRegulatorRole::value)
          .map(Arrays::asList)
          .ifPresent(anyRequiredRoles -> hasAnyStaticRole(userDetail, TeamType.REGULATOR, anyRequiredRoles));

      HandlerInterceptorUtil.findAnnotation(handlerMethod, UserIsRegulatorCaseProcessor.class)
          .ifPresent(a -> hasAnyStaticRole(userDetail, TeamType.REGULATOR, RoleGroup.REGULATOR_CASE_PROCESSING_ROLES));

      HandlerInterceptorUtil.findAnnotation(handlerMethod, HasConsulteeRole.class)
          .map(HasConsulteeRole::value)
          .ifPresent(requiredRole -> hasStaticRole(userDetail, TeamType.CONSULTEE, requiredRole));

      return true;
    }

    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Unexpected handler class %s".formatted(handler.getClass())
    );
  }

  private void hasStaticRole(ServiceUserDetail userDetail, TeamType teamType, Role role) {
    if (!teamQueryService.userHasStaticRole(userDetail, teamType, role)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "User [%d] does not have role [%s] in team [%s] to make this request".formatted(userDetail.wuaId(), role, teamType)
      );
    }
  }

  private void hasAnyStaticRole(ServiceUserDetail userDetail, TeamType teamType, Collection<Role> anyRequiredRoles) {
    if (!teamQueryService.userHasAtLeastOneStaticRole(userDetail, teamType, new HashSet<>(anyRequiredRoles))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "User [%d] does not have any required roles in team [%s] to make this request".formatted(userDetail.wuaId(), teamType)
      );
    }
  }

}