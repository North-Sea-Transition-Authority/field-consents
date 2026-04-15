package uk.co.nstauthority.fieldconsents.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroupEmailDomain;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class AllowedDomainServiceTest {

  private static final String USER_EMAIL = "user@example.com";

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @InjectMocks
  private AllowedDomainService allowedDomainService;

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_industry(String domain, boolean isAllowed) {
    var industryTeam = new Team(UUID.randomUUID());
    industryTeam.setTeamType(TeamType.INDUSTRY);
    industryTeam.setName("industry team");
    industryTeam.setScopeType("ORGANISATION_GROUP_ID");
    industryTeam.setScopeId("1");

    var orgGroup = new OrganisationGroupDto();
    orgGroup.setOrganisationGroupId(1);
    orgGroup.setEmailDomains(List.of(new OrganisationGroupEmailDomain(domain)));

    when(organisationGroupQueryService.getOrganisationGroupById(Integer.parseInt(industryTeam.getScopeId()))).thenReturn(
        Optional.of(orgGroup)
    );

    assertThat(allowedDomainService.isAllowedDomain(USER_EMAIL, industryTeam)).isEqualTo(isAllowed);
  }

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_regulator(String domain, boolean isAllowed) {
    var regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeam.setName("regulator team");

    var orgGroup = new OrganisationGroupDto();
    orgGroup.setEmailDomains(List.of(new OrganisationGroupEmailDomain(domain)));

    when(organisationGroupQueryService.getRegulatorOrganisationGroup()).thenReturn(
        Optional.of(orgGroup)
    );

    assertThat(allowedDomainService.isAllowedDomain(USER_EMAIL, regTeam)).isEqualTo(isAllowed);
  }

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_consultee(String domain, boolean isAllowed) {
    var consulteeTeam = new Team(UUID.randomUUID());
    consulteeTeam.setTeamType(TeamType.CONSULTEE);
    consulteeTeam.setName("consultee team");

    var orgGroup = new OrganisationGroupDto();
    orgGroup.setEmailDomains(List.of(new OrganisationGroupEmailDomain(domain)));

    when(organisationGroupQueryService.getConsulteeOrganisationGroup()).thenReturn(
        Optional.of(orgGroup)
    );

    assertThat(allowedDomainService.isAllowedDomain(USER_EMAIL, consulteeTeam)).isEqualTo(isAllowed);
  }

  private static Stream<Arguments> provideDomainIsAllowedCombinations() {
    return Stream.of(
        Arguments.of("example.com", true),
        Arguments.of("domain.com", false)
    );
  }
}