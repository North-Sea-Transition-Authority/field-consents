package uk.co.nstauthority.fieldconsents.teams.management.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementController;

public record TeamMemberView(
    Long wuaId,
    String forename,
    String surname,
    String email,
    String telNo,
    UUID teamId,
    List<Role> roles
) {

  public String getDisplayName() {
    return Stream.of(forename, surname)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(" "));
  }

  public String getEditUrl() {
    return ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(teamId, wuaId, null));
  }

  public String getRemoveUrl() {
    return ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(teamId, wuaId));
  }

  public static TeamMemberView from(EnergyPortalUserDto energyPortalUserDto, UUID teamId, Collection<Role> roles) {
    return new TeamMemberView(
        energyPortalUserDto.webUserAccountId(),
        energyPortalUserDto.forename(),
        energyPortalUserDto.surname(),
        energyPortalUserDto.emailAddress(),
        energyPortalUserDto.telephoneNumber(),
        teamId,
        roles.stream().distinct().sorted().toList()
    );
  }
}