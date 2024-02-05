package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class TeamViewComparatorTest {

  @InjectMocks
  private TeamViewComparator teamViewComparator;

  @Test
  void compare_byTypeDisplayOrder() {
    var customerBrandingConfigurationProperties = new CustomerBrandingConfigurationProperties(
        "stub",
        "mnem",
        "email@fcs.co.uk",
        "regulator legal name"
    );
    var firstTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var secondTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var firstTeamView = TeamView.fromTeam(firstTeam, customerBrandingConfigurationProperties);
    var secondTeamView = TeamView.fromTeam(secondTeam, customerBrandingConfigurationProperties);

    var sortResult = Stream.of(secondTeamView, firstTeamView)
        .sorted(teamViewComparator)
        .toList();

    assertThat(sortResult).containsExactly(firstTeamView, secondTeamView);
  }

  @Test
  void compare_byTeamName() {
    var customerBrandingConfigurationProperties = new CustomerBrandingConfigurationProperties(
        "stub",
        "mnem",
        "email@fcs.co.uk",
        "regulator legal name"
    );
    var firstTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withDisplayName("team 1")
        .build();
    var secondTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withDisplayName("team 2")
        .build();

    var firstTeamView = TeamView.fromTeam(firstTeam, customerBrandingConfigurationProperties);
    var secondTeamView = TeamView.fromTeam(secondTeam, customerBrandingConfigurationProperties);

    var sortResult = Stream.of(secondTeamView, firstTeamView)
        .sorted(teamViewComparator)
        .toList();

    assertThat(sortResult).containsExactly(firstTeamView, secondTeamView);
  }
}
