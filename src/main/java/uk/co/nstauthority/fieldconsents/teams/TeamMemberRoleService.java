package uk.co.nstauthority.fieldconsents.teams;

import java.util.ArrayList;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;

@Service
public class TeamMemberRoleService {

  static final String RESOURCE_TYPE_NAME = "FCS_ACCESS_TEAM";
  private static final Logger LOGGER = LoggerFactory.getLogger(TeamMemberRoleService.class);

  private final TeamMemberRoleRepository teamMemberRoleRepository;

  private final UserDetailService userDetailService;

  private final EnergyPortalAccessService energyPortalAccessService;

  private final TeamMemberRoleEmailService teamMemberRoleEmailService;

  @Autowired
  public TeamMemberRoleService(TeamMemberRoleRepository teamMemberRoleRepository,
                               UserDetailService userDetailService,
                               EnergyPortalAccessService energyPortalAccessService,
                               TeamMemberRoleEmailService teamMemberRoleEmailService) {
    this.teamMemberRoleRepository = teamMemberRoleRepository;
    this.userDetailService = userDetailService;
    this.energyPortalAccessService = energyPortalAccessService;
    this.teamMemberRoleEmailService = teamMemberRoleEmailService;
  }

  @Transactional
  public void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<String> roles) {
    var userToAddWuaId = userToAdd.webUserAccountId();
    var isNewUser = teamMemberRoleRepository.findAllByWuaId(userToAddWuaId).isEmpty();
    var isNewUserToTeam = isNewUser || !teamMemberRoleRepository.existsByWuaIdAndTeam_Id(userToAddWuaId, team.getId());

    updateUserTeamRoles(team, new WebUserAccountId(userToAddWuaId), roles);

    if (isNewUser) {
      energyPortalAccessService.addUserToAccessTeam(
          new ResourceType(RESOURCE_TYPE_NAME),
          new TargetWebUserAccountId(new WebUserAccountId(userToAddWuaId).id()),
          new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
      );
    }

    if (isNewUserToTeam) {
      try {
        teamMemberRoleEmailService.sendUserAddedToTeamEmail(
            FieldConsentsEmailRecipient.from(userToAdd),
            team
        );
      } catch (Exception exception) {
        LOGGER.error("An attempt to send a notification to user with wuaId {} for being newly added to a team failed. " +
                "Note: this hasn't prevented the user being added to the team.",
            userToAddWuaId, exception);
      }
    }
  }

  @Transactional
  public void updateUserTeamRoles(Team team, WebUserAccountId wuaId, Set<String> roles) {
    // Clear user's existing roles
    teamMemberRoleRepository.deleteAllByTeamAndWuaId(team, wuaId.id());

    var teamMemberRoles = new ArrayList<TeamMemberRole>();

    // Create new roles based on role selection
    roles.forEach(role -> {
      var teamMemberRole = new TeamMemberRole();
      teamMemberRole.setTeam(team);
      teamMemberRole.setWuaId(wuaId.id());
      teamMemberRole.setRole(role);
      teamMemberRoles.add(teamMemberRole);
    });

    teamMemberRoleRepository.saveAll(teamMemberRoles);
  }

  @Transactional
  public void removeMemberFromTeam(Team team, TeamMember teamMember) {
    teamMemberRoleRepository.deleteAllByTeamAndWuaId(team, teamMember.wuaId().id());

    var isUserRemovedFromAllTeams = teamMemberRoleRepository.findAllByWuaId(teamMember.wuaId().id()).isEmpty();

    if (isUserRemovedFromAllTeams) {
      energyPortalAccessService.removeUserFromAccessTeam(
          new ResourceType(RESOURCE_TYPE_NAME),
          new TargetWebUserAccountId(teamMember.wuaId().id()),
          new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
      );
    }
  }

}
