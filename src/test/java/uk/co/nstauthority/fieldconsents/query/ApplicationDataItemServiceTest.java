package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_PERMISSIONS;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.Condition;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerPermissionService;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class ApplicationDataItemServiceTest {

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private TeamService teamService;

  @Mock
  private FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService;

  @Mock
  PermissionService permissionService;

  private ApplicationDataItemViewService applicationDataItemService;

  private ServiceUserDetail user;
  private List<Condition> conditions;
  private Map<Integer, FieldJson> fieldJsonById;
  private Team shell1IndustryTeam;
  private Team regulatorTeam;

  @Captor
  private ArgumentCaptor<Map<Integer, String>> organisationUnitNamesByIdCaptor;

  @Captor
  private ArgumentCaptor<Map<Integer, FieldJson>> fieldJsonByIdCaptor;

  @Captor
  private ArgumentCaptor<Map<WebUserAccountId, EnergyPortalUserDto>> portalUserDtoByWuaIdCaptor;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();

    conditions = List.of(mock(Condition.class), mock(Condition.class));

    shell1IndustryTeam = TeamTestUtil.Builder()
        .withOrganisationGroupId(ORG_GROUP_ID_1)
        .withTeamType(TeamType.INDUSTRY)
        .build();

    regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    fieldJsonById = Map.of(field1Json.getId(), field1Json);

    applicationDataItemService = new ApplicationDataItemViewService(
        applicationDataItemDtoService,
        organisationUnitPermissionService,
        teamService,
        permissionService,
        fieldEquityPartnerPermissionService,
        new DefaultDSLContext(SQLDialect.DEFAULT)
    );
  }

  @Test
  void getItemsFromDtos() {
    var dto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    var applicationDataItemDtos = List.of(dto);

    var fieldJsonById = Map.of(field1Json.getId(), field1Json);
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(applicationDataItemDtos)).thenReturn(fieldJsonById);

    var energyPortalUserByWebUserAccountId = Map.of( mock(WebUserAccountId.class), mock(EnergyPortalUserDto.class));
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos)).thenReturn(energyPortalUserByWebUserAccountId);

    var teamType = TeamType.REGULATOR;
    var orgUnit = orgUnit1Json;

    when(permissionService.hasPermission(user, Set.of(EDIT_FCS_APPLICATIONS))).thenReturn(true);

    var applicationDataItem = mock(ApplicationDataItemView.class);
    when(applicationDataItemDtoService.getApplicationDataItemView(
        dto,
        ApplicationDataItemUserAction.RESUME_APPLICATION,
        teamType,
        Map.of(orgUnit.organisationUnitId(), orgUnit.name()),
        fieldJsonById,
        energyPortalUserByWebUserAccountId
    )).thenReturn(applicationDataItem);

    assertThat(applicationDataItemService.getItemViewsFromDtos(
        applicationDataItemDtos,
        List.of(orgUnit),
        teamType,
        user
    )).containsExactly(applicationDataItem);
  }

  @ParameterizedTest
  @EnumSource(TeamType.class)
  void getItemsFromDtos_emptyDtoCollection(TeamType teamType) {
    assertThat(applicationDataItemService.getItemViewsFromDtos(Collections.emptyList(), List.of(orgUnit1Json), teamType, user)).isEmpty();
  }

  @Test
  void getRegulatorApplicationDataItems_withNoPermission() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getRegulatorApplicationDataItems_withNoSearchResultItemsToDisplay() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getRegulatorApplicationDataItems_withFlareSubmitted_forTerminal() {
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.singletonList(applicationDataItemDto));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.singletonList(regulatorTeam));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(permissionService.hasPermission(user, Set.of(EDIT_FCS_APPLICATIONS))).thenReturn(false);

    when(applicationDataItemDtoService.getApplicationDataItemView(
        eq(applicationDataItemDto),
        eq(ApplicationDataItemUserAction.VIEW_APPLICATION),
        eq(TeamType.REGULATOR),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItemView());

    assertThat(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user))
        .containsExactly(ApplicationDataItemUtil.getApplicationDataItemView());

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
  void getRegulatorApplicationDataItems_withProductionInProgress_forField() {
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.singletonList(applicationDataItemDto));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.singletonList(regulatorTeam));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItemView(
        eq(applicationDataItemDto),
        eq(ApplicationDataItemUserAction.VIEW_APPLICATION),
        eq(TeamType.REGULATOR),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItemView());

    assertThat(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user))
        .containsExactly(ApplicationDataItemUtil.getApplicationDataItemView());

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
  void getIndustryApplicationDataItems_withNoOrganisationGroup() {
    shell1IndustryTeam.setOrganisationGroupId(null);
    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withEmptyResults() {
    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withNoViewPermission() {
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withEmptySearchResultItemsToDisplay() {
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withFlareSubmitted_forTerminal() {
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));

    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any()))
        .thenReturn(Collections.singletonList(applicationDataItemDto));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItemView(
        eq(applicationDataItemDto),
        eq(ApplicationDataItemUserAction.VIEW_APPLICATION),
        eq(TeamType.INDUSTRY),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItemView());

    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user))
        .containsExactly(ApplicationDataItemUtil.getApplicationDataItemView());

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
  void getIndustryApplicationDataItems_withProductionInProgress_forField() {
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(List.of(applicationDataItemDto));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItemView(
        eq(applicationDataItemDto),
        eq(ApplicationDataItemUserAction.VIEW_APPLICATION),
        eq(TeamType.INDUSTRY),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItemView());

    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user))
        .containsExactly(ApplicationDataItemUtil.getApplicationDataItemView());

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
  void getConsulteeApplicationDataItems_withNoPermission() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getConsulteeApplicationDataItemViews(conditions, user)).isEmpty();
  }

  @Test
  void getConsulteeApplicationDataItems_withNoSearchResultItemsToDisplay() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getConsulteeApplicationDataItemViews(conditions, user)).isEmpty();
  }

  @Test
  void getApplicationDataItemUserActionFromUser_hasEditPermission() {
    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(true);
    assertThat(applicationDataItemService.getApplicationDataItemUserActionFromUser(user)).isEqualTo(ApplicationDataItemUserAction.RESUME_APPLICATION);
  }

  @Test
  void getApplicationDataItemUserActionFromUser_doesNotHaveEditPermission() {
    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(false);
    assertThat(applicationDataItemService.getApplicationDataItemUserActionFromUser(user)).isEqualTo(ApplicationDataItemUserAction.VIEW_APPLICATION);
  }

}
