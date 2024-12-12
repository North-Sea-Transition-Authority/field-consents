package uk.co.nstauthority.fieldconsents.email;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class FieldConsentsEmailRecipientService {

  private final TeamQueryService teamQueryService;

  FieldConsentsEmailRecipientService(TeamQueryService teamQueryService) {
    this.teamQueryService = teamQueryService;
  }

  public Set<FieldConsentsEmailRecipient> getDistinctEmailRecipientsWithRoles(
      OrganisationUnitWithGroupsJson organisationUnitWithGroupsJson,
      Set<Role> roles
  ) {
    var teamScopeIds = organisationUnitWithGroupsJson.organisationGroups()
        .stream()
        .map(OrganisationGroupDto::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    var teamRoles = teamQueryService.getTeamRoles(
        TeamType.INDUSTRY,
        TeamScopeReference.ORGANISATION_GROUP_ID,
        teamScopeIds
    )
        .stream()
        .filter(teamRole -> roles.contains(teamRole.getRole()))
        .collect(Collectors.toSet());

    return teamQueryService.getTeamMemberViews(teamRoles)
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());
  }
}
