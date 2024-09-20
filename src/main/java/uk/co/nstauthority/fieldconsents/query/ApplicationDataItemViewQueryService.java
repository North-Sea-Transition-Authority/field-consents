package uk.co.nstauthority.fieldconsents.query;

import static org.jooq.impl.DSL.listAggDistinct;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.select;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.IS_ACE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENTS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENT_BREACHES;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENT_DATA;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENT_ISSUING_APPROVALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATION_FURTHER_INFORMATION;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_UPDATES;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_WITHDRAWALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.BULK_ISSUE_CONSENTS_TASKS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssetLicences.APPLICATION_ASSET_LICENCES;
import static uk.co.nstauthority.fieldconsents.search.SearchController.SEARCH_RESULT_RENDER_LIMIT;

import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.function.Consumer;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalStatus;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@Service
public class ApplicationDataItemViewQueryService {

  public static final Select<Record1<Integer>> LATEST_APP_VERSION_FOR_APP_QUERY =
      select(max(APPLICATION_VERSIONS.ID))
          .from(APPLICATION_VERSIONS)
          .where(APPLICATION_VERSIONS.APPLICATION_ID.eq(APPLICATIONS.ID))
          .and(APPLICATION_VERSIONS.STATUS.ne(ApplicationVersionStatus.DELETED.name()));

  public static final Select<?> APPLICATION_WITHDRAWALS_QUERY =
      select(
          APPLICATION_VERSIONS.APPLICATION_ID,
          APPLICATION_WITHDRAWALS.WITHDRAWAL_STATUS
      )
          .from(APPLICATION_WITHDRAWALS)
          .join(APPLICATION_VERSIONS).on(APPLICATION_VERSIONS.ID.eq(APPLICATION_WITHDRAWALS.APPLICATION_VERSION_ID))
          .where(APPLICATION_WITHDRAWALS.WITHDRAWAL_STATUS.eq(WithdrawalStatus.OPEN.name()));

  public static final Select<?> APPLICATION_TECHNICAL_REVIEWS_QUERY =
      select(
          APPLICATION_VERSIONS.APPLICATION_ID,
          APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID,
          APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEW_STATUS,
          APPLICATION_TECHNICAL_REVIEWS.DEADLINE_DATE_TIME
      )
          .from(APPLICATION_TECHNICAL_REVIEWS)
          .join(APPLICATION_VERSIONS)
              .on(APPLICATION_VERSIONS.ID.eq(APPLICATION_TECHNICAL_REVIEWS.REQUEST_APPLICATION_VERSION_ID))
          .where(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEW_STATUS.eq(TechnicalReviewStatus.OPEN.name()));

  public static final Select<?> APPLICATION_UPDATES_QUERY =
      select(
          APPLICATION_VERSIONS.APPLICATION_ID,
          APPLICATION_UPDATES.APPLICATION_UPDATE_STATUS,
          APPLICATION_UPDATES.DEADLINE_DATE_TIME
      )
          .from(APPLICATION_UPDATES)
          .join(APPLICATION_VERSIONS).on(APPLICATION_VERSIONS.ID.eq(APPLICATION_UPDATES.APPLICATION_VERSION_ID))
          .where(APPLICATION_UPDATES.APPLICATION_UPDATE_STATUS.eq(ApplicationUpdateStatus.OPEN.name()));

  public static final Select<?> APPLICATION_CONSULTATIONS_QUERY =
      select(
          APPLICATION_VERSIONS.APPLICATION_ID,
          APPLICATION_CONSULTATIONS.ID,
          APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID,
          APPLICATION_CONSULTATIONS.REQUEST_DEADLINE,
          APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID,
          APPLICATION_CONSULTATIONS.STATUS
      )
          .from(APPLICATION_CONSULTATIONS)
          .join(APPLICATION_VERSIONS).on(APPLICATION_VERSIONS.ID.eq(APPLICATION_CONSULTATIONS.REQUEST_APPLICATION_VERSION_ID))
          .where(APPLICATION_CONSULTATIONS.STATUS.eq(ConsultationStatus.OPEN.name()));

  public static final Select<Record1<Integer>> APPLICATION_VERSIONS_PENDING_CONSENT_ISSUE_QUERY =
      select(BULK_ISSUE_CONSENTS_TASKS.APPLICATION_VERSION_ID)
          .from(BULK_ISSUE_CONSENTS_TASKS)
          .where(BULK_ISSUE_CONSENTS_TASKS.FINISHED_AT.isNull());

