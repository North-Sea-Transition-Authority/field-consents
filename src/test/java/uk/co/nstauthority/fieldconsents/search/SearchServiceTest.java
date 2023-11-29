package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_PERMISSIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
class SearchServiceTest {

  @Mock
  private SearchFilterService searchFilterService;

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @Mock
  private SearchResultItemDtoService searchResultItemDtoService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private TeamService teamService;
  
  @InjectMocks
  private SearchService searchService;

  private ServiceUserDetail user;

  private SearchFilterForm form;

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

    shell1IndustryTeam = TeamTestUtil.Builder()
        .withOrganisationGroupId(ORG_GROUP_ID_1)
        .withTeamType(TeamType.INDUSTRY)
        .build();

    regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    form = new SearchFilterForm();
    fieldJsonById = Map.of(field1Json.getId(), field1Json);
  }


  @Test
  void getRegulatorSearchResultItems_withNoPermission() {
    when(searchFilterService.getConditions(form, TeamType.REGULATOR)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(searchService.getRegulatorApplicationDataItems(form, user)).isEmpty();
  }

  @Test
  void getRegulatorSearchResultItems_withNoSearchResultItemsToDisplay() {
    when(searchFilterService.getConditions(form, TeamType.REGULATOR)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.emptyList());

    assertThat(searchService.getRegulatorApplicationDataItems(form, user)).isEmpty();
  }

  @Test
  void getRegulatorSearchResultItems_withFlareSubmitted_forTerminal() {
    when(searchFilterService.getConditions(form, TeamType.REGULATOR)).thenReturn(Collections.emptyList());

    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.singletonList(applicationDataItemDto));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.singletonList(regulatorTeam));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItem(
        eq(applicationDataItemDto),
        eq(user),
        eq(TeamType.REGULATOR),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(searchService.getRegulatorApplicationDataItems(form, user))
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
  void getRegulatorSearchResultItems_withProductionInProgress_forField() {
    when(searchFilterService.getConditions(form, TeamType.REGULATOR)).thenReturn(Collections.emptyList());

    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.singletonList(applicationDataItemDto));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.singletonList(regulatorTeam));

    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .thenReturn(Collections.singletonList(field1JsonWithOperator.getOperatorJson()));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItem(
        eq(applicationDataItemDto),
        eq(user),
        eq(TeamType.REGULATOR),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(searchService.getRegulatorApplicationDataItems(form, user))
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
  void getIndustrySearchResultItems_withNoOrganisationGroup() {
    shell1IndustryTeam.setOrganisationGroupId(null);
    assertThat(searchService.getIndustryApplicationDataItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withEmptyResults() {
    assertThat(searchService.getIndustryApplicationDataItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withNoViewPermission() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(searchService.getIndustryApplicationDataItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withEmptySearchResultItemsToDisplay() {
    when(searchFilterService.getConditions(form, TeamType.INDUSTRY)).thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.singletonList(shell1IndustryTeam));
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.emptyList());

    assertThat(searchService.getIndustryApplicationDataItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withFlareSubmitted_forTerminal() {
    when(searchFilterService.getConditions(form, TeamType.INDUSTRY))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(shell1IndustryTeam));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));

    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(searchResultItemDtoService.runSearchQuery(any()))
        .thenReturn(Collections.singletonList(applicationDataItemDto));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItem(
        eq(applicationDataItemDto),
        eq(user),
        eq(TeamType.INDUSTRY),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(searchService.getIndustryApplicationDataItems(form, user))
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
  void getIndustrySearchResultItems_withProductionInProgress_forField() {
    when(searchFilterService.getConditions(form, TeamType.INDUSTRY)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(shell1IndustryTeam));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(List.of(applicationDataItemDto));

    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(fieldJsonById);

    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(user.wuaId()), ENERGY_PORTAL_USER_1);
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto))).thenReturn(portalUserDtoByWuaId);

    when(applicationDataItemDtoService.getApplicationDataItem(
        eq(applicationDataItemDto),
        eq(user),
        eq(TeamType.INDUSTRY),
        organisationUnitNamesByIdCaptor.capture(),
        fieldJsonByIdCaptor.capture(),
        portalUserDtoByWuaIdCaptor.capture()
    ))
        .thenReturn(ApplicationDataItemUtil.getApplicationDataItem());

    assertThat(searchService.getIndustryApplicationDataItems(form, user))
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
  void getConsulteeSearchResultItems_withNoPermission() {
    when(searchFilterService.getConditions(form, TeamType.OPRED)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(searchService.getConsulteeApplicationDataItems(form, user)).isEmpty();
  }

  @Test
  void getConsulteeSearchResultItems_withNoSearchResultItemsToDisplay() {
    when(searchFilterService.getConditions(form, TeamType.OPRED)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.emptyList());

    assertThat(searchService.getConsulteeApplicationDataItems(form, user)).isEmpty();
  }
}
