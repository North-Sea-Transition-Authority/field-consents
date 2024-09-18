package uk.co.nstauthority.fieldconsents.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.INDUSTRY_TEAM;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.CONSENT_RECIPIENT;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.SUBMITTER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsEmailRecipientServiceTest {

  private static final OrganisationGroupDto ORG_GROUP_1_DTO = OrganisationGroupDto.from(ORG_GROUP_1);

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private IndustryTeamService industryTeamService;

  private FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService;

  private OrganisationUnitWithGroupsJson organisationUnitJson;

  @BeforeEach
  void setUp() {
    fieldConsentsEmailRecipientService = new FieldConsentsEmailRecipientService(
        teamMemberViewService,
        industryTeamService
    );
    organisationUnitJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
  }

  @Test
  void getDistinctEmailRecipientsWithRoles_whenNoTeamExists() {
    when(industryTeamService.getTeamByOrganisationGroupId(organisationUnitJson.organisationUnitId())).thenReturn(
        Optional.empty());

    assertThat(fieldConsentsEmailRecipientService.getDistinctEmailRecipientsWithRoles(
        organisationUnitJson, anySet())).isEmpty();
  }

  @Test
  void getDistinctEmailRecipientsWithRoles_whenTeamExistsButNoRecipientsInRoles() {
    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM, Set.of(CONSENT_RECIPIENT))).thenReturn(Collections.emptyList());

    assertThat(fieldConsentsEmailRecipientService.getDistinctEmailRecipientsWithRoles(
        organisationUnitJson, Set.of(CONSENT_RECIPIENT))).isEmpty();
  }

  @Test
  void getDistinctEmailRecipientsWithRoles() {
    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM));

    var teamMemberViewConsentRecipient1 = TeamMemberViewTestUtil.Builder()
        .withWebUserAccountId(WebUserAccountId.from(10L))
        .withTeamId(INDUSTRY_TEAM.toTeamId())
        .withContactEmail("user1@email.com")
        .withRoles(Set.of(SUBMITTER))
        .build();

    var teamMemberViewConsentRecipient2 = TeamMemberViewTestUtil.Builder()
        .withWebUserAccountId(WebUserAccountId.from(20L))
        .withTeamId(INDUSTRY_TEAM.toTeamId())
        .withContactEmail("user2@email.com")
        .withRoles(Set.of(IndustryTeamRole.CONSENT_RECIPIENT))
        .build();

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM, Set.of(SUBMITTER, CONSENT_RECIPIENT)))
        .thenReturn(List.of(teamMemberViewConsentRecipient1, teamMemberViewConsentRecipient2));

    assertThat(fieldConsentsEmailRecipientService.getDistinctEmailRecipientsWithRoles(
        organisationUnitJson, Set.of(SUBMITTER, CONSENT_RECIPIENT)))
        .containsExactly(
            FieldConsentsEmailRecipient.from(teamMemberViewConsentRecipient1),
            FieldConsentsEmailRecipient.from(teamMemberViewConsentRecipient2)
        );
  }
}