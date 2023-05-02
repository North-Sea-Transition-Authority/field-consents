package uk.co.nstauthority.fieldconsents.authorisation.rules;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;
import uk.co.nstauthority.fieldconsents.logging.LoggerUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Component
public class ApplicationAccessInterceptorRule implements ApplicationInterceptorSecurityRule {

  private final OrganisationUnitService organisationUnitService;

  private final TeamService teamService;

  private final PermissionService permissionService;

  @Autowired
  public ApplicationAccessInterceptorRule(OrganisationUnitService organisationUnitService, TeamService teamService,
                                          PermissionService permissionService) {
    this.organisationUnitService = organisationUnitService;
    this.teamService = teamService;
    this.permissionService = permissionService;
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
    var applicationId = applicationVersion.getApplication().getId();
    var organisationUnitWithGroups = organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersion.getPrimaryOperatorOuId(),
        "Lookup organisation unit with groups for application security check"
    );

    if (organisationUnitWithGroups.organisationGroups().isEmpty()) {
      var errorMessage = "No organisation groups found for organisation unit id %s. Application id %s"
          .formatted(applicationVersion.getPrimaryOperatorOuId(), applicationId);
      return SecurityRuleResult.checkFailedWithStatusAndMessage(HttpStatus.FORBIDDEN, errorMessage);
    }

    // loop over the organisation groups for the operator
    // (there is typically 1 but can be more, so we have to cater for this here)
    for (var orgGroup: organisationUnitWithGroups.organisationGroups()) {
      var teamOptional = teamService.getTeamByOrganisationGroupId(orgGroup.getOrganisationGroupId());

      if (teamOptional.isEmpty()) {
        LoggerUtil.warn("Team not found for organisation group id %s. Application id %s"
            .formatted(orgGroup.getOrganisationGroupId(), applicationId));
      } else {
        var team = teamOptional.get();
        var hasPermissionForTeam = permissionService.hasPermissionForTeam(team.toTeamId(), user, Set.of(requiredPermissions));
        if (hasPermissionForTeam) {
          return SecurityRuleResult.continueAsNormal(); // return as soon as we find a team the user has permissions in
        } else {
          LoggerUtil.warn(
              "User %s attempted to access application %s with responsible organisation group id %s. "
                  .formatted(user.wuaId(), applicationId, orgGroup.getOrganisationGroupId()) +
              "User is expected to have at least one of the following permission(s): %s"
                  .formatted(Arrays.stream(requiredPermissions).map(RolePermission::name)
                      .collect(Collectors.joining(",")))
          );
        }
      }
    }

    return SecurityRuleResult.checkFailedWithStatus(HttpStatus.FORBIDDEN);
  }
}