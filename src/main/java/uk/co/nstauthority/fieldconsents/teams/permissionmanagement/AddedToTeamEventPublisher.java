package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.TeamId;

@Component
public class AddedToTeamEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public AddedToTeamEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(TeamId teamAddedTo,
                      WebUserAccountId addedUserWebUserAccountId,
                      Set<String> rolesGranted,
                      ServiceUserDetail instigatingUser) {
    applicationEventPublisher.publishEvent(
        new AddedToTeamEvent(
            this,
            teamAddedTo,
            addedUserWebUserAccountId,
            new WebUserAccountId(instigatingUser.wuaId()),
            rolesGranted
        )
    );
  }
}
