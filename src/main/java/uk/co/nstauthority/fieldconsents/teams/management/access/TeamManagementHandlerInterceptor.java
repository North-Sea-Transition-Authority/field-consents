package uk.co.nstauthority.fieldconsents.teams.management.access;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementService;

@Component
public class TeamManagementHandlerInterceptor implements HandlerInterceptor {

  private final TeamManagementService teamManagementService;
  private final UserDetailService userDetailService;
  private final TeamQueryService teamQueryService;

  TeamManagementHandlerInterceptor(
      TeamManagementService teamManagementService,
      UserDetailService userDetailService,
      TeamQueryService teamQueryService
  ) {
    this.teamManagementService = teamManagementService;
    this.userDetailService = userDetailService;
    this.teamQueryService = teamQueryService;
  }

  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler
  ) {
    if (handler instanceof ResourceHttpRequestHandler) {
      return true;
    }

    if (handler instanceof HandlerMethod handlerMethod) {
      var userDetail = userDetailService.getUserDetail();

      if (hasAnnotation(handlerMethod, InvokingUserCanManageTeam.class)) {
        return handleInvokingUserCanManageTeamCheck(request, userDetail);
      }

      if (hasAnnotation(handlerMethod, InvokingUserHasStaticRole.class)) {
        var annotation = getAnnotation(handlerMethod, InvokingUserHasStaticRole.class);
        return handleInvokingUserHasRoleCheck(userDetail, annotation.teamType(), annotation.role());
      }

      if (hasAnnotation(handlerMethod, InvokingUserHasAnyStaticRole.class)) {
        var annotation = getAnnotation(handlerMethod, InvokingUserHasAnyStaticRole.class);
        var roles = Arrays.stream(annotation.roles()).collect(Collectors.toSet());

        return handleInvokingUserHasAnyRoleCheck(userDetail, annotation.teamType(), roles);
      }

      if (hasAnnotation(handlerMethod, InvokingUserCanViewTeam.class)) {
        return handleInvokingUserCanViewTeam(request, userDetail);
      }

      return true;
    }

    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Unexpected handler class %s".formatted(handler.getClass())
    );
  }

  private boolean handleInvokingUserCanManageTeamCheck(HttpServletRequest request, ServiceUserDetail userDetail) {
    var team = getTeamFromRequest(request);

    var isScoped = team.getTeamType().isScoped();
    boolean canManageTeam;
    if (isScoped) {
      canManageTeam = teamManagementService.getScopedTeamsOfTypeUserCanManage(team.getTeamType(), userDetail).contains(team);
    } else {
      canManageTeam = teamManagementService.getStaticTeamOfTypeUserCanManage(team.getTeamType(), userDetail)
          .map(t -> t.equals(team))
          .isPresent();
    }

    if (!canManageTeam) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s does not have manage team role for teamId %s".formatted(userDetail.wuaId(), team.getId())
      );
    }

    return true;
  }

  private boolean handleInvokingUserHasRoleCheck(ServiceUserDetail userDetail, TeamType teamType, Role role) {
    if (!teamQueryService.userHasStaticRole(userDetail, teamType, role)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s does not have static role %s for teamType %s".formatted(userDetail.wuaId(), role, teamType)
      );
    }

    return true;
  }

  private boolean handleInvokingUserHasAnyRoleCheck(ServiceUserDetail userDetail, TeamType teamType, Collection<Role> roles) {
    if (!teamQueryService.userHasAtLeastOneStaticRole(userDetail, teamType, roles)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s does not have static roles %s for teamType %s".formatted(userDetail.wuaId(), roles, teamType)
      );
    }

    return true;
  }

  private boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation) != null
        || AnnotationUtils.findAnnotation(handlerMethod.getMethod().getDeclaringClass(), annotation) != null;
  }

  private  <T extends Annotation> T getAnnotation(HandlerMethod handlerMethod, Class<T> annotation) {
    return Objects.requireNonNullElse(
        AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation),
        AnnotationUtils.findAnnotation(handlerMethod.getMethod().getDeclaringClass(), annotation)
    );
  }

  private boolean handleInvokingUserCanViewTeam(HttpServletRequest request, ServiceUserDetail userDetail) {
    var team = getTeamFromRequest(request);

    if (teamManagementService.isMemberOfTeam(team, userDetail) || (TeamType.INDUSTRY.equals(team.getTeamType())
        && teamManagementService.userCanManageAnyOrganisationTeam(userDetail))) {
      return true;
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "wuaId %s is not a member of team %s"
        .formatted(userDetail.wuaId(), team.getId()));
  }

  @SuppressWarnings("unchecked")
  private Team getTeamFromRequest(HttpServletRequest request) {
    var pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    var teamIdString = pathVariables.get("teamId");
    if (teamIdString == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamId path variable not found in request");
    }

    UUID teamId;

    try {
      teamId = UUID.fromString(teamIdString);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UUID parse error", e);
    }

    return teamManagementService.getTeam(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "teamId %s not found".formatted(teamId)));
  }
}