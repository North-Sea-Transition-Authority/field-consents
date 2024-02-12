package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamManagementController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

class TeamViewTest {

  private CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @BeforeEach
  void setUp() {
    this.customerBrandingConfigurationProperties = new CustomerBrandingConfigurationProperties(
        "stub",
        "mnem",
        "email@fcs.co.uk",
        "regulator legal name",
        "team name"
    );
  }

  @Test
  void teamUrl_whenRegulator() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var teamView = TeamView.fromTeam(team, customerBrandingConfigurationProperties);
    assertThat(teamView)
        .extracting(
            TeamView::teamUrl,
            TeamView::displayName
        )
        .containsExactly(
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(team.toTeamId())),
            customerBrandingConfigurationProperties.mnemonic()
        );
  }

  @Test
  void teamUrl_whenIndustry() {
    var teamName = "test team";
    var team = TeamTestUtil.Builder()
        .withDisplayName(teamName)
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamView = TeamView.fromTeam(team, customerBrandingConfigurationProperties);
    assertThat(teamView)
        .extracting(
            TeamView::teamUrl,
            TeamView::displayName
        )
        .containsExactly(
            ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(team.toTeamId())),
            teamName
        );
  }

  @Test
  void fromTeam_whenRegulator() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var teamView = TeamView.fromTeam(team, customerBrandingConfigurationProperties);
    assertThat(teamView)
        .extracting(TeamView::displayName)
        .isEqualTo(customerBrandingConfigurationProperties.mnemonic())
        .isNotEqualTo(team.getDisplayName());
  }

  @Test
  void fromTeam_whenIndustry() {
    var teamName = "fromTeam industry team";
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withDisplayName(teamName)
        .build();
    var teamView = TeamView.fromTeam(team, customerBrandingConfigurationProperties);
    assertThat(teamView)
        .extracting(TeamView::displayName)
        .isEqualTo(teamName)
        .isNotEqualTo(customerBrandingConfigurationProperties.mnemonic());
  }
}
