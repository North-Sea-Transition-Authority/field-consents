package uk.co.nstauthority.fieldconsents.integrationtest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.search.SearchResultItem;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

public class ApplicationDataItemIntegrationTestUtil {

  public static final int ANNUAL_CONSENT_YEAR = LocalDate.now().getYear();
  public static final int LONG_TERM_START_YEAR = LocalDate.now().getYear();
  public static final int LONG_TERM_END_YEAR = LONG_TERM_START_YEAR + 4;
  public static final LocalDate SHORT_TERM_START_DATE = LocalDate.now();
  public static final LocalDate SHORT_TERM_END_DATE = LocalDate.now().plusMonths(6);

  public static final Team REGULATOR_TEAM = TeamTestUtil.Builder()
      .withTeamType(TeamType.REGULATOR)
      .build();

  public static final Team INDUSTRY_TEAM = TeamTestUtil.Builder()
      .withTeamType(TeamType.INDUSTRY)
      .build();

  public static final Team CONSULTATION_TEAM = new TeamTestUtil.TeamBuilder()
      .withId(1)
      .withTeamType(TeamType.OPRED)
      .build();

  public static final ServiceUserDetail USER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(((long) ThreadLocalRandom.current().nextInt()))
      .withForename("Test Forename")
      .withSurname("Test Surname")
      .build();

  public static final ServiceUserDetail CASE_MANAGER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(((long) ThreadLocalRandom.current().nextInt()))
      .withForename("Case Manager Forename")
      .withSurname("Case Manager Surname")
      .build();

  public static final ServiceUserDetail CASE_OFFICER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(((long) ThreadLocalRandom.current().nextInt()))
      .withForename("Case Officer Forename")
      .withSurname("Case Officer Surname")
      .build();

  public static final ServiceUserDetail TECHNICAL_REVIEWER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(((long) ThreadLocalRandom.current().nextInt()))
      .withForename("Technical Reviewer Forename")
      .withSurname("Technical Reviewer Surname")
      .build();

  public static final ServiceUserDetail CONSULTEE_ALLOCATOR_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(((long) ThreadLocalRandom.current().nextInt()))
      .withForename("Consultee Allocator Forename")
      .withSurname("Consultee Allocator Surname")
      .build();

  public static final ServiceUserDetail CONSULTEE_RESPONDER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(((long) ThreadLocalRandom.current().nextInt()))
      .withForename("Consultee Responder Forename")
      .withSurname("Consultee Responder Surname")
      .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(USER_DETAIL.wuaId())
          .withWebUserAccountId(USER_DETAIL.wuaId())
          .withForename(USER_DETAIL.forename())
          .withSurname(USER_DETAIL.surname())
          .build();

  public static final EnergyPortalUserDto CASE_MANAGER_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(CASE_MANAGER_DETAIL.wuaId())
          .withWebUserAccountId(CASE_MANAGER_DETAIL.wuaId())
          .withForename(CASE_MANAGER_DETAIL.forename())
          .withSurname(CASE_MANAGER_DETAIL.surname())
          .build();

  public static final EnergyPortalUserDto CASE_OFFICER_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(CASE_OFFICER_DETAIL.wuaId())
          .withWebUserAccountId(CASE_OFFICER_DETAIL.wuaId())
          .withForename(CASE_OFFICER_DETAIL.forename())
          .withSurname(CASE_OFFICER_DETAIL.surname())
          .build();

  public static final EnergyPortalUserDto TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(TECHNICAL_REVIEWER_DETAIL.wuaId())
          .withWebUserAccountId(TECHNICAL_REVIEWER_DETAIL.wuaId())
          .withForename(TECHNICAL_REVIEWER_DETAIL.forename())
          .withSurname(TECHNICAL_REVIEWER_DETAIL.surname())
          .build();

  public static final EnergyPortalUserDto CONSULTEE_ALLOCATOR_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(CONSULTEE_ALLOCATOR_DETAIL.wuaId())
          .withWebUserAccountId(CONSULTEE_ALLOCATOR_DETAIL.wuaId())
          .withForename(CONSULTEE_ALLOCATOR_DETAIL.forename())
          .withSurname(CONSULTEE_ALLOCATOR_DETAIL.surname())
          .build();

  public static final EnergyPortalUserDto CONSULTEE_RESPONDER_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(CONSULTEE_RESPONDER_DETAIL.wuaId())
          .withWebUserAccountId(CONSULTEE_RESPONDER_DETAIL.wuaId())
          .withForename(CONSULTEE_RESPONDER_DETAIL.forename())
          .withSurname(CONSULTEE_RESPONDER_DETAIL.surname())
          .build();

  public static final Map<WebUserAccountId, EnergyPortalUserDto> PORTAL_USERS_DTO_MAP = Map.of(
      WebUserAccountId.from(USER_DETAIL), ENERGY_PORTAL_USER_DTO,
      WebUserAccountId.from(CASE_MANAGER_DETAIL), CASE_MANAGER_ENERGY_PORTAL_USER_DTO,
      WebUserAccountId.from(CASE_OFFICER_DETAIL), CASE_OFFICER_ENERGY_PORTAL_USER_DTO,
      WebUserAccountId.from(TECHNICAL_REVIEWER_DETAIL), TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO);

