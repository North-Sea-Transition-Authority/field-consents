package uk.co.nstauthority.fieldconsents.teams;

import java.util.Set;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

public record TeamMember(WebUserAccountId wuaId, TeamView teamView, Set<TeamRole> roles) {
}
