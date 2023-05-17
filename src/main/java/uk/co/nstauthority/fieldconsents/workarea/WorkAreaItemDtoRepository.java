package uk.co.nstauthority.fieldconsents.workarea;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.CONSENT_LENGTHS;

import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;

@Repository
class WorkAreaItemDtoRepository {

  private final DSLContext context;

  @Autowired
  WorkAreaItemDtoRepository(DSLContext context) {
    this.context = context;
  }

  public List<WorkAreaItemDto> runQuery(List<Condition> conditions) {
    // Generates sub query to return application version ids that are applicable for work area, based on conditions.
    // Only allows one Application Version per Application

    // TODO: Need to cater for picking the latest version of the application when updates/revisions are added to the service
    var detailsSubQuery = context.select(APPLICATION_VERSIONS.ID)
        .from(APPLICATIONS)
        .join(APPLICATION_VERSIONS).onKey(APPLICATION_VERSIONS.APPLICATION_ID)
        .where(conditions);

    return context.select(
            APPLICATIONS.ID,
            APPLICATION_VERSIONS.ID,
            APPLICATIONS.TYPE,
            APPLICATIONS.VARIATION_NO,
            APPLICATIONS.APPLICATION_NO,
            APPLICATION_VERSIONS.VERSION_NO,
            APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID,
            APPLICATION_VERSIONS.STATUS,
            APPLICATION_ASSETS.FIELD_ID,
            APPLICATION_ASSETS.CACHED_FIELD_NAME,
            APPLICATION_ASSETS.TERMINAL_ID,
            APPLICATION_ASSETS.CACHED_TERMINAL_NAME,
            CONSENT_LENGTHS.CONSENT_LENGTH,
            CONSENT_LENGTHS.ANNUAL_CONSENT_YEAR,
            CONSENT_LENGTHS.SHORT_TERM_START_DATE,
            CONSENT_LENGTHS.SHORT_TERM_END_DATE,
            CONSENT_LENGTHS.LONG_TERM_START_YEAR,
            CONSENT_LENGTHS.LONG_TERM_END_YEAR,
            APPLICATION_VERSIONS.SUBMITTED_DATE_TIME,
            APPLICATION_VERSIONS.SUBMITTED_BY_WUA_ID
        )
        .from(APPLICATIONS)
        .join(APPLICATION_VERSIONS).onKey(APPLICATION_VERSIONS.APPLICATION_ID)
        .leftJoin(APPLICATION_ASSETS)
        .on(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
            .and(APPLICATION_ASSETS.ASSET_ROLE.eq(AssetRole.PRIMARY.name())))
        .leftJoin(CONSENT_LENGTHS).onKey(CONSENT_LENGTHS.APPLICATION_VERSION_ID)
        .where(APPLICATION_VERSIONS.ID.in(detailsSubQuery))
        .orderBy(greatest(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME, APPLICATION_VERSIONS.CREATED_DATE_TIME).desc())
        .fetchInto(WorkAreaItemDto.class);
  }
}
