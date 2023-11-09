package uk.co.nstauthority.fieldconsents.search;

import static org.jooq.impl.DSL.greatest;
import static org.jooq.impl.DSL.listAggDistinct;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssetLicences.APPLICATION_ASSET_LICENCES;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.List;
import java.util.Objects;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemQueryService;

@Service
public class SearchResultItemDtoService {

  private final DSLContext context;
  private final ApplicationDataItemQueryService applicationDataItemQueryService;

  SearchResultItemDtoService(DSLContext context,
                             ApplicationDataItemQueryService applicationDataItemQueryService) {
    this.context = context;
    this.applicationDataItemQueryService = applicationDataItemQueryService;
  }

  List<SearchResultItemDto> runSearchQuery(List<Condition> conditions) {
    // Get the CSV for the licences associated to the field when this is the primary application asset
    var fieldLicencesQuery =
        context.select(
            APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID,
            listAggDistinct(APPLICATION_ASSET_LICENCES.CACHED_LICENCE_REF, ", ")
                .withinGroupOrderBy(APPLICATION_ASSET_LICENCES.CACHED_LICENCE_REF)
                .as("fieldLicences")
           )
        .from(APPLICATION_ASSETS)
        .join(APPLICATION_ASSET_LICENCES).onKey(APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID)
        .where(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
        .and(APPLICATION_ASSETS.ASSET_ID.isNotNull())
        .groupBy(APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID);

    return applicationDataItemQueryService.runQueryWithCustom(conditions, selectQuery -> {
      selectQuery.addSelect(
          fieldLicencesQuery.field("fieldLicences"));
      selectQuery.addJoin(fieldLicencesQuery, JoinType.LEFT_OUTER_JOIN,
          Objects.requireNonNull(fieldLicencesQuery.field(APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID))
              .eq(APPLICATION_ASSETS.ID)
              .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
              .and(APPLICATION_ASSETS.ASSET_ID.isNotNull()));
      selectQuery.addOrderBy(greatest(
          APPLICATION_VERSIONS.SUBMITTED_DATE_TIME,
          APPLICATION_VERSIONS.CREATED_DATE_TIME).desc());
    }, SearchResultItemDto.class);
  }
}
