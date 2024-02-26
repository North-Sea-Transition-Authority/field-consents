package uk.co.nstauthority.fieldconsents.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryEditMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryRemoveMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredEditMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredRemoveMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorEditMemberController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorRemoveMemberController;
import uk.co.nstauthority.fieldconsents.util.userutil.UserDisplayNameUtil;

public record TeamMemberView(
    WebUserAccountId wuaId,
    TeamView teamView,
    String title,
    String firstName,
    String lastName,
    String contactEmail,
    String contactNumber,
    Set<TeamRole> teamRoles
) {

  public String getDisplayName() {
    return UserDisplayNameUtil.getUserDisplayName(firstName, lastName);
  }

  public String removeUrl() {
    return switch (teamView.teamType()) {
      case REGULATOR -> ReverseRouter.route(on(RegulatorRemoveMemberController.class)
          .renderRemoveMember(teamView.teamId(), wuaId));
      case INDUSTRY -> ReverseRouter.route(on(IndustryRemoveMemberController.class)
          .renderRemoveMember(teamView.teamId(), wuaId));
      case OPRED -> ReverseRouter.route(on(OpredRemoveMemberController.class)
          .renderRemoveMember(teamView.teamId(), wuaId));
    };
  }

  public String editUrl() {
    return switch (teamView.teamType()) {
      case REGULATOR -> ReverseRouter.route(on(RegulatorEditMemberController.class)
          .renderEditMember(teamView.teamId(), wuaId));
      case INDUSTRY -> ReverseRouter.route(on(IndustryEditMemberController.class)
          .renderEditMember(teamView.teamId(), wuaId));
      case OPRED -> ReverseRouter.route(on(OpredEditMemberController.class)
          .renderEditMember(teamView.teamId(), wuaId));
    };
  }
}
