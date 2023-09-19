package uk.co.nstauthority.fieldconsents.search;

import static org.jooq.impl.DSL.exists;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationFlags.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.FIELD_LOOKUP_PURPOSE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;

@Service
public class SearchFilterService {

  public static final String TERMINAL_LOOKUP_PURPOSE = "Lookup terminal for search data";

  private final DSLContext context;
  private final FieldService fieldService;
  private final TerminalService terminalService;
  private final ApplicationDataFilterService applicationDataFilterService;

  public SearchFilterService(DSLContext context,
                             FieldService fieldService,
                             TerminalService terminalService,
                             ApplicationDataFilterService applicationDataFilterService) {
    this.context = context;
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationDataFilterService = applicationDataFilterService;
  }

  List<Condition> getConditions(SearchFilterForm form) {
    var applicationDataFilterConditions = applicationDataFilterService.getConditions(form);
    List<Condition> searchFilterConditions = new ArrayList<>(applicationDataFilterConditions);

    Optional.ofNullable(form.getAceFlagStatuses())
        .map(this::getAceStatusCondition)
        .ifPresent(searchFilterConditions::add);

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
}
