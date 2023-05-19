package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormService.FIELD_LOOKUP_PURPOSE;

import java.util.ArrayList;
import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
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
  private TeamService teamService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private FieldService fieldService;

  @InjectMocks
  private WorkAreaService workAreaService;

  private ServiceUserDetail user;

  private WorkAreaFilter filter;

  private Team shell1IndustryTeam;

  private Team regulatorTeam;

  private ApplicationVersion ventVersionSubmitted;

  private ApplicationVersion flareVersionSubmitted;

  private EnergyPortalUserDto submitter;

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

    when(userDetailService.getUserDetail()).thenReturn(user);
    when(workAreaFilterService.getConditions(filter)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(List.of(shell1IndustryTeam));
  }

  @Test
  void getWorkAreaItems_forIndustryNoOrganisationGroup() {
    when(workAreaFilterService.getConditions(filter)).thenReturn(new ArrayList<>());
    shell1IndustryTeam.setOrganisationGroupId(null);

    assertThat(workAreaService.getWorkAreaItems(filter)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forIndustry_withEmptyResults() {
    when(workAreaFilterService.getConditions(filter)).thenReturn(new ArrayList<>());
    when(workAreaItemDtoRepository.runQuery(any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getWorkAreaItems(filter)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forIndustry_withNoEditPermission() {
    when(workAreaFilterService.getConditions(filter)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(
        Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(
        Collections.emptyList());

    assertThat(workAreaService.getWorkAreaItems(filter)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forRegulator_withEditPermission() {
    when(workAreaFilterService.getConditions(filter)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(
        Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, Set.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(
        List.of(regulatorTeam));

    // TODO: FCS-78 Update this when implementing the work-area for regulator users
    assertThat(workAreaService.getWorkAreaItems(filter)).isEmpty();
  }

  @Test
  void getWorkAreaItems_forIndustry_withProductionInProgress_forField_noDuration() {
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForProductionInProgressForFieldNoDuration();
    when(workAreaItemDtoRepository.runQuery(any())).thenReturn(List.of(workAreaItemDto));
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(List.of(field1JsonWithOperator));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var workAreaItemList = workAreaService.getWorkAreaItems(filter);

    assertThat(workAreaItemList).hasSize(1);
    assertThat(workAreaItemList.get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withProductionInProgress_forField_annual() {
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForAnnualProductionInProgressForField();
    when(workAreaItemDtoRepository.runQuery(any())).thenReturn(List.of(workAreaItemDto));
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(List.of(field1JsonWithOperator));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var workAreaItemList = workAreaService.getWorkAreaItems(filter);

    assertThat(workAreaItemList).hasSize(1);
    assertThat(workAreaItemList.get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withVentSubmitted_forTerminal_shortTerm() {
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForShortVentSubmittedForTerminal();
    when(workAreaItemDtoRepository.runQuery(any())).thenReturn(List.of(workAreaItemDto));
    when(applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())).thenReturn(
        ventVersionSubmitted);
    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(workAreaItemDto.submittedByWuaId())))).thenReturn(List.of(submitter));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    doCallRealMethod().when(applicationService).generateApplicationReference(ventVersionSubmitted);

    var workAreaItemList = workAreaService.getWorkAreaItems(filter);

    assertThat(workAreaItemList).hasSize(1);
    assertThat(workAreaItemList.get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }

  @Test
  void getWorkAreaItems_forIndustry_withFlareSubmitted_forTerminal_longTerm() {
    var workAreaItemDto = WorkAreaTestUtil.getWorkAreaItemDtoForLongFlareSubmittedForTerminal();
    when(workAreaItemDtoRepository.runQuery(any())).thenReturn(List.of(workAreaItemDto));
    when(applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())).thenReturn(
        flareVersionSubmitted);
    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(workAreaItemDto.submittedByWuaId())))).thenReturn(List.of(submitter));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1))).thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    doCallRealMethod().when(applicationService).generateApplicationReference(flareVersionSubmitted);

    var workAreaItemList = workAreaService.getWorkAreaItems(filter);

    assertThat(workAreaItemList).hasSize(1);
    assertThat(workAreaItemList.get(0)).usingRecursiveComparison().isEqualTo(WorkAreaTestUtil.getWorkAreaItemFromDto(workAreaItemDto));
  }
}
