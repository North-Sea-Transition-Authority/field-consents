package uk.co.nstauthority.fieldconsents.authorisation;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.logging.LoggerUtil;
import uk.co.nstauthority.fieldconsents.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Component
public class IsMemberOfTeamTypeInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      IsMemberOfTeamType.class
  );

  private final TeamService teamService;
  private final UserDetailService userDetailService;

  @Autowired
  IsMemberOfTeamTypeInterceptor(TeamService teamService,
                                       UserDetailService userDetailService) {
    this.teamService = teamService;
    this.userDetailService = userDetailService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var teamType = ((IsMemberOfTeamType) getAnnotation(handlerMethod, IsMemberOfTeamType.class)).teamType();

      var user = userDetailService.getUserDetail();

      var isMemberOfTeamType = switch (teamType) {
        case REGULATOR -> teamService.isRegulatorUser(user);
        case INDUSTRY -> teamService.isIndustryUser(user);
        case OPRED -> teamService.isConsulteeUser(user);
      };

      if (!isMemberOfTeamType) {
        var errorMessage = "User [%s] is not in the team type [%s] required to access case processing".formatted(
            user.wuaId(),
            teamType
        );
        LoggerUtil.warn(errorMessage);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
      }
    }

    return true;
  }
}
