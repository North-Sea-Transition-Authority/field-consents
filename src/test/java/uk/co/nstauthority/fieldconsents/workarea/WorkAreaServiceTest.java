package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.REGULATOR_PERMISSIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class WorkAreaServiceTest {

  private static final Set<RolePermission> CONSULTEE_ALLOCATE_RESPOND_PERMISSIONS =
      EnumSet.of(ALLOCATE_CONSULTATION, RESPOND_TO_CONSULTATION);

  @Mock
  private WorkAreaItemDtoService workAreaItemDtoService;

  @Mock
  private WorkAreaFilterService workAreaFilterService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private TeamService teamService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @InjectMocks
  private WorkAreaService workAreaService;

  private ServiceUserDetail user;

  private WorkAreaFilter filter;

  private Team shell1IndustryTeam;

  private Team regulatorTeam;

  private Team consulteeTeam;

  private Map<Integer, FieldJson> fieldJsonById;

  @Captor
  private ArgumentCaptor<Map<Integer, String>> organisationUnitNamesByIdCaptor;

  @Captor
  private ArgumentCaptor<Map<Integer, FieldJson>> fieldJsonByIdCaptor;

  @Captor
  private ArgumentCaptor<Map<WebUserAccountId, EnergyPortalUserDto>> portalUserDtoByWuaIdCaptor;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();

    shell1IndustryTeam = TeamTestUtil.Builder()
        .withOrganisationGroupId(ORG_GROUP_ID_1)
        .withTeamType(TeamType.INDUSTRY)
        .build();

    regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    consulteeTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .build();

    filter = new WorkAreaFilter();

    fieldJsonById = Map.of(field1Json.getId(), field1Json);
  }

  @Test
  void getIndustryWorkAreaItems_withNoOrganisationGroup() {
    shell1IndustryTeam.setOrganisationGroupId(null);
    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getIndustryWorkAreaItems_withEmptyResults() {
    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getIndustryWorkAreaItems_withNoEditPermission() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(Collections.emptyList());
    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getIndustryWorkAreaItems_withWorkAreaItemsToDisplay() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(EDIT_FCS_APPLICATIONS))).thenReturn(
        List.of(shell1IndustryTeam));
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getRegulatorWorkAreaItems_withNoPermission() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.REGULATOR, REGULATOR_PERMISSIONS
    ))
        .thenReturn(Collections.emptyList());

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS)).isEmpty();
  }

  @Test
  void getRegulatorWorkAreaItems_withNoWorkAreaItemsToDisplay() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS)).isEmpty();
  }

  @Test
  void getRegulatorWorkAreaItems_withFlareSubmitted_forTerminal() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(Collections.emptyList());
    var workAreaItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal();
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(workAreaItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(workAreaItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(workAreaItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItem(
        eq(workAreaItemDto),
        eq(user),
        eq(TeamType.REGULATOR),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS))
        .containsExactly(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(organisationUnitNamesByIdCaptor.getValue())
        .hasSize(1)
        .containsEntry(
            field1JsonWithOperator.getOperatorJson().organisationUnitId(),
            field1JsonWithOperator.getOperatorJson().name()
        );

    assertThat(fieldJsonByIdCaptor.getValue()).containsExactlyEntriesOf(fieldJsonById);
    assertThat(portalUserDtoByWuaIdCaptor.getValue()).containsExactlyEntriesOf(portalUserDtoByWuaId);
  }

  @Test
  void getIndustryWorkAreaItems_withProductionInProgress_forField() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS)))
        .thenReturn(List.of(shell1IndustryTeam));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var workAreaItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(List.of(workAreaItemDto));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(workAreaItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(workAreaItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItem(
        eq(workAreaItemDto),
        eq(user),
        eq(TeamType.INDUSTRY),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user))
        .containsExactly(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(organisationUnitNamesByIdCaptor.getValue())
        .hasSize(1)
        .containsEntry(
            field1JsonWithOperator.getOperatorJson().organisationUnitId(),
            field1JsonWithOperator.getOperatorJson().name()
        );

    assertThat(fieldJsonByIdCaptor.getValue()).containsExactlyEntriesOf(fieldJsonById);
    assertThat(portalUserDtoByWuaIdCaptor.getValue()).containsExactlyEntriesOf(portalUserDtoByWuaId);
  }

  @Test
  void getConsulteeWorkAreaItems_withNoPermission() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.OPRED,
        CONSULTEE_ALLOCATE_RESPOND_PERMISSIONS
    ))
        .thenReturn(Collections.emptyList());

    assertThat(workAreaService.getConsulteeWorkAreaItems(filter, user, WorkAreaTab.ALL_CONSULTATIONS)).isEmpty();
  }

  @Test
  void getConsulteeWorkAreaItems_withNoWorkAreaItemsToDisplay() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED,
        CONSULTEE_ALLOCATE_RESPOND_PERMISSIONS))
        .thenReturn(List.of(consulteeTeam));
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getConsulteeWorkAreaItems(filter, user, WorkAreaTab.ALL_CONSULTATIONS)).isEmpty();
  }

  @Test
  void getConsulteeWorkAreaItems_withFlareAnnualSubmittedConsultationOpen() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS))
        .thenReturn(Collections.emptyList());

    var workAreaItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualFlareSubmittedForFieldConsultationOpen();
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any()))
        .thenReturn(Collections.singletonList(workAreaItemDto));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, CONSULTEE_ALLOCATE_RESPOND_PERMISSIONS))
        .thenReturn(Collections.singletonList(consulteeTeam));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(workAreaItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(workAreaItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(workAreaItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItem(
        eq(workAreaItemDto),
        eq(user),
        eq(TeamType.OPRED),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(workAreaService.getConsulteeWorkAreaItems(filter, user, WorkAreaTab.ALL_CONSULTATIONS))
        .containsExactly(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(organisationUnitNamesByIdCaptor.getValue())
        .hasSize(1)
        .containsEntry(
            field1JsonWithOperator.getOperatorJson().organisationUnitId(),
            field1JsonWithOperator.getOperatorJson().name()
        );

    assertThat(fieldJsonByIdCaptor.getValue()).containsExactlyEntriesOf(fieldJsonById);
    assertThat(portalUserDtoByWuaIdCaptor.getValue()).containsExactlyEntriesOf(portalUserDtoByWuaId);
  }

  @Test
  void getTabsAvailableToUser_withNoPermissionForAnyTab() {
    when(permissionService.hasPermission(any(), any()))
        .thenReturn(false);

    assertThat(workAreaService.getTabsAvailableToUser(user)).isEmpty();
  }

  @Test
  void getTabsAvailableToUser_withProcessFcsApplications() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.MY_APPLICATIONS,
            WorkAreaTab.UNASSIGNED_APPLICATIONS
        );
  }

  @Test
  void getTabsAvailableToUser_withAssignFcsApplications() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(false);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.ALL_APPLICATIONS,
            WorkAreaTab.UNASSIGNED_APPLICATIONS
        );
  }

  @Test
  void getTabsAvailableToUser_withTechnicalReviewFcsApplications() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(false);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.MY_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_TECHNICAL_REVIEWS
        );
  }

  @Test
  void getTabsAvailableToUser_withAllocationConsultation() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(true);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.ALL_CONSULTATIONS,
            WorkAreaTab.UNASSIGNED_CONSULTATIONS
        );
  }

  @Test
  void getTabsAvailableToUser_withProcessFcsApplicationsAndAssignFcsApplications() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(false);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.MY_APPLICATIONS,
            WorkAreaTab.ALL_APPLICATIONS,
            WorkAreaTab.UNASSIGNED_APPLICATIONS
        );
  }

  @Test
  void getTabsAvailableToUser_withProcessFcsApplicationsAndTechnicalReviewFcsApplications() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(false);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.MY_APPLICATIONS,
            WorkAreaTab.MY_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_TECHNICAL_REVIEWS,
            WorkAreaTab.UNASSIGNED_APPLICATIONS
        );
  }

  @Test
  void getTabsAvailableToUser_withAssignFcsApplicationsAndTechnicalReviewFcsApplications() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(false);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.MY_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_APPLICATIONS,
            WorkAreaTab.UNASSIGNED_APPLICATIONS
        );
  }

  @Test
  void getTabsAvailableToUser_withProcessFcsApplicationsAndAssignFcsApplicationsAndTechnicalReviewFcsApplications() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(false);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.MY_APPLICATIONS,
            WorkAreaTab.MY_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_APPLICATIONS,
            WorkAreaTab.UNASSIGNED_APPLICATIONS
        );
  }

  @Test
  void getTabsAvailableToUser_withAllRegulatorAndConsulteePermissions() {
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(ALLOCATE_CONSULTATION)))
        .thenReturn(true);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.MY_APPLICATIONS,
            WorkAreaTab.MY_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_TECHNICAL_REVIEWS,
            WorkAreaTab.ALL_APPLICATIONS,
            WorkAreaTab.UNASSIGNED_APPLICATIONS,
            WorkAreaTab.ALL_CONSULTATIONS,
            WorkAreaTab.UNASSIGNED_CONSULTATIONS
        );
  }
}
