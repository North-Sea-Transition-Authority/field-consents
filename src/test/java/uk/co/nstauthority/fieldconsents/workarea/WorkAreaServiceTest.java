package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class WorkAreaServiceTest {

  @Mock
  private WorkAreaItemDtoService workAreaItemDtoService;

  @Mock
  private WorkAreaFilterService workAreaFilterService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @Mock
  private ApplicationDataItemViewService applicationDataItemViewService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private WorkAreaService workAreaService;

  private ServiceUserDetail user;

  private WorkAreaFilter filter;

  private Team shell1IndustryTeam;

  private Team regulatorTeam;

  private Team consulteeTeam;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();

    shell1IndustryTeam = TeamTestUtil.newBuilder()
        .withScopeId(ORG_GROUP_ID_1.toString())
        .withScopeType(TeamScopeReference.ORGANISATION_GROUP_ID)
        .withTeamType(TeamType.INDUSTRY)
        .build();

    regulatorTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    consulteeTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    filter = new WorkAreaFilter();
  }

  @Test
  void getIndustryWorkAreaItemViews_withNoOrganisationGroup() {
    shell1IndustryTeam.setScopeId(null);
    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getIndustryWorkAreaItemViews_withEmptyResults() {
    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getIndustryWorkAreaItemViews_noEditOrSubmitApplicationRoles() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(shell1IndustryTeam)
            .withRole(Role.INDUSTRY_ACCESS_MANAGER)
            .build()
    ));
    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getIndustryWorkAreaItemViews_withNoWorkAreaItemsToDisplay() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(new ArrayList<>());
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(shell1IndustryTeam)
            .withRole(Role.EDITOR)
            .build()
    ));
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user)).isEmpty();
  }

  @Test
  void getRegulatorWorkAreaItemViews_withNoPermission() {
    var regulatorRoles = new HashSet<>(RoleGroup.REGULATOR_CASE_PROCESSING_ROLES);
    regulatorRoles.add(Role.VIEWER);
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, regulatorRoles)).thenReturn(false);

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS)).isEmpty();
  }

  @Test
  void getRegulatorWorkAreaItemViews_withNoWorkAreaItemsToDisplay() {
    var regulatorRoles = new HashSet<>(RoleGroup.REGULATOR_CASE_PROCESSING_ROLES);
    regulatorRoles.add(Role.VIEWER);
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, regulatorRoles)).thenReturn(true);
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(new ArrayList<>());
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS)).isEmpty();
  }

  @Test
  void getRegulatorWorkAreaItemViews_withFlareSubmitted_forTerminal() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS)).thenReturn(Collections.emptyList());
    var workAreaItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal();
    var workAreaItemDtos = List.of(workAreaItemDto);
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(workAreaItemDtos);

    var regulatorRoles = new HashSet<>(RoleGroup.REGULATOR_CASE_PROCESSING_ROLES);
    regulatorRoles.add(Role.VIEWER);
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, regulatorRoles)).thenReturn(true);

    var organisationUnitJson = List.of(field1JsonWithOperator.getOperatorJson());
    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(workAreaItemDtos))
        .thenReturn(organisationUnitJson);

    var applicationDataItemView = ApplicationDataItemUtil.getApplicationDataItemView();
    when(applicationDataItemViewService.getItemViewsFromDtos(
        workAreaItemDtos,
        organisationUnitJson,
        TeamType.REGULATOR,
        user
    ))
        .thenReturn(Collections.singletonList(applicationDataItemView));

    assertThat(workAreaService.getRegulatorWorkAreaItems(filter, user, WorkAreaTab.MY_APPLICATIONS))
        .containsExactly(applicationDataItemView);
  }

  @Test
  void getIndustryWorkAreaItemViews_withProductionInProgress_forField() {
    when(workAreaFilterService.getConditions(filter, user, null)).thenReturn(Collections.emptyList());
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(shell1IndustryTeam)
            .withRole(Role.SUBMITTER)
            .build()
    ));

    var organisationUnitJsons = List.of(field1JsonWithOperator.getOperatorJson());
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1)))
        .thenReturn(organisationUnitJsons);

    var workAreaItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    var workAreaItemDtos = List.of(workAreaItemDto);
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(workAreaItemDtos);

    var collectionCaptor = ArgumentCaptor.forClass(Collection.class);
    var applicationDataItem = ApplicationDataItemUtil.getApplicationDataItemView();
    when(applicationDataItemViewService.getItemViewsFromDtos(
        eq(workAreaItemDtos),
        collectionCaptor.capture(),
        eq(TeamType.INDUSTRY),
        eq(user)
    ))
        .thenReturn(Collections.singletonList(applicationDataItem));

    assertThat(workAreaService.getIndustryWorkAreaItems(filter, user))
        .containsExactly(applicationDataItem);

    assertThat(collectionCaptor.getValue())
        .containsExactlyElementsOf(organisationUnitJsons);
  }

  @Test
  void getConsulteeWorkAreaItemViews_withNoPermission() {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE, Set.of(Role.ALLOCATOR, Role.RESPONDER))).thenReturn(true);

    assertThat(workAreaService.getConsulteeWorkAreaItems(filter, user, WorkAreaTab.ALL_CONSULTATIONS)).isEmpty();
  }

  @Test
  void getConsulteeWorkAreaItemViews_withNoWorkAreaItemsToDisplay() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS)).thenReturn(new ArrayList<>());
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE, Set.of(Role.ALLOCATOR, Role.RESPONDER))).thenReturn(true);
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any())).thenReturn(Collections.emptyList());

    assertThat(workAreaService.getConsulteeWorkAreaItems(filter, user, WorkAreaTab.ALL_CONSULTATIONS)).isEmpty();
  }

  @Test
  void getConsulteeWorkAreaItemViews_withFlareAnnualSubmittedConsultationOpen() {
    when(workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS))
        .thenReturn(Collections.emptyList());

    var workAreaItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualFlareSubmittedForFieldConsultationOpen();
    var workAreaItemDtos = List.of(workAreaItemDto);
    when(workAreaItemDtoService.runWorkAreaQuery(any(), any()))
        .thenReturn(Collections.singletonList(workAreaItemDto));

    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE, Set.of(Role.ALLOCATOR, Role.RESPONDER))).thenReturn(true);

    var organisationUnitJsons = List.of(field1JsonWithOperator.getOperatorJson());
    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(workAreaItemDto)))
        .thenReturn(organisationUnitJsons);

    var applicationDataItemView = ApplicationDataItemUtil.getApplicationDataItemView();
    when(applicationDataItemViewService.getItemViewsFromDtos(
        workAreaItemDtos,
        organisationUnitJsons,
        TeamType.CONSULTEE,
        user
    )).thenReturn(Collections.singletonList(applicationDataItemView));

    assertThat(workAreaService.getConsulteeWorkAreaItems(filter, user, WorkAreaTab.ALL_CONSULTATIONS))
        .containsExactly(applicationDataItemView);
  }

  @Test
  void getTabsAvailableToUser_withNoPermissionForAnyTab() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of());
    assertThat(workAreaService.getTabsAvailableToUser(user)).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("getTabsAvailableToUser_arguments")
  void getTabsAvailableToUser(Role role, List<WorkAreaTab> expectedWorkAreaTabs) {
    when(teamQueryService.getTeamRoles(user))
        .thenReturn(List.of(TeamRoleTestUtil.newBuilder().withRole(role).build()
    ));

    assertThat(workAreaService.getTabsAvailableToUser(user))
        .isEqualTo(expectedWorkAreaTabs);
  }

  private static Stream<Arguments> getTabsAvailableToUser_arguments() {
    return Stream.of(
        arguments(Role.CASE_OFFICER, List.of(WorkAreaTab.MY_APPLICATIONS, WorkAreaTab.UNASSIGNED_APPLICATIONS)),
        arguments(Role.CASE_MANAGER, List.of(WorkAreaTab.ALL_APPLICATIONS, WorkAreaTab.UNASSIGNED_APPLICATIONS)),
        arguments(Role.TECHNICAL_REVIEWER, List.of(WorkAreaTab.MY_TECHNICAL_REVIEWS, WorkAreaTab.ALL_TECHNICAL_REVIEWS)),
        arguments(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, List.of(WorkAreaTab.MY_CAM_APPLICATIONS, WorkAreaTab.ALL_APPLICATIONS)),
        arguments(Role.ALLOCATOR, List.of(WorkAreaTab.UNASSIGNED_CONSULTATIONS, WorkAreaTab.ALL_CONSULTATIONS)),
        arguments(Role.RESPONDER, List.of(WorkAreaTab.MY_CONSULTATIONS))
    );
  }
}