  public static SearchResultItem getSearchResultItemProductionSubmittedOfConsentLength(int applicationId,
                                                                                       Instant submittedTimestamp,
                                                                                       ConsentLengthType consentLengthType) {
    var applicationDataItem = ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(getConsentDurationString(consentLengthType))
        .withReference("PCON/10/0 (Version 1)")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus("Submitted")
        .withSubmittedDateTime("Submitted: %s".formatted(
            DateUtils.format(submittedTimestamp, DateUtils.DATE_TIME)))
        .withSubmittedBy("Submitter: %s %s".formatted(
            USER_DETAIL.forename(),
            USER_DETAIL.surname()))
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(false)
        .withConsultationDeadline("")
        .withConsultationFurtherInformationOpen(false)
        .build();
    var licenses = "P1, P2, P3";
    return new SearchResultItem(applicationDataItem, licenses);
  }

  public static SearchResultItem getSearchResultItemForFieldInProgressOfType(int applicationId,
                                                                             ApplicationType applicationType,
                                                                             FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    var applicationDataItem = ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(applicationType.getDisplayName())
        .withDuration("Short term %s - %s".formatted(
            DateUtils.format(SHORT_TERM_START_DATE, DateUtils.SHORT_DATE),
            DateUtils.format(SHORT_TERM_END_DATE, DateUtils.SHORT_DATE)))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(fieldWithOperatorAndLicencesJson.getName())
        .withGeographicArea(fieldWithOperatorAndLicencesJson.getGeographicArea().getDisplayName())
        .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(false)
        .withConsultationDeadline("")
        .withConsultationFurtherInformationOpen(false)
        .build();
    var licenses = fieldWithOperatorAndLicencesJson.getLicencesAsString();
    return new SearchResultItem(applicationDataItem, licenses);
  }

  public static SearchResultItem getSearchResultItemForTerminalInProgressOfType(int applicationId,
                                                                                ApplicationType applicationType,
                                                                                TerminalWithOperatorJson terminal1JsonWithOperator) {
    var applicationDataItem = ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(applicationType.getDisplayName())
        .withDuration("Short term %s - %s".formatted(
            DateUtils.format(SHORT_TERM_START_DATE, DateUtils.SHORT_DATE),
            DateUtils.format(SHORT_TERM_END_DATE, DateUtils.SHORT_DATE)))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(terminal1JsonWithOperator.getName())
        .withGeographicArea("")
        .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(false)
        .withConsultationDeadline("")
        .withConsultationFurtherInformationOpen(false)
        .build();
    return new SearchResultItem(applicationDataItem, null);
  }

  public static SearchResultItem getSearchResultItemInProgressOfTypeAndLength(int applicationId,
                                                                              ApplicationType applicationType,
                                                                              ConsentLengthType consentLengthType) {
    var applicationDataItem = ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(applicationType.getDisplayName())
        .withDuration(getConsentDurationString(consentLengthType))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(false)
        .withConsultationDeadline("")
        .withConsultationFurtherInformationOpen(false)
        .build();
    var licenses = "P1, P2, P3";
    return new SearchResultItem(applicationDataItem, licenses);
  }

