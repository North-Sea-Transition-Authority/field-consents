package uk.co.nstauthority.fieldconsents.query;

import static org.jooq.impl.DSL.listAggDistinct;
import static org.jooq.impl.DSL.max;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.IS_ACE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENTS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENT_DATA;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSENT_ISSUING_APPROVALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATION_FURTHER_INFORMATION;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_UPDATES;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_WITHDRAWALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssetLicences.APPLICATION_ASSET_LICENCES;

import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
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
import uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions;

@Service
public class ApplicationDataItemViewQueryService {

  private final DSLContext context;

  public ApplicationDataItemViewQueryService(DSLContext context) {
    this.context = context;
  }

  public SelectQuery<Record> getApplicationDataItemViewsQuery(List<Condition> conditions) {
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

    // Get the CSV for the licences associated to the field when this is the primary application asset
    var fieldLicencesQuery = context.select(
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
            APPLICATION_WITHDRAWALS.WITHDRAWAL_STATUS.isNotNull(),
            APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID,
            APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEW_STATUS.isNotNull(),
            APPLICATION_TECHNICAL_REVIEWS.DEADLINE_DATE_TIME,
            APPLICATION_UPDATES.APPLICATION_UPDATE_STATUS.isNotNull(),
            APPLICATION_UPDATES.DEADLINE_DATE_TIME,
            APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull(),
            APPLICATION_CONSULTATIONS.REQUEST_DEADLINE,
            APPLICATION_CONSULTATION_FURTHER_INFORMATION.STATUS,
            fieldLicencesQuery.field("fieldLicences", String.class),
            APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull(),
            APPLICATION_CONSENT_DATA.CONSENT_START_DATE,
            APPLICATION_CONSENT_DATA.CONSENT_END_DATE,
            APPLICATION_CONSENT_DATA.ID.isNotNull()
              .and(APPLICATION_CONSENTS.ID.isNotNull()).as("consentIssued"),
            APPLICATION_CONSENTS.SUPERSEDED_BY_APPLICATION_CONSENT_ID.isNotNull()
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
            .and(APPLICATION_CONSULTATIONS.STATUS.eq(ConsultationStatus.OPEN.name()))
        .leftJoin(APPLICATION_CONSULTATION_FURTHER_INFORMATION)
            .on(APPLICATION_CONSULTATION_FURTHER_INFORMATION.CONSULTATION_ID.eq(APPLICATION_CONSULTATIONS.ID))
            .and(APPLICATION_CONSULTATION_FURTHER_INFORMATION.STATUS.eq(FurtherInformationStatus.OPEN.name()))
        .leftJoin(fieldLicencesQuery)
            .on(Objects.requireNonNull(fieldLicencesQuery.field(APPLICATION_ASSET_LICENCES.APPLICATION_ASSET_ID))
            .eq(APPLICATION_ASSETS.ID)
            .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
            .and(APPLICATION_ASSETS.ASSET_ID.isNotNull()))
        .leftJoin(APPLICATION_CONSENT_ISSUING_APPROVALS)
            .onKey(APPLICATION_CONSENT_ISSUING_APPROVALS.APPLICATION_ID)
            .and(APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name()))
        .leftJoin(APPLICATION_CONSENTS)
            .onKey(APPLICATION_CONSENTS.APPLICATION_ID)
        .leftJoin(APPLICATION_CONSENT_DATA)
            .onKey(APPLICATION_CONSENT_DATA.APPLICATION_ID)
        .where(APPLICATION_VERSIONS.ID.in(detailsSubQuery));
    return applicationDataItemViewsSelectStatement.getQuery();
  }

  public List<ApplicationDataItemDto> runQueryWithCustom(List<Condition> conditions,
                                                         Consumer<SelectQuery<Record>> selectQueryConsumer) {
    return runQueryWithCustom(conditions, selectQueryConsumer, ApplicationDataItemDto.class);
  }

  @Observed(name = "fcs.database.jooq-query", contextualName = "jooq query executed")
  public <X extends ApplicationDataItemDto> List<X> runQueryWithCustom(List<Condition> conditions,
                                                                       Consumer<SelectQuery<Record>> selectQueryConsumer,
                                                                       Class<X> fetchIntoClass) {
    var selectQuery = getApplicationDataItemViewsQuery(conditions);
    selectQueryConsumer.accept(selectQuery);

    return selectQuery.fetchInto(fetchIntoClass);
  }
}
