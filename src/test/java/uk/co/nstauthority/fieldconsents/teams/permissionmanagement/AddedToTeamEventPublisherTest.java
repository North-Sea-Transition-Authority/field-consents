package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.TeamId;

@ExtendWith(MockitoExtension.class)
class AddedToTeamEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private AddedToTeamEventPublisher addedToTeamEventPublisher;

  @Test
  void publish_verifyEventPublished() {

    var teamId = new TeamId(randomInteger());
    var addedWebUserAccountId = new WebUserAccountId(123);
    var rolesGranted = Set.of("ROLE_1", "ROLE_2");
    var instigatingUser = ServiceUserDetailTestUtil.Builder().build();

    var addedToTeamEventCaptor = ArgumentCaptor.forClass(AddedToTeamEvent.class);

    addedToTeamEventPublisher.publish(teamId, addedWebUserAccountId, rolesGranted, instigatingUser);

    verify(applicationEventPublisher, times(1)).publishEvent(addedToTeamEventCaptor.capture());

    var addToTeamEvent = addedToTeamEventCaptor.getValue();
    assertThat(addToTeamEvent.getTeamAddedTo()).isEqualTo(teamId);
    assertThat(addToTeamEvent.getAddedUserWebUserAccountId()).isEqualTo(addedWebUserAccountId);
    assertThat(addToTeamEvent.getRolesGranted()).isEqualTo(rolesGranted);
    assertThat(addToTeamEvent.getInstigatingUserWebUserAccountId().id()).isEqualTo(instigatingUser.wuaId());
  }

}