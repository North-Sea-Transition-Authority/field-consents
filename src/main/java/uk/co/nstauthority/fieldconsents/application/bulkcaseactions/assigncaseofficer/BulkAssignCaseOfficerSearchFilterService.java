package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.UNASSIGNED;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
class BulkAssignCaseOfficerSearchFilterService {

  public static final String FIELD_LOOKUP_PURPOSE = "Lookup field for bulk action application data";
  public static final String ORGANISATION_UNIT_LOOKUP_PURPOSE = "Lookup organisation unit for bulk action application data";

  private final ApplicationDataFilterFormService filterFormService;
  private final ApplicationDataFilterService applicationDataFilterService;
  private final TeamService teamService;
  private final CaseAssignmentService caseAssignmentService;

  BulkAssignCaseOfficerSearchFilterService(
      ApplicationDataFilterFormService filterFormService,
      ApplicationDataFilterService applicationDataFilterService,
      TeamService teamService,
      CaseAssignmentService caseAssignmentService
  ) {
    this.filterFormService = filterFormService;
    this.applicationDataFilterService = applicationDataFilterService;
    this.teamService = teamService;
    this.caseAssignmentService = caseAssignmentService;
  }

  RestSearchItem getPrefilledOrganisation(Integer operatorId) {
    return filterFormService.getPrefilledOrganisation(operatorId, ORGANISATION_UNIT_LOOKUP_PURPOSE);
  }

  RestSearchItem getPrefilledAsset(String assetKey) {
    return filterFormService.getPrefilledAsset(assetKey);
  }

  Map<String, String> getCaseOfficerDisplayOptions() {
    var caseOfficerDisplayOptions = new LinkedHashMap<String, String>();
    caseOfficerDisplayOptions.put(UNASSIGNED, "Unassigned");

    for (var energyPortalUser : caseAssignmentService.getCurrentCaseOfficers()) {
      caseOfficerDisplayOptions.put(energyPortalUser.webUserAccountId().toString(), energyPortalUser.displayName());
    }

    return caseOfficerDisplayOptions;
  }

  List<Condition> getConditions(BulkAssignCaseOfficerSearchFiltersForm filtersForm, ServiceUserDetail user) {
    var conditions = new ArrayList<Condition>();

    Optional.ofNullable(filtersForm.operatorId())
        .map(applicationDataFilterService::getOperatorCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.geographicAreas())
        .filter(list -> !list.isEmpty())
        .map(applicationDataFilterService::getGeographicAreasQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.aceFlagStatuses())
        .filter(list -> !list.isEmpty())
        .map(applicationDataFilterService::getAceStatusCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.assetTypesWithShore())
        .filter(list -> !list.isEmpty())
        .map(applicationDataFilterService::getAssetTypesQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.fieldAssetKey())
        .flatMap(AssetKey::parse)
        .map(applicationDataFilterService::getFieldCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.terminalAssetKey())
        .flatMap(AssetKey::parse)
        .map(applicationDataFilterService::getTerminalCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.caseOfficerWuaId())
        .flatMap(this::getCaseOfficerCondition)
        .ifPresent(conditions::add);

    getUserCondition(user).ifPresent(conditions::add);

    conditions.add(getCurrentCaseOwnerIsEmptyOrIsCaseOfficerCondition());

    return conditions;
  }

  private Condition getCurrentCaseOwnerIsEmptyOrIsCaseOfficerCondition() {
    return APPLICATION_VERSIONS.CURRENT_CASE_OWNER.isNull()
        .or(APPLICATION_VERSIONS.CURRENT_CASE_OWNER.eq(CASE_OFFICER.name()));
  }

  private Optional<Condition> getUserCondition(ServiceUserDetail user) {
    if (!teamService.isRegulatorUser(user)) {
      return Optional.empty();
    }

    return Optional.of(applicationDataFilterService.getSubmittedApplicationStatusCondition());
  }

  public Optional<Condition> getCaseOfficerCondition(String caseOfficerWuaId) {
    if (UNASSIGNED.equals(caseOfficerWuaId)) {
      return Optional.of(APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull());
    }

    try {
      return Optional.of(APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(Integer.parseInt(caseOfficerWuaId)));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

}
