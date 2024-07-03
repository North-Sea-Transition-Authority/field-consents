package uk.co.nstauthority.fieldconsents.integrationtest;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

public class ApplicationDataItemViewIntegrationTestUtil {

  public static final String APPLICATION_REFERENCE = "PCON/8000/0 (Version 1)";
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

  public static final ServiceUserDetail CAM_USER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(((long) ThreadLocalRandom.current().nextInt()))
      .withForename("Cam User Forename")
      .withSurname("Cam User Surname")
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

  public static final EnergyPortalUserDto CAM_USER_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(CAM_USER_DETAIL.wuaId())
          .withWebUserAccountId(CAM_USER_DETAIL.wuaId())
          .withForename(CAM_USER_DETAIL.forename())
          .withSurname(CAM_USER_DETAIL.surname())
          .build();

  public static final Map<WebUserAccountId, EnergyPortalUserDto> PORTAL_USERS_DTO_MAP = Map.of(
      WebUserAccountId.from(USER_DETAIL), ENERGY_PORTAL_USER_DTO,
      WebUserAccountId.from(CASE_MANAGER_DETAIL), CASE_MANAGER_ENERGY_PORTAL_USER_DTO,
      WebUserAccountId.from(CASE_OFFICER_DETAIL), CASE_OFFICER_ENERGY_PORTAL_USER_DTO,
      WebUserAccountId.from(TECHNICAL_REVIEWER_DETAIL), TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO,
      WebUserAccountId.from(CAM_USER_DETAIL), CAM_USER_ENERGY_PORTAL_USER_DTO);

  public static ApplicationDataItemView.Builder getCompleteApplicationDataItemForSearchBuilder() {
    return ApplicationDataItemView.newBuilder()
        .withApplicationId(1)
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(getConsentDurationString(ConsentLengthType.SHORT_TERM))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag(null)
        .withCaseOfficer("")
        .withCamUser("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withTechnicalReviewOpen(false)
        .withTechnicalReviewDeadline("")
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(false)
        .withConsultationDeadline("")
        .withConsultationFurtherInformationOpen(false)
        .withLicences("P1, P2, P3")
        .withApprovedForIssue(false)
        .withConsentStatus(null);
  }

  public static ApplicationDataItemView.Builder getCompleteApplicationDataItemForWorkAreaIndustryBuilder() {
    return ApplicationDataItemView.newBuilder()
        .withApplicationId(1)
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(getConsentDurationString(ConsentLengthType.SHORT_TERM))
        .withReference("View application")
        .withOperator(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
        .withAsset(FieldTestUtil.FIELD_NAME_1)
        .withGeographicArea(FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
        .withStatus("In progress")
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withAceFlag(null)
        .withCaseOfficer("")
        .withCamUser("")
        .withWithdrawalOpen(false)
        .withTechnicalReviewer("")
        .withTechnicalReviewOpen(null)
        .withTechnicalReviewDeadline(null)
        .withApplicationUpdateOpen(false)
        .withApplicationUpdateDeadline("")
        .withConsultationOpen(null)
        .withConsultationDeadline(null)
        .withConsultationFurtherInformationOpen(null)
        .withLicences("P1, P2, P3")
        .withApprovedForIssue(null)
        .withConsentStatus(null);
  }

  public static String getConsentDurationString(ConsentLengthType consentLengthType) {
    return switch (consentLengthType) {
      case ANNUAL -> "Annual %s".formatted(ANNUAL_CONSENT_YEAR);
      case LONG_TERM -> "Long term %d - %d".formatted(LONG_TERM_START_YEAR, LONG_TERM_END_YEAR);
      case SHORT_TERM -> "Short term %s - %s".formatted(
          DateUtils.format(SHORT_TERM_START_DATE, DateUtils.SHORT_DATE),
          DateUtils.format(SHORT_TERM_END_DATE, DateUtils.SHORT_DATE));
    };
  }
}
