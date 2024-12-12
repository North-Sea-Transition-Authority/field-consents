package uk.co.nstauthority.fieldconsents.search;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENT_DATA;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsultations.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class SearchFilterService {

  private final DSLContext context;
  private final ApplicationDataFilterService applicationDataFilterService;
  private final OrganisationGroupQueryService organisationGroupQueryService;

  SearchFilterService(
      DSLContext context,
      ApplicationDataFilterService applicationDataFilterService,
      OrganisationGroupQueryService organisationGroupQueryService
  ) {
    this.context = context;
    this.applicationDataFilterService = applicationDataFilterService;
    this.organisationGroupQueryService = organisationGroupQueryService;
  }

  List<Condition> getConditions(SearchFilterForm form, TeamType teamType) {
    var applicationDataFilterConditions = applicationDataFilterService.getConditions(form);
    List<Condition> searchFilterConditions = new ArrayList<>(applicationDataFilterConditions);

    if (TeamType.REGULATOR.equals(teamType) || TeamType.CONSULTEE.equals(teamType)) {
      Optional.ofNullable(form.getAceFlagStatuses())
          .map(applicationDataFilterService::getAceStatusCondition)
          .ifPresent(searchFilterConditions::add);
    }

    if (TeamType.REGULATOR.equals(teamType) && Boolean.TRUE.equals(form.getApprovedForIssue())) {
      searchFilterConditions.add(applicationDataFilterService.getApprovedForIssueCondition());
    }

    Optional.ofNullable(form.getFieldAssetKey())
        .flatMap(AssetKey::parse)
        .map(applicationDataFilterService::getFieldCondition)
        .ifPresent(searchFilterConditions::add);

    Optional.ofNullable(form.getTerminalAssetKey())
        .flatMap(AssetKey::parse)
        .map(applicationDataFilterService::getTerminalCondition)
        .ifPresent(searchFilterConditions::add);

    var consentStartYear = form.getConsentStartYear();
    if (consentStartYear != null) {
      if (isNumeric(consentStartYear)) {
        searchFilterConditions.add(this.getConsentStartYearQueryCondition(Integer.parseInt(consentStartYear)));
      } else {
        searchFilterConditions.add(falseCondition());
      }
    }

    var consentEndYear = form.getConsentEndYear();
    if (consentEndYear != null) {
      if (isNumeric(consentEndYear)) {
        searchFilterConditions.add(this.getConsentEndYearQueryCondition(Integer.parseInt(consentEndYear)));
      } else {
        searchFilterConditions.add(falseCondition());
      }
    }

    if (TeamType.CONSULTEE.equals(teamType)) {
      searchFilterConditions.add(getConsultationsCondition());
    }

    Optional.ofNullable(form.getOperatorGroupId())
        .map(this::getOperatorGroupCondition)
        .ifPresent(searchFilterConditions::add);

    return searchFilterConditions;
  }

  private Condition getOperatorGroupCondition(Integer organisationGroupId) {
    var organisationUnitJsonById = organisationGroupQueryService
        .getOrganisationUnitsByOrganisationGroupIds(List.of(organisationGroupId))
        .stream()
        .collect(Collectors.toMap(
            OrganisationUnitJson::organisationUnitId,
            Function.identity()
        ));

    if (organisationUnitJsonById.isEmpty()) {
      return falseCondition();
    }

    return APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitJsonById.keySet());
  }

  private Condition getConsultationsCondition() {
    return exists(context.select(APPLICATION_CONSULTATIONS.ID)
        .from(APPLICATION_CONSULTATIONS)
        .join(APPLICATION_VERSIONS)
          .onKey(APPLICATION_CONSULTATIONS.REQUEST_APPLICATION_VERSION_ID)
        .where(APPLICATION_VERSIONS.APPLICATION_ID.eq(APPLICATIONS.ID)));
  }

  private Condition getConsentStartYearQueryCondition(Integer consentStartYear) {
    return year(APPLICATION_CONSENT_DATA.CONSENT_START_DATE).eq(consentStartYear);
  }

  private Condition getConsentEndYearQueryCondition(Integer consentEndYear) {
    return year(APPLICATION_CONSENT_DATA.CONSENT_END_DATE).eq(consentEndYear);
  }
}
