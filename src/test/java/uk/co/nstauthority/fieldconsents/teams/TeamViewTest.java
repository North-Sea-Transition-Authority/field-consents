package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.branding.BrandingTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamManagementController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

class TeamViewTest {

  @Test
  void teamUrl_whenRegulator() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var teamView = TeamView.fromTeam(team, BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES);
    assertThat(teamView)
        .extracting(
            TeamView::teamUrl,
            TeamView::displayName
        )
        .containsExactly(
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(team.toTeamId())),
            BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.mnemonic()
        );
  }

  @Test
  void teamUrl_whenIndustry() {
    var teamName = "test team";
    var team = TeamTestUtil.Builder()
        .withDisplayName(teamName)
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamView = TeamView.fromTeam(team, BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES);
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
    var teamView = TeamView.fromTeam(team, BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES);
    assertThat(teamView)
        .extracting(TeamView::displayName)
        .isEqualTo(BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.mnemonic())
        .isNotEqualTo(team.getDisplayName());
  }

  @Test
  void fromTeam_whenIndustry() {
    var teamName = "fromTeam industry team";
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withDisplayName(teamName)
        .build();
    var teamView = TeamView.fromTeam(team, BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES);
    assertThat(teamView)
        .extracting(TeamView::displayName)
        .isEqualTo(teamName)
        .isNotEqualTo(BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.mnemonic());
  }
}