  // Get the CSV for the licences associated to the field when this is the primary application asset
  public static final Select<?> FIELD_LICENCES_QUERY =
      select(
          APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID,
          listAggDistinct(APPLICATION_ASSET_LICENCES.CACHED_LICENCE_REF, ", ")
              .withinGroupOrderBy(APPLICATION_ASSET_LICENCES.CACHED_LICENCE_REF)
              .as("fieldLicences")
      )
          .from(APPLICATION_ASSETS)
          .join(APPLICATION_ASSET_LICENCES).onKey(APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID)
          .where(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
          .groupBy(APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID);

  private final DSLContext context;

  ApplicationDataItemViewQueryService(DSLContext context) {
    this.context = context;
  }

  @Observed(name = "fcs.database.jooq-query", contextualName = "jooq query executed")
  public List<ApplicationDataItemDto> runQueryWithCustom(
      List<Condition> conditions,
      Consumer<SelectQuery<Record>> selectQueryConsumer
  ) {
    var selectQuery = getApplicationDataItemViewsQuery(conditions);
    selectQueryConsumer.accept(selectQuery);

    return selectQuery.fetchInto(ApplicationDataItemDto.class);
  }

  private SelectQuery<Record> getApplicationDataItemViewsQuery(List<Condition> conditions) {
    var applicationDataItemViewsSelectStatement = context.select(
            APPLICATIONS.ID,
            APPLICATION_VERSIONS.ID,
            APPLICATIONS.TYPE,
            APPLICATIONS.VARIATION_NO,
            APPLICATIONS.APPLICATION_NO,
            APPLICATION_VERSIONS.VERSION_NO,
            APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID,
            APPLICATION_VERSIONS.STATUS,
            APPLICATION_ASSETS.ASSET_TYPE,
            APPLICATION_ASSETS.ASSET_ID,
            APPLICATION_ASSETS.CACHED_ASSET_NAME,
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
            APPLICATION_VERSIONS.CAM_WUA_ID,
            APPLICATION_VERSIONS.CURRENT_CASE_OWNER,
            APPLICATION_WITHDRAWALS_QUERY.field(APPLICATION_WITHDRAWALS.WITHDRAWAL_STATUS).isNotNull(),
            APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID),
            APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEW_STATUS).isNotNull(),
            APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.DEADLINE_DATE_TIME),
            APPLICATION_UPDATES_QUERY.field(APPLICATION_UPDATES.APPLICATION_UPDATE_STATUS).isNotNull(),
            APPLICATION_UPDATES_QUERY.field(APPLICATION_UPDATES.DEADLINE_DATE_TIME),
            APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID).isNotNull(),
            APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.REQUEST_DEADLINE),
            APPLICATION_CONSULTATION_FURTHER_INFORMATION.STATUS,
            FIELD_LICENCES_QUERY.field("fieldLicences", String.class),
            APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull(),
            APPLICATION_CONSENT_DATA.CONSENT_START_DATE,
            APPLICATION_CONSENT_DATA.CONSENT_END_DATE,
            APPLICATION_CONSENT_DATA.ID.isNotNull().and(APPLICATION_CONSENTS.ID.isNotNull()).as("consentIssued"),
            APPLICATION_CONSENTS.SUPERSEDED_BY_APPLICATION_CONSENT_ID.isNotNull(),
            APPLICATION_CONSENT_BREACHES.ID.isNotNull()
        )
        .from(APPLICATIONS)
        .join(APPLICATION_VERSIONS).onKey(APPLICATION_VERSIONS.APPLICATION_ID)
        .leftJoin(APPLICATION_ASSETS)
            .onKey(APPLICATION_ASSETS.APPLICATION_VERSION_ID)
            .and(APPLICATION_ASSETS.ASSET_ROLE.eq(AssetRole.PRIMARY.name()))
        .leftJoin(CONSENT_LENGTHS)
            .onKey(CONSENT_LENGTHS.APPLICATION_VERSION_ID)
        .leftJoin(APPLICATION_FLAGS)
            .onKey(APPLICATION_FLAGS.APPLICATION_VERSION_ID)
            .and(APPLICATION_FLAGS.FLAG_TYPE.eq(IS_ACE_APPLICATION.name()))
        .leftJoin(APPLICATION_CONSENT_ISSUING_APPROVALS)
            .onKey(APPLICATION_CONSENT_ISSUING_APPROVALS.APPLICATION_ID)
            .and(APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name()))
        .leftJoin(APPLICATION_CONSENTS)
            .onKey(APPLICATION_CONSENTS.APPLICATION_ID)
        .leftJoin(APPLICATION_CONSENT_DATA)
            .onKey(APPLICATION_CONSENT_DATA.APPLICATION_ID)
        .leftJoin(APPLICATION_CONSENT_BREACHES)
            .onKey(APPLICATION_CONSENT_BREACHES.CONSENT_ID)
        .leftJoin(APPLICATION_WITHDRAWALS_QUERY)
            .on(APPLICATION_WITHDRAWALS_QUERY.field(APPLICATION_VERSIONS.APPLICATION_ID).eq(APPLICATIONS.ID))
        .leftJoin(APPLICATION_TECHNICAL_REVIEWS_QUERY)
            .on(APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_VERSIONS.APPLICATION_ID).eq(APPLICATIONS.ID))
        .leftJoin(APPLICATION_UPDATES_QUERY)
            .on(APPLICATION_UPDATES_QUERY.field(APPLICATION_VERSIONS.APPLICATION_ID).eq(APPLICATIONS.ID))
        .leftJoin(APPLICATION_CONSULTATIONS_QUERY)
            .on(APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_VERSIONS.APPLICATION_ID).eq(APPLICATIONS.ID))
        .leftJoin(APPLICATION_CONSULTATION_FURTHER_INFORMATION)
            .on(APPLICATION_CONSULTATION_FURTHER_INFORMATION.CONSULTATION_ID
                .eq(APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.ID)))
            .and(APPLICATION_CONSULTATION_FURTHER_INFORMATION.STATUS.eq(FurtherInformationStatus.OPEN.name()))
        .leftJoin(FIELD_LICENCES_QUERY)
            .on(FIELD_LICENCES_QUERY.field(APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID).eq(APPLICATION_ASSETS.ID)
            .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name())))
        .where(conditions)
        .and(APPLICATION_VERSIONS.ID.notIn(APPLICATION_VERSIONS_PENDING_CONSENT_ISSUE_QUERY))
        .and(APPLICATION_VERSIONS.ID.eq(LATEST_APP_VERSION_FOR_APP_QUERY))
        .limit(SEARCH_RESULT_RENDER_LIMIT + 1); // +1 because we want to know when more than 300 results are returned
    return applicationDataItemViewsSelectStatement.getQuery();
  }
}
