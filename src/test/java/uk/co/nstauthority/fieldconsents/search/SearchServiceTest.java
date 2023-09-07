package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUserAction.RESUME_APPLICATION;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.portalUserDtosMap;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_PERMISSIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;
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

  private ApplicationDataFilterForm form;

  private Map<Integer, FieldJson> fieldJsonMap;

  private Team shell1IndustryTeam;

  private Team regulatorTeam;

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
    form = new ApplicationDataFilterForm();
    fieldJsonMap = Map.of(field1Json.getId(), field1Json);
  }


  @Test
  void getRegulatorSearchResultItems_withNoPermission() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(searchService.getRegulatorSearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getRegulatorSearchResultItems_withNoSearchResultItemsToDisplay() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.emptyList());

    assertThat(searchService.getRegulatorSearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getRegulatorSearchResultItems_withFlareSubmitted_forTerminal() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    var searchResultItemDto = ApplicationDataItemUtil.getSearchResultItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(List.of(searchResultItemDto));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        List.of(searchResultItemDto))).thenReturn(fieldJsonMap);
    when(applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    when(applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(portalUserDtosMap);
    when(applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(user)).thenReturn(RESUME_APPLICATION);
    when(applicationDataItemDtoService.getDisplayReference(searchResultItemDto, RESUME_APPLICATION))
        .thenReturn(ApplicationDataItemUtil.getCaseReference(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayConsentDuration(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getDuration(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayCaseOfficer(searchResultItemDto, portalUserDtosMap))
        .thenReturn(ApplicationDataItemUtil.getCaseOfficer(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAceFlag(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getAceFlag(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplaySubmitter(searchResultItemDto, portalUserDtosMap))
        .thenReturn(ApplicationDataItemUtil.getSubmitter(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAssetLocation(searchResultItemDto, fieldJsonMap))
        .thenReturn(ApplicationDataItemUtil.getGeographicArea(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayTechnicalReviewer(searchResultItemDto, portalUserDtosMap, TeamType.REGULATOR))
        .thenReturn(ApplicationDataItemUtil.getTechnicalReviewer(searchResultItemDto, TeamType.REGULATOR));

    var searchResultItems = searchService.getRegulatorSearchResultItems(form, user);

    assertThat(searchResultItems).hasSize(1);
    assertThat(searchResultItems.stream().toList().get(0)).usingRecursiveComparison()
        .isEqualTo(ApplicationDataItemUtil.getSearchResultItemFromDto(searchResultItemDto, TeamType.REGULATOR));
  }

  @Test
  void getRegulatorSearchResultItems_withProductionInProgress_forField() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    var searchResultItemDto = ApplicationDataItemUtil.getSearchResultItemDtoForAnnualProductionInProgressForField();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(List.of(searchResultItemDto));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        List.of(searchResultItemDto))).thenReturn(fieldJsonMap);
    when(applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    when(applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(portalUserDtosMap);
    when(applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(user)).thenReturn(RESUME_APPLICATION);
    when(applicationDataItemDtoService.getDisplayReference(searchResultItemDto, RESUME_APPLICATION))
        .thenReturn(ApplicationDataItemUtil.getCaseReference(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayConsentDuration(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getDuration(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayCaseOfficer(searchResultItemDto, portalUserDtosMap))
        .thenReturn(ApplicationDataItemUtil.getCaseOfficer(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAceFlag(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getAceFlag(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAssetLocation(searchResultItemDto, fieldJsonMap))
        .thenReturn(ApplicationDataItemUtil.getGeographicArea(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayTechnicalReviewer(searchResultItemDto, portalUserDtosMap, TeamType.REGULATOR))
        .thenReturn(ApplicationDataItemUtil.getTechnicalReviewer(searchResultItemDto, TeamType.REGULATOR));

    var searchResultItems = searchService.getRegulatorSearchResultItems(form, user);

    assertThat(searchResultItems).hasSize(1);
    assertThat(searchResultItems.stream().toList().get(0)).usingRecursiveComparison()
        .isEqualTo(ApplicationDataItemUtil.getSearchResultItemFromDto(searchResultItemDto, TeamType.REGULATOR));
  }

  @Test
  void getIndustrySearchResultItems_withNoOrganisationGroup() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    shell1IndustryTeam.setOrganisationGroupId(null);

    assertThat(searchService.getIndustrySearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withEmptyResults() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());

    assertThat(searchService.getIndustrySearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withNoViewPermission() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(searchService.getIndustrySearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withEmptySearchResultItemsToDisplay() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS)).thenReturn(
        List.of(shell1IndustryTeam));
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.emptyList());

    assertThat(searchService.getIndustrySearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getIndustrySearchResultItems_withFlareSubmitted_forTerminal() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(shell1IndustryTeam));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var searchResultItemDto = ApplicationDataItemUtil.getSearchResultItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(List.of(searchResultItemDto));
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        List.of(searchResultItemDto))).thenReturn(fieldJsonMap);
    when(applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(portalUserDtosMap);
    when(applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(user)).thenReturn(RESUME_APPLICATION);
    when(applicationDataItemDtoService.getDisplayReference(searchResultItemDto, RESUME_APPLICATION))
        .thenReturn(ApplicationDataItemUtil.getCaseReference(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayConsentDuration(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getDuration(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayCaseOfficer(searchResultItemDto, portalUserDtosMap))
        .thenReturn(ApplicationDataItemUtil.getCaseOfficer(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAceFlag(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getAceFlag(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplaySubmitter(searchResultItemDto, portalUserDtosMap))
        .thenReturn(ApplicationDataItemUtil.getSubmitter(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAssetLocation(searchResultItemDto, fieldJsonMap))
        .thenReturn(ApplicationDataItemUtil.getGeographicArea(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayTechnicalReviewer(searchResultItemDto, portalUserDtosMap, TeamType.INDUSTRY))
        .thenReturn(ApplicationDataItemUtil.getTechnicalReviewer(searchResultItemDto, TeamType.INDUSTRY));

    var searchResultItems = searchService.getIndustrySearchResultItems(form, user);

    assertThat(searchResultItems).hasSize(1);
    assertThat(searchResultItems.stream().toList().get(0))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationDataItemUtil.getSearchResultItemFromDto(searchResultItemDto, TeamType.INDUSTRY));
  }

  @Test
  void getIndustrySearchResultItems_withProductionInProgress_forField() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(shell1IndustryTeam));
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    var searchResultItemDto = ApplicationDataItemUtil.getSearchResultItemDtoForAnnualProductionInProgressForField();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(List.of(searchResultItemDto));
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        List.of(searchResultItemDto))).thenReturn(fieldJsonMap);
    when(applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(portalUserDtosMap);
    when(applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(user)).thenReturn(RESUME_APPLICATION);
    when(applicationDataItemDtoService.getDisplayReference(searchResultItemDto, RESUME_APPLICATION))
        .thenReturn(ApplicationDataItemUtil.getCaseReference(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayConsentDuration(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getDuration(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayCaseOfficer(searchResultItemDto, portalUserDtosMap))
        .thenReturn(ApplicationDataItemUtil.getCaseOfficer(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAceFlag(searchResultItemDto))
        .thenReturn(ApplicationDataItemUtil.getAceFlag(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayAssetLocation(searchResultItemDto, fieldJsonMap))
        .thenReturn(ApplicationDataItemUtil.getGeographicArea(searchResultItemDto));
    when(applicationDataItemDtoService.getDisplayTechnicalReviewer(searchResultItemDto, portalUserDtosMap, TeamType.INDUSTRY))
        .thenReturn(ApplicationDataItemUtil.getTechnicalReviewer(searchResultItemDto, TeamType.INDUSTRY));

    var searchResultItems = searchService.getIndustrySearchResultItems(form, user);

    assertThat(searchResultItems).hasSize(1);
    assertThat(searchResultItems.stream().toList().get(0))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationDataItemUtil.getSearchResultItemFromDto(searchResultItemDto, TeamType.INDUSTRY));
  }
}
