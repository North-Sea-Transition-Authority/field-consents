package uk.co.nstauthority.fieldconsents.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamManagementController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

public record TeamView(TeamId teamId, TeamType teamType, String displayName) {

  public String teamUrl() {
    return switch (teamType) {
      case REGULATOR -> ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
      case INDUSTRY -> ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(teamId));
    };
  }

  public static TeamView fromTeam(Team team, CustomerConfigurationProperties customerConfigurationProperties) {
    var teamId = new TeamId(team.getId());
    var teamName = switch (team.getTeamType()) {
      case REGULATOR -> customerConfigurationProperties.mnemonic();
      case INDUSTRY -> team.getDisplayName();
    };
    return new TeamView(teamId, team.getTeamType(), teamName);
  }

  public static TeamViewComparator sort() {
    return new TeamViewComparator();
  }

}
