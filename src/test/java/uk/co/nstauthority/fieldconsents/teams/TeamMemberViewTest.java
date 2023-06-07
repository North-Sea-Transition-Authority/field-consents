package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryEditMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryRemoveMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorEditMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorRemoveMemberController;

class TeamMemberViewTest {

  @Test
  void getDisplayName_whenTitle_thenTitleIncludedInDisplayName() {

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTitle("Mr")
        .withFirstName("Forename")
        .withLastName("Surname")
        .build();

    assertThat(teamMemberView.getDisplayName()).isEqualTo("Forename Surname");
  }

  @Test
  void getDisplayName_whenNoTitle_thenTitleIncludedInDisplayName() {

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTitle(null)
        .withFirstName("Forename")
        .withLastName("Surname")
        .build();

    assertThat(teamMemberView.getDisplayName()).isEqualTo("Forename Surname");
  }

  @Test
  void removeUrl_whenRegulatorTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.REGULATOR)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::removeUrl)
        .isEqualTo(ReverseRouter.route(on(RegulatorRemoveMemberController.class).renderRemoveMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void removeUrl_whenIndustryTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::removeUrl)
        .isEqualTo(ReverseRouter.route(on(IndustryRemoveMemberController.class).renderRemoveMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void editUrl_whenRegulatorTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.REGULATOR)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::editUrl)
        .isEqualTo(ReverseRouter.route(on(RegulatorEditMemberController.class).renderEditMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void editUrl_whenIndustryTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::editUrl)
        .isEqualTo(ReverseRouter.route(on(IndustryEditMemberController.class).renderEditMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }
}
