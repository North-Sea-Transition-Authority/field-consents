package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.portalUserDtosMap;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.REGULATOR_PERMISSIONS;

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
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @Mock
  private SearchFilterService searchFilterService;

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @Mock
  private SearchResultItemDtoService searchResultItemDtoService;

  @Mock
  private TeamService teamService;
  
  @InjectMocks
  private SearchService searchService;

  private ServiceUserDetail user;

  private Team regulatorTeam;

  private ApplicationDataFilterForm form;

  private Map<Integer, FieldJson> fieldJsonMap;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
    regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    form = new ApplicationDataFilterForm();
    fieldJsonMap = Map.of(field1Json.getId(), field1Json);
  }


  @Test
  void getRegulatorSearchResultItems_withNoPermission() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(searchService.getRegulatorSearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getRegulatorSearchResultItems_withNoSearchResultItemsToDisplay() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(Collections.emptyList());

    assertThat(searchService.getRegulatorSearchResultItems(form, user)).isEmpty();
  }

  @Test
  void getRegulatorSearchResultItems_withFlareSubmitted_forTerminal() {
    when(searchFilterService.getConditions(form)).thenReturn(new ArrayList<>());
    var searchResultItemDto = ApplicationDataItemUtil.getSearchResultItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest();
    when(searchResultItemDtoService.runSearchQuery(any())).thenReturn(List.of(searchResultItemDto));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        List.of(searchResultItemDto))).thenReturn(fieldJsonMap);
    when(applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    when(applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(portalUserDtosMap);
    when(applicationDataItemDtoService.getDisplayReference(searchResultItemDto))
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
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        List.of(searchResultItemDto))).thenReturn(fieldJsonMap);
    when(applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(List.of(field1JsonWithOperator.getOperatorJson()));
    when(applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(searchResultItemDto)))
        .thenReturn(portalUserDtosMap);
    when(applicationDataItemDtoService.getDisplayReference(searchResultItemDto))
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
}
