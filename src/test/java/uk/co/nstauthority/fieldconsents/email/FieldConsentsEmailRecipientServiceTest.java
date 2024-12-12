package uk.co.nstauthority.fieldconsents.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class FieldConsentsEmailRecipientServiceTest {

  private static final OrganisationGroupDto ORG_GROUP_1_DTO = OrganisationGroupDto.from(ORG_GROUP_1);

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService;

  private OrganisationUnitWithGroupsJson organisationUnitJson;

  @BeforeEach
  void setUp() {
    organisationUnitJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
  }

  @Test
  void getDistinctEmailRecipientsWithRoles_noCreatorsFound() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder().withRole(Role.EDITOR).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.SUBMITTER).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.ACCESS_MANAGER).build()
    );

    var teamScopeIds = organisationUnitJson.organisationGroups()
        .stream()
        .map(OrganisationGroupDto::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    when(teamQueryService.getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds))
        .thenReturn(teamRoles);

    assertThat(fieldConsentsEmailRecipientService.getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(Role.CREATOR)))
        .isEmpty();
  }

  @Test
  void getDistinctEmailRecipientsWithRoles_multipleCreatorsFound() {
    var creator1 = TeamRoleTestUtil.newBuilder().withRole(Role.CREATOR).build();
    var creator2 = TeamRoleTestUtil.newBuilder().withRole(Role.CREATOR).build();

    var teamRoles = List.of(
        creator1,
        creator2,
        TeamRoleTestUtil.newBuilder().withRole(Role.ACCESS_MANAGER).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.RESPONDER).build()
    );

    var teamMemberViews = List.of(
        TeamMemberViewTestUtil.newBuilder().withEmail("creator1").build(),
        TeamMemberViewTestUtil.newBuilder().withEmail("creator2").build()
    );

    var teamScopeIds = organisationUnitJson.organisationGroups()
        .stream()
        .map(OrganisationGroupDto::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    when(teamQueryService.getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds))
        .thenReturn(teamRoles);

    when(teamQueryService.getTeamMemberViews(new HashSet<>(teamRoles.subList(0, 2)))).thenReturn(teamMemberViews);

    var expectedEmailRecipients = teamMemberViews
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());

    assertThat(fieldConsentsEmailRecipientService.getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(Role.CREATOR)))
        .isEqualTo(expectedEmailRecipients);
  }


}