package uk.co.nstauthority.fieldconsents.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.logging.LoggerUtil;
import uk.co.nstauthority.fieldconsents.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Component
public class HasAssetPermissionInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(HasAssetPermission.class);
  private static final String SEARCH_FIELD_PURPOSE = "Search field for asset permission";
  private static final String SEARCH_TERMINAL_PURPOSE = "Search terminal for asset permission";

  private final FieldService fieldService;
  private final TerminalService terminalService;
  private final UserDetailService userDetailService;
  private final AssetAccessService assetAccessService;

  @Autowired
  public HasAssetPermissionInterceptor(FieldService fieldService,
                                       TerminalService terminalService,
                                       UserDetailService userDetailService,
                                       AssetAccessService assetAccessService) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.userDetailService = userDetailService;
    this.assetAccessService = assetAccessService;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {

      var requiredPermissions = ((HasAssetPermission) getAnnotation(handlerMethod, HasAssetPermission.class)).permissions();
      var pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      var fieldId = pathVariables.get("fieldId");
      var terminalId = pathVariables.get("terminalId");
      var user = userDetailService.getUserDetail();

      if (fieldId == null && terminalId == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Received request with no fieldId or terminalId present");
      }

      var userHasPermissions = false;
      if (fieldId != null) {
        // check for permissions on field with Operator for logged-in user
        var fieldWithOperatorJson = fieldService.getFieldWithOperator(Integer.parseInt(fieldId), SEARCH_FIELD_PURPOSE);
        userHasPermissions = assetAccessService.hasAssetPermission(user, fieldWithOperatorJson, requiredPermissions);
      }

      if (terminalId != null) {
        // check for permissions on terminal with Operator for logged-in user
        var terminalWithOperatorJson = terminalService
            .getTerminalWithOperator(Integer.parseInt(terminalId), SEARCH_TERMINAL_PURPOSE);
        userHasPermissions = assetAccessService.hasAssetPermission(user, terminalWithOperatorJson, requiredPermissions);
      }

      if (!userHasPermissions) {
        var requiredPermissionsCsv = Arrays.stream(requiredPermissions)
            .map(RolePermission::name)
            .collect(Collectors.joining(", "));

        var errorMessage = "User with ID %s doesn't have the required permissions %s for asset with id %s"
            .formatted(user.wuaId(), requiredPermissionsCsv, fieldId != null ? fieldId : terminalId);

        LoggerUtil.warn(errorMessage);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
      }
    }

    return true;
  }
}
