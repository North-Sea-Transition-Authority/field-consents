package uk.co.nstauthority.fieldconsents.query;

import static org.jooq.impl.DSL.max;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.IS_ACE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_UPDATES;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_WITHDRAWALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.CONSENT_LENGTHS;

import java.util.List;
import java.util.function.Consumer;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalStatus;
import uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions;

@Service
public class ApplicationDataItemQueryService {

  private final DSLContext context;

  public ApplicationDataItemQueryService(DSLContext context) {
    this.context = context;
  }

  public SelectQuery<Record> getApplicationDataItemsQuery(List<Condition> conditions) {
    var allAppVersionsForAppSubQuery = context.select(APPLICATION_VERSIONS.ID)
        .from(APPLICATION_VERSIONS)
        .where(APPLICATION_VERSIONS.APPLICATION_ID.eq(APPLICATIONS.ID));

    var latestAppVersionForAppSubQuery = context.select(max(APPLICATION_VERSIONS.ID))
        .from(APPLICATION_VERSIONS)
        .where(APPLICATION_VERSIONS.APPLICATION_ID.eq(APPLICATIONS.ID))
        .and(ApplicationVersions.APPLICATION_VERSIONS.STATUS.ne(ApplicationVersionStatus.DELETED.name()));

    // Generates sub query to return application version ids that are applicable for work area and search, based on conditions.
    // Only allows one Application Version per Application
    var detailsSubQuery = context.select(APPLICATION_VERSIONS.ID)
        .from(APPLICATIONS)
        .join(APPLICATION_VERSIONS)
        .onKey(APPLICATION_VERSIONS.APPLICATION_ID)
        .and(APPLICATION_VERSIONS.ID.eq(latestAppVersionForAppSubQuery))
        .where(conditions);

    var applicationDataItemsSelectStatement = context.select(
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
            APPLICATION_VERSIONS.SUBMITTED_BY_WUA_ID,
            APPLICATION_FLAGS.FLAG_VALUE.as("aceFlag"),
            APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID,
            APPLICATION_WITHDRAWALS.WITHDRAWAL_STATUS.isNotNull(),
            APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID,
            APPLICATION_UPDATES.APPLICATION_UPDATE_STATUS.isNotNull(),
            APPLICATION_UPDATES.DEADLINE_DATE_TIME,
            APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull(),
            APPLICATION_CONSULTATIONS.REQUEST_DEADLINE
        )
        .from(APPLICATIONS)
        .join(APPLICATION_VERSIONS).onKey(APPLICATION_VERSIONS.APPLICATION_ID)
        .leftJoin(APPLICATION_ASSETS)
            .on(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
            .and(APPLICATION_ASSETS.ASSET_ROLE.eq(AssetRole.PRIMARY.name())))
        .leftJoin(CONSENT_LENGTHS).onKey(CONSENT_LENGTHS.APPLICATION_VERSION_ID)
        .leftJoin(APPLICATION_FLAGS).onKey(APPLICATION_FLAGS.APPLICATION_VERSION_ID)
            .and(APPLICATION_FLAGS.FLAG_TYPE.eq(IS_ACE_APPLICATION.name()))
        .leftJoin(APPLICATION_WITHDRAWALS)
            .on(APPLICATION_WITHDRAWALS.APPLICATION_VERSION_ID.in(allAppVersionsForAppSubQuery))
            .and(APPLICATION_WITHDRAWALS.WITHDRAWAL_STATUS.eq(WithdrawalStatus.OPEN.name()))
        .leftJoin(APPLICATION_TECHNICAL_REVIEWS)
            .on(APPLICATION_TECHNICAL_REVIEWS.REQUEST_APPLICATION_VERSION_ID.in(allAppVersionsForAppSubQuery))
            .and(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEW_STATUS.eq(TechnicalReviewStatus.OPEN.name()))
        .leftJoin(APPLICATION_UPDATES)
            .on(APPLICATION_UPDATES.APPLICATION_VERSION_ID.in(allAppVersionsForAppSubQuery))
            .and(APPLICATION_UPDATES.APPLICATION_UPDATE_STATUS.eq(ApplicationUpdateStatus.OPEN.name()))
        .leftJoin(APPLICATION_CONSULTATIONS)
            .on(APPLICATION_CONSULTATIONS.REQUEST_APPLICATION_VERSION_ID.in(allAppVersionsForAppSubQuery))
        // TODO FCS-394 need to add join condition below when the status is added otherwise this consultation
        // join could add cardinality when there are more that one consultation on an application (only 1 will
        // be open at any time)
        //    .and(APPLICATION_CONSULTATIONS.CONSULTATION_STATUS.eq(ConsultationStatus.OPEN.name()))
        .where(APPLICATION_VERSIONS.ID.in(detailsSubQuery));
    return applicationDataItemsSelectStatement.getQuery();
  }

  public List<ApplicationDataItemDto> runQueryWithCustom(List<Condition> conditions,
                                                         Consumer<SelectQuery<Record>> selectQueryConsumer) {
    var selectQuery = getApplicationDataItemsQuery(conditions);
    selectQueryConsumer.accept(selectQuery);

    return selectQuery.fetchInto(ApplicationDataItemDto.class);
  }
}
