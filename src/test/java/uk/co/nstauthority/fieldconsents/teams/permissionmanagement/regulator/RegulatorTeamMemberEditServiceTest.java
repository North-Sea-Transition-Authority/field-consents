package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator;

import static org.mockito.Mockito.verify;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMember;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;

@ExtendWith(MockitoExtension.class)
class RegulatorTeamMemberEditServiceTest {

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  private RegulatorTeamMemberEditService regulatorTeamMemberEditService;

  @Test
  void updateRoles() {
    var team = new Team(randomInteger());
    var teamView = TeamTestUtil.createTeamView(team);
    var teamMember = new TeamMember(new WebUserAccountId(1L), teamView, Set.of());
    var newRoles = Set.of(RegulatorTeamRole.ACCESS_MANAGER.name());

    regulatorTeamMemberEditService.updateRoles(team, teamMember, newRoles);

    verify(teamMemberRoleService).updateUserTeamRoles(team, teamMember.wuaId(), newRoles);
  }
}