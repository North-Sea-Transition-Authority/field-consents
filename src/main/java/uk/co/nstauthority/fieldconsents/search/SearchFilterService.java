package uk.co.nstauthority.fieldconsents.search;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsultations.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationFlags.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.FIELD_LOOKUP_PURPOSE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class SearchFilterService {

  public static final String TERMINAL_LOOKUP_PURPOSE = "Lookup terminal for search data";

  private final DSLContext context;
  private final FieldService fieldService;
  private final TerminalService terminalService;
  private final ApplicationDataFilterService applicationDataFilterService;
  private final ApplicationAssetService applicationAssetService;

  public SearchFilterService(DSLContext context,
                             FieldService fieldService,
                             TerminalService terminalService,
                             ApplicationDataFilterService applicationDataFilterService,
                             ApplicationAssetService applicationAssetService) {
    this.context = context;
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationDataFilterService = applicationDataFilterService;
    this.applicationAssetService = applicationAssetService;
  }

  List<Condition> getConditions(SearchFilterForm form, TeamType teamType) {
    var applicationDataFilterConditions = applicationDataFilterService.getConditions(form);
    List<Condition> searchFilterConditions = new ArrayList<>(applicationDataFilterConditions);

    if (TeamType.REGULATOR.equals(teamType) || TeamType.OPRED.equals(teamType)) {
      Optional.ofNullable(form.getAceFlagStatuses())
          .map(this::getAceStatusCondition)
          .ifPresent(searchFilterConditions::add);
    }

    if (Objects.nonNull(form.getFieldAssetKey())) {
      var fieldJson = fieldService.getField(AssetKey.from(form.getFieldAssetKey()).assetId(), FIELD_LOOKUP_PURPOSE);
      searchFilterConditions.add(getFieldCondition(fieldJson));
    }

    if (Objects.nonNull(form.getTerminalAssetKey())) {
      var terminalJson = terminalService.getTerminal(
          AssetKey.from(form.getTerminalAssetKey()).assetId(),
          TERMINAL_LOOKUP_PURPOSE
      );
      searchFilterConditions.add(getTerminalCondition(terminalJson));
    }

    var consentStartYear = form.getConsentStartYear();
    if (Objects.nonNull(consentStartYear)) {
      if (isNumeric(consentStartYear)) {
        searchFilterConditions.add(this.getConsentStartYearQueryCondition(Integer.parseInt(consentStartYear)));
      } else {
        searchFilterConditions.add(falseCondition());
      }
    }

    var licenceReference = form.getLicenceReference();
    if (Objects.nonNull(licenceReference)) {
      List<Integer> fieldIdsWithMatchingLicence = getFieldIdsWithMatchingLicence(licenceReference);

      if (!fieldIdsWithMatchingLicence.isEmpty()) {
        searchFilterConditions.add(getLicenceReferenceQueryCondition(fieldIdsWithMatchingLicence));
      } else {
        searchFilterConditions.add(falseCondition());
      }
    }

    if (TeamType.OPRED.equals(teamType)) {
      searchFilterConditions.add(getConsultationsCondition());
    }

    return searchFilterConditions;
  }

  private Condition getFieldCondition(FieldJson fieldJson) {
    return exists(context.select(APPLICATION_ASSETS.FIELD_ID)
        .from(APPLICATION_ASSETS)
        .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
            .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
            .and(APPLICATION_ASSETS.FIELD_ID.eq(fieldJson.getId()))));
  }

  private Condition getTerminalCondition(TerminalJson terminalJson) {
    return exists(context.select(APPLICATION_ASSETS.TERMINAL_ID)
        .from(APPLICATION_ASSETS)
        .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
            .and(APPLICATION_ASSETS.ASSET_ROLE.eq(AssetRole.PRIMARY.name()))
            .and(APPLICATION_ASSETS.TERMINAL_ID.eq(terminalJson.getId()))));
  }

  private Condition getAceStatusCondition(List<AceFlagStatus> aceFlagStatuses) {
    var aceFlagIsAceApplicationValues = aceFlagStatuses
        .stream()
        .map(AceFlagStatus::isAceApplication)
        .toList();
    return exists(context.select(APPLICATION_FLAGS.FLAG_VALUE)
        .from(APPLICATION_FLAGS)
        .where(APPLICATION_FLAGS.FLAG_TYPE.eq(ApplicationFlagType.IS_ACE_APPLICATION.name())
            .and(APPLICATION_FLAGS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID))
            .and(APPLICATION_FLAGS.FLAG_VALUE.in(aceFlagIsAceApplicationValues)))
    );
  }

  private Condition getConsultationsCondition() {
    return exists(context.select(APPLICATION_CONSULTATIONS.ID)
        .from(APPLICATION_CONSULTATIONS)
        .join(APPLICATION_VERSIONS)
          .onKey(APPLICATION_CONSULTATIONS.REQUEST_APPLICATION_VERSION_ID)
        .where(APPLICATION_VERSIONS.APPLICATION_ID.eq(APPLICATIONS.ID)));
  }

  private Condition getConsentStartYearQueryCondition(Integer consentStartYear) {
    return
        coalesce(
            year(CONSENT_LENGTHS.SHORT_TERM_START_DATE),
            CONSENT_LENGTHS.LONG_TERM_START_YEAR,
            CONSENT_LENGTHS.ANNUAL_CONSENT_YEAR
        ).eq(consentStartYear);
  }

  private Condition getLicenceReferenceQueryCondition(List<Integer> fieldIdsWithMatchingLicence) {
    return
        exists(context.select(APPLICATION_ASSETS.FIELD_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.FIELD_ID.in(fieldIdsWithMatchingLicence))));
  }

  private List<Integer> getFieldIdsWithMatchingLicence(String licenceReference) {
    var primaryAndSecondaryFieldIds = applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()
        .stream()
        .map(ApplicationAsset::getFieldId)
        .toList();

    if (primaryAndSecondaryFieldIds.isEmpty()) {
      return Collections.emptyList();
    }

    return fieldService
        .findFieldsWithOperatorAndLicences(primaryAndSecondaryFieldIds, FIELD_LOOKUP_PURPOSE)
        .stream()
        .filter(fieldJson -> fieldJson.getLicenceReferences().contains(licenceReference))
        .map(FieldJson::getId)
        .toList();
  }
}