  public static ApplicationDataItem getApplicationDataItemProductionSubmittedOfConsentLength(int applicationId,
                                                                                             Instant submittedTimestamp,
                                                                                             ConsentLengthType consentLengthType) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(getConsentDurationString(consentLengthType))
        .withReference("PCON/10/0 (Version 1)")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus("Submitted")
        .withSubmittedDateTime("Submitted: %s".formatted(
            DateUtils.format(submittedTimestamp, DateUtils.DATE_TIME)))
        .withSubmittedBy("Submitter: %s %s".formatted(
            USER_DETAIL.forename(),
            USER_DETAIL.surname()))
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(null)
        .withConsultationDeadline(null)
        .withConsultationFurtherInformationOpen(null)
        .build();
  }

  public static ApplicationDataItem getApplicationDataItemProductionAssignedOfConsentLength(int applicationId,
                                                                                            Instant submittedTimestamp,
                                                                                            ConsentLengthType consentLengthType) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(getConsentDurationString(consentLengthType))
        .withReference("PCON/10/0 (Version 1)")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus("Submitted")
        .withSubmittedDateTime("Submitted: %s".formatted(
            DateUtils.format(submittedTimestamp, DateUtils.DATE_TIME)))
        .withSubmittedBy("Submitter: %s %s".formatted(
            USER_DETAIL.forename(),
            USER_DETAIL.surname()))
        .withAceFlag("")
        .withCaseOfficer("Case officer: %s %s".formatted(
            CASE_OFFICER_DETAIL.forename(),
            CASE_OFFICER_DETAIL.surname()))
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(false)
        .withConsultationDeadline("")
        .withConsultationFurtherInformationOpen(false)
        .build();
  }

  public static ApplicationDataItem getApplicationDataItemProductionWithReviewOpenOfConsentLength(int applicationId,
                                                                                                  Instant submittedTimestamp,
                                                                                                  ConsentLengthType consentLengthType) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(getConsentDurationString(consentLengthType))
        .withReference("PCON/10/0 (Version 1)")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus("Submitted")
        .withSubmittedDateTime("Submitted: %s".formatted(
            DateUtils.format(submittedTimestamp, DateUtils.DATE_TIME)))
        .withSubmittedBy("Submitter: %s %s".formatted(
            USER_DETAIL.forename(),
            USER_DETAIL.surname()))
        .withAceFlag("")
        .withCaseOfficer("Case officer: %s %s".formatted(
            CASE_OFFICER_DETAIL.forename(),
            CASE_OFFICER_DETAIL.surname()))
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("Technical reviewer: %s %s".formatted(
            TECHNICAL_REVIEWER_DETAIL.forename(),
            TECHNICAL_REVIEWER_DETAIL.surname()))
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(false)
        .withConsultationDeadline("")
        .withConsultationFurtherInformationOpen(false)
        .build();
  }

  public static ApplicationDataItem getApplicationDataItemProductionWithConsultationConsentLength(int applicationId,
                                                                                                  Instant submittedTimestamp,
                                                                                                  ConsentLengthType consentLengthType,
                                                                                                  Instant consultationDeadline) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(getConsentDurationString(consentLengthType))
        .withReference("PCON/10/0 (Version 1)")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus("Submitted")
        .withSubmittedDateTime("Submitted: %s".formatted(
            DateUtils.format(submittedTimestamp, DateUtils.DATE_TIME)))
        .withSubmittedBy("Submitter: %s %s".formatted(
            USER_DETAIL.forename(),
            USER_DETAIL.surname()))
        .withAceFlag("")
        .withCaseOfficer("Case officer: %s %s".formatted(
            CASE_OFFICER_DETAIL.forename(),
            CASE_OFFICER_DETAIL.surname()))
        .withWithdrawalOpen(null)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(null)
        .withApplicationUpdateDeadline(null)
        .withConsultationOpen(true)
        .withConsultationDeadline(DateUtils.format(consultationDeadline, DateUtils.DATE_TIME))
        .withConsultationFurtherInformationOpen(false)
        .build();
  }

  public static ApplicationDataItem getApplicationDataItemForFieldInProgressOfType(int applicationId,
                                                                                   ApplicationType applicationType,
                                                                                   FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(applicationType.getDisplayName())
        .withDuration("Short term %s - %s".formatted(
            DateUtils.format(SHORT_TERM_START_DATE, DateUtils.SHORT_DATE),
            DateUtils.format(SHORT_TERM_END_DATE, DateUtils.SHORT_DATE)))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(fieldWithOperatorAndLicencesJson.getName())
        .withGeographicArea(fieldWithOperatorAndLicencesJson.getGeographicArea().getDisplayName())
        .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(null)
        .withConsultationDeadline(null)
        .withConsultationFurtherInformationOpen(null)
        .build();
  }

  public static ApplicationDataItem getApplicationDataItemForTerminalInProgressOfType(int applicationId,
                                                                                      ApplicationType applicationType,
                                                                                      TerminalWithOperatorJson terminal1JsonWithOperator) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(applicationType.getDisplayName())
        .withDuration("Short term %s - %s".formatted(
            DateUtils.format(SHORT_TERM_START_DATE, DateUtils.SHORT_DATE),
            DateUtils.format(SHORT_TERM_END_DATE, DateUtils.SHORT_DATE)))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(terminal1JsonWithOperator.getName())
        .withGeographicArea("")
        .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(null)
        .withConsultationDeadline(null)
        .withConsultationFurtherInformationOpen(null)
        .build();
  }


  public static ApplicationDataItem getApplicationDataItemInProgressOfTypeAndLength(int applicationId,
                                                                                 ApplicationType applicationType,
                                                                                 ConsentLengthType consentLengthType) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType(applicationType.getDisplayName())
        .withDuration(getConsentDurationString(consentLengthType))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag("")
        .withCaseOfficer("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(null)
        .withConsultationDeadline(null)
        .withConsultationFurtherInformationOpen(null)
        .build();
  }

  private static String getConsentDurationString(ConsentLengthType consentLengthType) {
    return switch (consentLengthType) {
      case ANNUAL -> "Annual %s".formatted(ANNUAL_CONSENT_YEAR);
      case LONG_TERM -> "Long term %d - %d".formatted(LONG_TERM_START_YEAR, LONG_TERM_END_YEAR);
      case SHORT_TERM -> "Short term %s - %s".formatted(
          DateUtils.format(SHORT_TERM_START_DATE, DateUtils.SHORT_DATE),
          DateUtils.format(SHORT_TERM_END_DATE, DateUtils.SHORT_DATE));
    };
  }
}
