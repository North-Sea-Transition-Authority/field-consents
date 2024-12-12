package uk.co.nstauthority.fieldconsents.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.management.ScopedTeamManagementController;

public enum TeamType {

  REGULATOR(
      "Regulator",
      "regulator",
      false,
      EnumSet.of(
          Role.ACCESS_MANAGER,
          Role.INDUSTRY_ACCESS_MANAGER,
          Role.DOCUMENT_TEMPLATE_MANAGER,
          Role.CASE_OFFICER,
          Role.CASE_MANAGER,
          Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
          Role.TECHNICAL_REVIEWER,
          Role.VIEWER
      ),
      null
  ),
  CONSULTEE(
      "Consultee",
      "consultee",
      false,
      EnumSet.of(
          Role.ACCESS_MANAGER,
          Role.ALLOCATOR,
          Role.RESPONDER,
          Role.VIEWER
      ),
      null
  ),
  INDUSTRY(
      "Industry",
      "industry",
      true,
      EnumSet.of(
          Role.ACCESS_MANAGER,
          Role.VIEWER,
          Role.EDITOR,
          Role.SUBMITTER,
          Role.CREATOR,
          Role.FINANCE_ADMINISTRATOR,
          Role.CONSENT_RECIPIENT
      ),
  //CHECKSTYLE:OFF
      () -> ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrgTeam(null))
  //CHECKSTYLE:ON
  );

  private final String displayName;
  private final String urlSlug;
  private final boolean isScoped;
  private final Set<Role> allowedRoles;

  /*
    Springs classloader evaluates this enum on application startup because it's used in annotations which exist on
    controller classes. In order for this field to successfully resolve the spring context must be present, along with
    all its controllers, which isn't guaranteed to exist before this class is loaded. By moving any method calls that
    depend on the Spring context into a deferred function (in this case a supplier), Spring will not try to evaluate
    the value in context start up.

    Without this, any annotation that depends on this is enum is ignored.
   */
  private final Supplier<String> createNewInstanceRoute;

  TeamType(
      String displayName,
      String urlSlug,
      boolean isScoped,
      Set<Role> allowedRoles,
      Supplier<String> createNewInstanceRoute
  ) {
    this.displayName = displayName;
    this.urlSlug = urlSlug;
    this.isScoped = isScoped;
    this.allowedRoles = allowedRoles;
    this.createNewInstanceRoute = createNewInstanceRoute;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrlSlug() {
    return urlSlug;
  }

  public boolean isScoped() {
    return isScoped;
  }

  public Set<Role> getAllowedRoles() {
    return allowedRoles;
  }

  public Supplier<String> getCreateNewInstanceRoute() {
    return createNewInstanceRoute;
  }

  public static Optional<TeamType> fromUrlSlug(String urlSlug) {
    return Arrays.stream(values())
        .filter(teamType -> teamType.urlSlug.equals(urlSlug))
        .findFirst();
  }

}
