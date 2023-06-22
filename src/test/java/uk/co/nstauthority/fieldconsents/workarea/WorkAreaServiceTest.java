package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CASE_OFFICER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaService.ALL_ORG_UNITS_WORK_AREA_PURPOSE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class WorkAreaServiceTest {

  @Mock
  private WorkAreaItemDtoRepository workAreaItemDtoRepository;

  @Mock
  private WorkAreaFilterService workAreaFilterService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private TeamService teamService;

  @Mock
  private FieldService fieldService;

  @Mock
  PermissionService permissionService;

  @InjectMocks
  private WorkAreaService workAreaService;

  private ServiceUserDetail user;

  private WorkAreaFilter filter;

  private Team shell1IndustryTeam;

  private Team regulatorTeam;

  private ApplicationVersion ventVersionSubmitted;

  private ApplicationVersion flareVersionSubmitted;

  private EnergyPortalUserDto submitter;

  private EnergyPortalUserDto caseOfficer;

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

    ventVersionSubmitted = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    flareVersionSubmitted = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    filter = new WorkAreaFilter();
    submitter = EnergyPortalUserDtoTestUtil.Builder().build();
    caseOfficer = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(CASE_OFFICER_WUA_ID)
        .build();
  }

  @Test
  void getWorkAreaItems_forIndustryNoOrganisationGroup() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    shell1IndustryTeam.setOrganisationGroupId(null);

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forIndustry_withEmptyResults() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forIndustry_withNoEditPermission() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(
        Collections.emptyList());

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forIndustry_withWorkAreaItemsToDisplay() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(EDIT_FCS_APPLICATIONS))).thenReturn(
        List.of(shell1IndustryTeam));
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forRegulator_withNoPermission() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, Set.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS))).thenReturn(
        Collections.emptyList());

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forRegulator_withNoWorkAreaItemsToDisplay() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR,
        Set.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS))).thenReturn(
        List.of(regulatorTeam));
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forRegulator_withSubmittedApplication() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, Set.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS))).thenReturn(
        List.of(regulatorTeam));
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForShortVentSubmittedForTerminal();
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(organisationUnitService.getOrganisationUnitsByIds(List.of(PRIMARY_OPERATOR_OU_ID_1), ALL_ORG_UNITS_WORK_AREA_PURPOSE))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(workAreaItemDto.submittedByWuaId())))).thenReturn(List.of(submitter));
    when(applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())).thenReturn(
        ventVersionSubmitted);
    doCallRealMethod().when(applicationService).generateApplicationReference(ventVersionSubmitted);

    var workAreaItems = workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS);

    assertThat(workAreaItems).hasSize(1);
    assertThat(workAreaItems.stream().toList().get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forRegulator_withCaseAssigned() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, Set.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS))).thenReturn(
        List.of(regulatorTeam));
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForShortVentAssignedForTerminal();
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(organisationUnitService.getOrganisationUnitsByIds(List.of(PRIMARY_OPERATOR_OU_ID_1), ALL_ORG_UNITS_WORK_AREA_PURPOSE))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var portalUserWuaIdList = List.of(new WebUserAccountId(workAreaItemDto.submittedByWuaId()), new WebUserAccountId(workAreaItemDto.caseOfficerWuaId()));
    when(energyPortalUserService.findByWuaIds(portalUserWuaIdList)).thenReturn(List.of(submitter, caseOfficer));
    when(applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())).thenReturn(
        ventVersionSubmitted);
    doCallRealMethod().when(applicationService).generateApplicationReference(ventVersionSubmitted);

    var workAreaItems = workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS);

    assertThat(workAreaItems).hasSize(1);
    assertThat(workAreaItems.stream().toList().get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withProductionInProgress_forField_noDuration() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(List.of(shell1IndustryTeam));
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForProductionInProgressForFieldNoDuration();
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(List.of(field1JsonWithOperator));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var workAreaItems = workAreaService.getIndustryWorkAreaItems(filter, user);

    assertThat(workAreaItems).hasSize(1);
    assertThat(workAreaItems.stream().toList().get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withProductionInProgress_forField_annual() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(List.of(shell1IndustryTeam));
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForAnnualProductionInProgressForField();
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(List.of(field1JsonWithOperator));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var workAreaItems = workAreaService.getIndustryWorkAreaItems(filter, user);

    assertThat(workAreaItems).hasSize(1);
    assertThat(workAreaItems.stream().toList().get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withVentSubmitted_forTerminal_shortTerm() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(List.of(shell1IndustryTeam));
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForShortVentSubmittedForTerminal();
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())).thenReturn(
        ventVersionSubmitted);
    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(workAreaItemDto.submittedByWuaId())))).thenReturn(List.of(submitter));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    doCallRealMethod().when(applicationService).generateApplicationReference(ventVersionSubmitted);

    var workAreaItems = workAreaService.getIndustryWorkAreaItems(filter, user);

    assertThat(workAreaItems).hasSize(1);
    assertThat(workAreaItems.stream().toList().get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withFlareSubmitted_forTerminal_longTerm() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(List.of(shell1IndustryTeam));
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForLongFlareSubmittedForTerminal();
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())).thenReturn(
        flareVersionSubmitted);
    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(workAreaItemDto.submittedByWuaId())))).thenReturn(List.of(submitter));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    doCallRealMethod().when(applicationService).generateApplicationReference(flareVersionSubmitted);

    var workAreaItems = workAreaService.getIndustryWorkAreaItems(filter, user);

    assertThat(workAreaItems).hasSize(1);
    assertThat(workAreaItems.stream().toList().get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withFlareSubmitted_forTerminal_longTerm_openWithdrawalRequest() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(List.of(shell1IndustryTeam));
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(workAreaItemDtoRepository.runQuery(any(), any())).thenReturn(List.of(workAreaItemDto));
    when(applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())).thenReturn(
        flareVersionSubmitted);
    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(workAreaItemDto.submittedByWuaId())))).thenReturn(List.of(submitter));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    doCallRealMethod().when(applicationService).generateApplicationReference(flareVersionSubmitted);

    var workAreaItems = workAreaService.getIndustryWorkAreaItems(filter, user);

    assertThat(workAreaItems).hasSize(1);
    assertThat(workAreaItems.stream().toList().get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
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
    when(permissionService.hasPermission(user, EnumSet.of(ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .containsExactly(
            WorkAreaTab.ALL_APPLICATIONS
        );
  }
}
