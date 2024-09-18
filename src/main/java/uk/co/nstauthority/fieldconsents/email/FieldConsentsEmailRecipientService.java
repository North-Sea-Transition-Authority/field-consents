package uk.co.nstauthority.fieldconsents.email;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamService;

@Service
public class FieldConsentsEmailRecipientService {

  private final TeamMemberViewService teamMemberViewService;
  private final IndustryTeamService industryTeamService;

  public FieldConsentsEmailRecipientService(
      TeamMemberViewService teamMemberViewService,
      IndustryTeamService industryTeamService
  ) {
    this.teamMemberViewService = teamMemberViewService;
    this.industryTeamService = industryTeamService;
  }

  public Set<FieldConsentsEmailRecipient> getDistinctEmailRecipientsWithRoles(
      OrganisationUnitWithGroupsJson organisationUnitWithGroupsJson,
      Set<TeamRole> teamRoles
  ) {
    return organisationUnitWithGroupsJson.organisationGroups().stream()
        .flatMap(organisationGroupDto ->
            industryTeamService.getTeamByOrganisationGroupId(organisationGroupDto.getOrganisationGroupId()).stream())
        .flatMap(team ->
            teamMemberViewService
                .getTeamMemberViewsWithRolesForTeam(team, teamRoles).stream())
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());
  }
}
