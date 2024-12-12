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

import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerAccessService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ApplicationDataItemServiceTest {

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private FieldEquityPartnerAccessService fieldEquityPartnerAccessService;

  @Mock
  private TeamQueryService teamQueryService;

  private ApplicationDataItemViewService applicationDataItemService;

  private ServiceUserDetail user;
  private List<Condition> conditions;
  private Map<Integer, FieldJson> fieldJsonById;
  private Team shell1IndustryTeam;

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

    shell1IndustryTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.INDUSTRY)
        .withScopeId(ORG_GROUP_ID_1.toString())
        .build();

    fieldJsonById = Map.of(field1Json.getId(), field1Json);

    applicationDataItemService = new ApplicationDataItemViewService(
        applicationDataItemDtoService,
        organisationUnitPermissionService,
        fieldEquityPartnerAccessService,
        new DefaultDSLContext(SQLDialect.DEFAULT),
        teamQueryService
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

    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(Role.EDITOR)
            .build()
    ));

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
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);

    assertThat(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getRegulatorApplicationDataItems_withNoSearchResultItemsToDisplay() {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getRegulatorApplicationDataItems_withFlareSubmitted_forTerminal() {
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.singletonList(applicationDataItemDto));

    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(Role.ACCESS_MANAGER)
            .build()
    ));

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

    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);

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
    shell1IndustryTeam.setScopeId(null);
    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withEmptyResults() {
    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withNoViewPermission() {
    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withEmptySearchResultItemsToDisplay() {
    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).isEmpty();
  }

  @Test
  void getIndustryApplicationDataItems_withFlareSubmitted_forTerminal() {
    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
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
    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
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
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE,  Set.of(Role.ALLOCATOR, Role.RESPONDER, Role.VIEWER)))
        .thenReturn(false);

    assertThat(applicationDataItemService.getConsulteeApplicationDataItemViews(conditions, user)).isEmpty();
  }

  @Test
  void getConsulteeApplicationDataItems_withNoSearchResultItemsToDisplay() {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE,  Set.of(Role.ALLOCATOR, Role.RESPONDER, Role.VIEWER)))
        .thenReturn(true);
    when(applicationDataItemDtoService.runGetDataItemDtoQuery(any())).thenReturn(Collections.emptyList());

    assertThat(applicationDataItemService.getConsulteeApplicationDataItemViews(conditions, user)).isEmpty();
  }

  @Test
  void getApplicationDataItemUserActionFromUser_hasEditorRole() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder().withRole(Role.EDITOR).build()
    ));
    assertThat(applicationDataItemService.getApplicationDataItemUserActionFromUser(user)).isEqualTo(ApplicationDataItemUserAction.RESUME_APPLICATION);
  }

  @Test
  void getApplicationDataItemUserActionFromUser_doesNotHaveEditPermission() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of());
    assertThat(applicationDataItemService.getApplicationDataItemUserActionFromUser(user)).isEqualTo(ApplicationDataItemUserAction.VIEW_APPLICATION);
  }

}
