package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.TeamMember;

public class TeamMemberRemovedEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 1694652841778510682L;
  private final TeamMember teamMember;

  private final WebUserAccountId instigatingUserWebUserAccountId;

  public TeamMemberRemovedEvent(Object source,
                                TeamMember teamMember,
                                WebUserAccountId instigatingUserWebUserAccountId) {
    super(source);
    this.teamMember = teamMember;
    this.instigatingUserWebUserAccountId = instigatingUserWebUserAccountId;
  }

  public TeamMember getTeamMember() {
    return teamMember;
  }

  public WebUserAccountId getInstigatingUserWebUserAccountId() {
    return instigatingUserWebUserAccountId;
  }
}
