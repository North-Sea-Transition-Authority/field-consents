package uk.co.nstauthority.fieldconsents.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Component
public class UserCanManageAssetsInterceptor implements HandlerInterceptor {

  private final UserDetailService userDetailService;
  private final TeamQueryService teamQueryService;

  UserCanManageAssetsInterceptor(
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
      if (HandlerInterceptorUtil.findAnnotation(handlerMethod, UserCanManageAssets.class).isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "%s must be annotated with %s".formatted(
                handlerMethod.getMethod().getDeclaringClass().getSimpleName(),
                UserCanManageAssets.class.getSimpleName()
            )
        );
      }

      var userDetail = userDetailService.getUserDetail();

      var rolesByTeamType = teamQueryService.getTeamRoles(userDetail)
          .stream()
          .collect(Collectors.groupingBy(
              teamRole -> teamRole.getTeam().getTeamType(),
              Collectors.mapping(TeamRole::getRole, Collectors.toSet())
          ));

      var regulatorRoles = rolesByTeamType.getOrDefault(TeamType.REGULATOR, Set.of());
      if (CollectionUtils.containsAny(regulatorRoles, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES)) {
        return true;
      }

      var industryRoles = rolesByTeamType.getOrDefault(TeamType.INDUSTRY, Set.of());
      if (CollectionUtils.containsAny(industryRoles, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES)) {
        return true;
      }

      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have the required role to manage assets");
    }

    return true;
  }

}
