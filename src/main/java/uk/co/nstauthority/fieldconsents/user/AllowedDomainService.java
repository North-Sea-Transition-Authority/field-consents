package uk.co.nstauthority.fieldconsents.user;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class AllowedDomainService {

  private final OrganisationGroupQueryService organisationGroupQueryService;

  AllowedDomainService(OrganisationGroupQueryService organisationGroupQueryService) {
    this.organisationGroupQueryService = organisationGroupQueryService;
  }

  public boolean isAllowedDomain(String userEmail, Team team) {
    Optional<OrganisationGroupDto> group;
    switch (team.getTeamType()) {
      case TeamType.INDUSTRY -> group = organisationGroupQueryService
          .getOrganisationGroupById(Integer.parseInt(team.getScopeId()));
      case TeamType.REGULATOR -> group = organisationGroupQueryService.getRegulatorOrganisationGroup();
      case TeamType.CONSULTEE ->  group = organisationGroupQueryService.getConsulteeOrganisationGroup();
      default -> throw new IllegalStateException("Unexpected value: " + team.getTeamType());
    }

    List<String> emailDomains = List.of();
    if (group.isPresent()) {
      emailDomains = group.get().getEmailDomains();
    }

    return emailDomains.contains(userEmail.split("@")[1]);
  }
}
