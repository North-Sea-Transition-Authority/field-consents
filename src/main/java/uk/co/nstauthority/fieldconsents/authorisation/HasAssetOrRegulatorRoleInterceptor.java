package uk.co.nstauthority.fieldconsents.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;

@Component
public class HasAssetOrRegulatorRoleInterceptor implements HandlerInterceptor {

  private static final String EPA_ASSET_LOOKUP_REQUEST_PURPOSE = "Looking up asset in handler interceptor";

  private final FieldConsentsAccessService fieldConsentsAccessService;
  private final UserDetailService userDetailService;
  private final FieldService fieldService;
  private final TerminalService terminalService;

  HasAssetOrRegulatorRoleInterceptor(
      FieldConsentsAccessService fieldConsentsAccessService,
      UserDetailService userDetailService,
      FieldService fieldService,
      TerminalService terminalService
  ) {
    this.fieldConsentsAccessService = fieldConsentsAccessService;
    this.userDetailService = userDetailService;
    this.fieldService = fieldService;
    this.terminalService = terminalService;
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
      var hasAnyAssetScopedOrRegulatorRole =
          HandlerInterceptorUtil.findAnnotation(handlerMethod, HasAssetOrRegulatorRole.class).orElse(null);

      if (hasAnyAssetScopedOrRegulatorRole == null) {
        return true;
      }

      var regulatorRoles = Arrays.stream(hasAnyAssetScopedOrRegulatorRole.regulatorRoles()).collect(Collectors.toSet());
      var industryRoles = Arrays.stream(hasAnyAssetScopedOrRegulatorRole.industryRoles()).collect(Collectors.toSet());

      if (regulatorRoles.isEmpty() && industryRoles.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No roles provided to security annotation");
      }

      var userDetail = userDetailService.getUserDetail();

      if (fieldConsentsAccessService.userHasAnyRegulatorRole(userDetail, regulatorRoles)) {
        return true;
      }

      var assetWithOperatorJson = findAssetWithOperatorJson(request)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

      if (fieldConsentsAccessService.userHasAnyIndustryRole(userDetail, assetWithOperatorJson, industryRoles)) {
        return true;
      }

      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "User does not have any of the required roles to access asset %s".formatted(assetWithOperatorJson.getAssetKey())
      );
    }

    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Unexpected handler class %s".formatted(handler.getClass())
    );
  }

  @SuppressWarnings("unchecked")
  private Optional<? extends AssetWithOperatorJson> findAssetWithOperatorJson(HttpServletRequest request) {
    var pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    var fieldId = pathVariables.get("fieldId");
    if (fieldId != null) {
      return parseAssetId(fieldId).flatMap(this::findFieldWithOperatorJson);
    }

    var terminalId = pathVariables.get("terminalId");
    if (terminalId != null) {
      return parseAssetId(terminalId).flatMap(this::findTerminalWithOperatorJson);
    }

    return Optional.empty();
  }

  private Optional<Integer> parseAssetId(String assetId) {
    try {
      return Optional.of(Integer.parseInt(assetId));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  private Optional<FieldWithOperatorJson> findFieldWithOperatorJson(Integer fieldId) {
    return fieldService.findFieldWithOperator(fieldId, EPA_ASSET_LOOKUP_REQUEST_PURPOSE);
  }

  private Optional<TerminalWithOperatorJson> findTerminalWithOperatorJson(Integer terminalId) {
    return terminalService.findTerminalWithOperator(terminalId, EPA_ASSET_LOOKUP_REQUEST_PURPOSE);
  }

}
