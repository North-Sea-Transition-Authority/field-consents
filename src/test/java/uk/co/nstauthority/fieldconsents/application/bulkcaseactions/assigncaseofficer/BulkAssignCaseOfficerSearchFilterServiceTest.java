package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_3;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@ExtendWith(MockitoExtension.class)
class BulkAssignCaseOfficerSearchFilterServiceTest {

  private static final Condition CURRENT_CASE_OWNER_IS_EMPTY_OR_IS_CASE_OFFICER_CONDITION =
      APPLICATION_VERSIONS.CURRENT_CASE_OWNER.isNull()
          .or(APPLICATION_VERSIONS.CURRENT_CASE_OWNER.eq(CASE_OFFICER.name()));

  @Mock
  private ApplicationDataFilterFormService filterFormService;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @Mock
  private TeamService teamService;

  @Mock
  private CaseAssignmentService caseAssignmentService;

  @Spy
  @InjectMocks
  private BulkAssignCaseOfficerSearchFilterService bulkAssignCaseOfficerSearchFilterService;

  private ServiceUserDetail caseManagerUser;

  @BeforeEach
  void setUp() {
    caseManagerUser = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getPrefilledOrganisation() {
    var restSearchItem = mock(RestSearchItem.class);
    when(filterFormService.getPrefilledOrganisation(eq(1), anyString())).thenReturn(restSearchItem);
    assertThat(bulkAssignCaseOfficerSearchFilterService.getPrefilledOrganisation(1)).isEqualTo(restSearchItem);
  }

  @Test
  void getPrefilledAsset() {
    var restSearchItem = mock(RestSearchItem.class);
    when(filterFormService.getPrefilledAsset("1")).thenReturn(restSearchItem);
    assertThat(bulkAssignCaseOfficerSearchFilterService.getPrefilledAsset("1")).isEqualTo(restSearchItem);
  }

  @Test
  void getCaseOfficerDisplayOptions() {
    when(caseAssignmentService.getCurrentCaseOfficers()).thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2, ENERGY_PORTAL_USER_3));
    assertThat(bulkAssignCaseOfficerSearchFilterService.getCaseOfficerDisplayOptions())
        .containsExactly(
            entry("unassigned", "Unassigned"),
            entry(ENERGY_PORTAL_USER_1.webUserAccountId().toString(), ENERGY_PORTAL_USER_1.displayName()),
            entry(ENERGY_PORTAL_USER_2.webUserAccountId().toString(), ENERGY_PORTAL_USER_2.displayName()),
            entry(ENERGY_PORTAL_USER_3.webUserAccountId().toString(), ENERGY_PORTAL_USER_3.displayName())
        );
  }

  @Test
  void getConditions_emptyForm_isNotRegulator() {
    when(teamService.isRegulatorUser(caseManagerUser)).thenReturn(false);

    assertThat(bulkAssignCaseOfficerSearchFilterService.getConditions(BulkAssignCaseOfficerSearchFiltersForm.empty(), caseManagerUser))
        .containsExactly(CURRENT_CASE_OWNER_IS_EMPTY_OR_IS_CASE_OFFICER_CONDITION);
  }

  @Test
  void getConditions_emptyForm_isRegulator() {
    var condition = mock(Condition.class);
    when(teamService.isRegulatorUser(caseManagerUser)).thenReturn(true);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(condition);

    assertThat(bulkAssignCaseOfficerSearchFilterService.getConditions(BulkAssignCaseOfficerSearchFiltersForm.empty(), caseManagerUser))
        .containsExactly(condition, CURRENT_CASE_OWNER_IS_EMPTY_OR_IS_CASE_OFFICER_CONDITION);
  }

  @Test
  void getConditions_operatorId() {
    var form = new BulkAssignCaseOfficerSearchFiltersForm(
        1,
        "1FIELD",
        "1TERMINAL",
        "2",
        Set.of(GeographicArea.CNS, GeographicArea.WOS),
        Set.of(AceFlagStatus.ACE),
        Set.of(AssetTypeWithShore.FIELD_ONSHORE)
    );

    var operatorCondition = mock(Condition.class);
    when(applicationDataFilterService.getOperatorCondition(form.operatorId())).thenReturn(operatorCondition);

    var geographicAreasCondition = mock(Condition.class);
    when(applicationDataFilterService.getGeographicAreasQueryCondition(form.geographicAreas())).thenReturn(geographicAreasCondition);

    var aceFlagStatusesCondition = mock(Condition.class);
    when(applicationDataFilterService.getAceStatusCondition(form.aceFlagStatuses())).thenReturn(aceFlagStatusesCondition);

    var assetTypesQueryCondition = mock(Condition.class);
    when(applicationDataFilterService.getAssetTypesQueryCondition(form.assetTypesWithShore())).thenReturn(assetTypesQueryCondition);

    var fieldAssetKeyCondition = mock(Condition.class);
    when(applicationDataFilterService.getFieldCondition(AssetKey.from(form.fieldAssetKey()))).thenReturn(fieldAssetKeyCondition);

    var terminalAssetKeyCondition = mock(Condition.class);
    when(applicationDataFilterService.getTerminalCondition(AssetKey.from(form.terminalAssetKey()))).thenReturn(terminalAssetKeyCondition);

    var caseOfficerCondition = mock(Condition.class);
    doReturn(Optional.of(caseOfficerCondition)).when(bulkAssignCaseOfficerSearchFilterService).getCaseOfficerCondition(form.caseOfficerWuaId());

    var userCondition = mock(Condition.class);
    when(teamService.isRegulatorUser(caseManagerUser)).thenReturn(true);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(userCondition);

    assertThat(bulkAssignCaseOfficerSearchFilterService.getConditions(form, caseManagerUser)).containsExactly(
        operatorCondition,
        geographicAreasCondition,
        aceFlagStatusesCondition,
        assetTypesQueryCondition,
        fieldAssetKeyCondition,
        terminalAssetKeyCondition,
        caseOfficerCondition,
        userCondition,
        CURRENT_CASE_OWNER_IS_EMPTY_OR_IS_CASE_OFFICER_CONDITION
    );
  }

  @ParameterizedTest
  @MethodSource("getCaseOfficerCondition_arguments")
  void getCaseOfficerCondition_withCaseOfficerId_includeApplicationsWithoutCaseOfficer(
      String caseOfficerWuaId,
      Optional<Condition> expectedCondition
  ) {
    var actualCondition = bulkAssignCaseOfficerSearchFilterService.getCaseOfficerCondition(caseOfficerWuaId);
    assertThat(actualCondition).isEqualTo(expectedCondition);
  }

  private static Stream<Arguments> getCaseOfficerCondition_arguments() {
    return Stream.of(
        arguments("1", Optional.of(APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(1))),
        arguments("unassigned", Optional.of(APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull())),
        arguments("invalid string", Optional.empty()),
        arguments(null, Optional.empty())
    );
  }

}
