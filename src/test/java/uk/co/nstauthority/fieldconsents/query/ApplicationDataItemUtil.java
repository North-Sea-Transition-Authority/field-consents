package uk.co.nstauthority.fieldconsents.query;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_NUMBER;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CASE_OFFICER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.LONG_TERM_END_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.LONG_TERM_START_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import java.time.Instant;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.search.SearchResultItem;
import uk.co.nstauthority.fieldconsents.search.SearchResultItemDto;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

public class ApplicationDataItemUtil {

  private static final String SUBMITTED_DATE_TIME = "1 Oct 2022 12:00";

  public static final Long TECHNICAL_REVIEWER_WUA_ID = 99L;

  public static EnergyPortalUserDto submitter = EnergyPortalUserDtoTestUtil.Builder().build();
  public static EnergyPortalUserDto caseOfficer = EnergyPortalUserDtoTestUtil.Builder()
      .withWebUserAccountId(CASE_OFFICER_WUA_ID)
      .build();
  public static EnergyPortalUserDto technicalReviewer = EnergyPortalUserDtoTestUtil.Builder()
      .withWebUserAccountId(TECHNICAL_REVIEWER_WUA_ID)
      .build();

  public static Map<Long, EnergyPortalUserDto> portalUserDtosMap = Map.of(
      submitter.webUserAccountId(), submitter,
      caseOfficer.webUserAccountId(), caseOfficer,
      technicalReviewer.webUserAccountId(), technicalReviewer
    );

  public static ApplicationDataItemDto getApplicationDataItemDtoForAnnualProductionInProgressForField() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.PRODUCTION,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.IN_PROGRESS,
        FIELD_ID_1,
        FIELD_NAME_1,
        null,
        null,
        ConsentLengthType.ANNUAL,
        ANNUAL_CONSENT_YEAR,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForProductionInProgressForFieldNoDuration() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.PRODUCTION,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.IN_PROGRESS,
        FIELD_ID_1,
        FIELD_NAME_1,
        null,
        null,
        null,
        ANNUAL_CONSENT_YEAR,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForShortVentSubmittedForTerminal() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.VENT,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        null,
        null,
        TERMINAL_ID_1,
        TERMINAL_NAME_1,
        ConsentLengthType.SHORT_TERM,
        null,
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE,
        null,
        null,
        Instant.now(),
        USER_WUA_ID,
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForShortVentVersion2InProgressForTerminal() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.VENT,
        0,
        APPLICATION_NO,
        2,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.IN_PROGRESS,
        null,
        null,
        TERMINAL_ID_1,
        TERMINAL_NAME_1,
        ConsentLengthType.SHORT_TERM,
        null,
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE,
        null,
        null,
        Instant.now(),
        USER_WUA_ID,
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForShortVentAssignedForTerminal() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.VENT,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        null,
        null,
        TERMINAL_ID_1,
        TERMINAL_NAME_1,
        ConsentLengthType.SHORT_TERM,
        null,
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE,
        null,
        null,
        Instant.now(),
        USER_WUA_ID,
        null,
        CASE_OFFICER_WUA_ID,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForLongFlareSubmittedForTerminal() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.FLARE,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        null,
        null,
        TERMINAL_ID_1,
        TERMINAL_NAME_1,
        ConsentLengthType.LONG_TERM,
        null,
        null,
        null,
        LONG_TERM_START_YEAR,
        LONG_TERM_END_YEAR,
        Instant.now(),
        USER_WUA_ID,
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForLongFlareSubmittedForField() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.FLARE,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        null,
        null,
        FIELD_ID_1,
        FIELD_NAME_1,
        ConsentLengthType.LONG_TERM,
        null,
        null,
        null,
        LONG_TERM_START_YEAR,
        LONG_TERM_END_YEAR,
        Instant.now(),
        USER_WUA_ID,
        true,
        null,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForAnnualFlareSubmittedForFieldConsultationOpen() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.FLARE,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        FIELD_ID_1,
        FIELD_NAME_1,
        null,
        null,
        ConsentLengthType.ANNUAL,
        2024,
        null,
        null,
        null,
        null,
        Instant.now(),
        USER_WUA_ID,
        true,
        null,
        false,
        null,
        false,
        null,
        true,
        Instant.now().plusSeconds(2*60*60));
  }

  public static SearchResultItemDto getSearchResultItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest() {
    return new SearchResultItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.FLARE,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        null,
        null,
        TERMINAL_ID_1,
        TERMINAL_NAME_1,
        ConsentLengthType.LONG_TERM,
        null,
        null,
        null,
        LONG_TERM_START_YEAR,
        LONG_TERM_END_YEAR,
        Instant.now(),
        USER_WUA_ID,
        null,
        null,
        true,
        null,
        false,
        null,
        false,
        null,
        "P1, P2, P3"
    );
  }

  public static SearchResultItemDto getSearchResultItemDtoForAnnualProductionInProgressForField() {
    return new SearchResultItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.PRODUCTION,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.IN_PROGRESS,
        FIELD_ID_1,
        FIELD_NAME_1,
        null,
        null,
        ConsentLengthType.ANNUAL,
        ANNUAL_CONSENT_YEAR,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        "P1, P2, P3"
    );
  }

  public static ApplicationDataItem getApplicationDataItemFromDto(ApplicationDataItemDto applicationDataItemDto, TeamType teamType) {
    return new ApplicationDataItem(
        applicationDataItemDto.getApplicationId(),
        applicationDataItemDto.getType().getDisplayName(),
        getDuration(applicationDataItemDto),
        getCaseReference(applicationDataItemDto),
        getOperator(applicationDataItemDto),
        getAsset(applicationDataItemDto),
        getGeographicArea(applicationDataItemDto),
        applicationDataItemDto.getStatus().getDisplayName(),
        getSubmittedDateTime(applicationDataItemDto),
        getSubmitter(applicationDataItemDto),
        getAceFlag(applicationDataItemDto),
        getCaseOfficer(applicationDataItemDto),
        applicationDataItemDto.getWithdrawalOpen(),
        getTechnicalReviewer(applicationDataItemDto, teamType),
        applicationDataItemDto.getApplicationUpdateOpen(),
        getApplicationUpdateDeadline(applicationDataItemDto),
        applicationDataItemDto.getConsultationOpen(),
        getConsultationDeadline(applicationDataItemDto));
  }

  public static SearchResultItem getSearchResultItemFromDto(SearchResultItemDto searchResultItemDto, TeamType teamType) {
    return new SearchResultItem(
        searchResultItemDto.getApplicationId(),
        searchResultItemDto.getType().getDisplayName(),
        getDuration(searchResultItemDto),
        getCaseReference(searchResultItemDto),
        getOperator(searchResultItemDto),
        getAsset(searchResultItemDto),
        getGeographicArea(searchResultItemDto),
        searchResultItemDto.getStatus().getDisplayName(),
        getSubmittedDateTime(searchResultItemDto),
        getSubmitter(searchResultItemDto),
        getAceFlag(searchResultItemDto),
        getCaseOfficer(searchResultItemDto),
        searchResultItemDto.getWithdrawalOpen(),
        getTechnicalReviewer(searchResultItemDto, teamType),
        searchResultItemDto.getApplicationUpdateOpen(),
        getApplicationUpdateDeadline(searchResultItemDto),
        searchResultItemDto.getConsultationOpen(),
        getConsultationDeadline(searchResultItemDto),
        searchResultItemDto.getLicences()
    );
  }

  public static String getAceFlag(ApplicationDataItemDto applicationDataItemDto) {
    return Boolean.TRUE.equals(applicationDataItemDto.getAceFlag()) ? "ACE" : "";
  }

  private static String getSubmittedDateTime(ApplicationDataItemDto applicationDataItemDto) {
    return applicationDataItemDto.getStatus().equals(ApplicationVersionStatus.SUBMITTED)
        ? "Submitted: %s".formatted(DateUtils.format(applicationDataItemDto.getSubmittedDateTime(), DateUtils.DATE_TIME))
        : "";
  }

  public static String getGeographicArea(ApplicationDataItemDto applicationDataItemDto) {
    return applicationDataItemDto.getFieldId() != null
        ? field1JsonWithOperator.getGeographicArea().getDisplayName()
        : "";
  }

  private static String getAsset(ApplicationDataItemDto applicationDataItemDto) {
    return applicationDataItemDto.getFieldId() != null
        ? applicationDataItemDto.getFieldName()
        : applicationDataItemDto.getTerminalName();
  }

  public static String getOperator(ApplicationDataItemDto applicationDataItemDto) {
    return applicationDataItemDto.getFieldId() != null
        ? field1JsonWithOperator.getOperatorName()
        : terminal1JsonWithOperator.getOperatorName();
  }

  public static String getDuration(ApplicationDataItemDto applicationDataItemDto) {
    if (applicationDataItemDto.getDuration() != null) {
      var consentDurationString = applicationDataItemDto.getDuration().getShortDisplayName();

      switch (applicationDataItemDto.getDuration()) {
        case ANNUAL -> {
          return "%s %s".formatted(consentDurationString, applicationDataItemDto.getConsentYear());
        }
        case LONG_TERM -> {
          return "%s %d - %d".formatted(consentDurationString, applicationDataItemDto.getLongTermStartYear(), applicationDataItemDto.getLongTermEndYear());
        }

        case SHORT_TERM -> {
          return "%s %s - %s".formatted(consentDurationString,
              DateUtils.format(applicationDataItemDto.getShortTermStartDate(), DateUtils.SHORT_DATE),
              DateUtils.format(applicationDataItemDto.getShortTermEndDate(), DateUtils.SHORT_DATE)
          );
        }
      }
    }

    return "";
  }

  public static String getSubmitter(ApplicationDataItemDto applicationDataItemDto) {
    if (applicationDataItemDto.getStatus().equals(ApplicationVersionStatus.SUBMITTED)) {
      var submitter = EnergyPortalUserDtoTestUtil.Builder().build();
      return "Submitter: %s".formatted(submitter.displayName());
    }

    return "";
  }

  public static String getCaseOfficer(ApplicationDataItemDto applicationDataItemDto) {
    if (applicationDataItemDto.getCaseOfficerWuaId() != null) {
      var caseOfficer = EnergyPortalUserDtoTestUtil.Builder().build();
      return "Case officer: %s".formatted(caseOfficer.displayName());
    }

    return "";
  }

  public static String getTechnicalReviewer(ApplicationDataItemDto applicationDataItemDto, TeamType teamType) {
    if (applicationDataItemDto.getTechnicalReviewerWuaId() != null
        && TeamType.REGULATOR.equals(teamType)) {
      var technicalReviewer = EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(applicationDataItemDto.getTechnicalReviewerWuaId())
          .build();
      return "Technical reviewer: %s".formatted(technicalReviewer.displayName());
    }

    return "";
  }

  public static String getCaseReference(ApplicationDataItemDto applicationDataItemDto) {
    if (applicationDataItemDto.getStatus().equals(ApplicationVersionStatus.IN_PROGRESS)
        && applicationDataItemDto.getVersionNo() == 1) {
      return "Resume application";
    } else if (applicationDataItemDto.getStatus().equals(ApplicationVersionStatus.IN_PROGRESS)
        && applicationDataItemDto.getVersionNo() > 1) {
      return "Resume %s".formatted(generateApplicationReference(applicationDataItemDto));
    }

    return generateApplicationReference(applicationDataItemDto);
  }

  private static String generateApplicationReference(ApplicationDataItemDto applicationDataItemDto) {
    return "%s/%d/%d (Version %d)".formatted(
        applicationDataItemDto.getType().getReferenceMnemonic(),
        applicationDataItemDto.getApplicationNo(),
        applicationDataItemDto.getVariationNo(),
        applicationDataItemDto.getVersionNo()
    );
  }

  private static String getApplicationUpdateDeadline(ApplicationDataItemDto applicationDataItemDto) {
    return applicationDataItemDto.getApplicationUpdateOpen()
        ? DateUtils.format(applicationDataItemDto.getApplicationUpdateDeadline(), DateUtils.DATE_TIME)
        : "";
  }

  private static String getConsultationDeadline(ApplicationDataItemDto applicationDataItemDto) {
    return applicationDataItemDto.getConsultationOpen()
        ? DateUtils.format(applicationDataItemDto.getConsultationDeadline(), DateUtils.DATE_TIME)
        : "";
  }

  public static ApplicationDataItem getApplicationDataItem() {
    return new ApplicationDataItem(
        APPLICATION_ID,
        ApplicationType.FLARE.getDisplayName(),
        ConsentLengthType.ANNUAL.getDisplayName(),
        APPLICATION_REFERENCE,
        CACHED_PRIMARY_OPERATOR_NAME_1,
        FIELD_NAME_1,
        FIELD_1_GEOGRAPHIC_AREA.getDisplayName(),
        ApplicationVersionStatus.SUBMITTED.getDisplayName(),
        SUBMITTED_DATE_TIME,
        String.valueOf(USER_WUA_ID),
        "",
        "",
        false,
        "",
        false,
        "",
        false,
        ""
    );
  }

  public static SearchResultItem getSearchResultItem() {
    return new SearchResultItem(
        APPLICATION_ID,
        ApplicationType.FLARE.getDisplayName(),
        ConsentLengthType.ANNUAL.getDisplayName(),
        APPLICATION_REFERENCE,
        CACHED_PRIMARY_OPERATOR_NAME_1,
        FIELD_NAME_1,
        FIELD_1_GEOGRAPHIC_AREA.getDisplayName(),
        ApplicationVersionStatus.SUBMITTED.getDisplayName(),
        SUBMITTED_DATE_TIME,
        String.valueOf(USER_WUA_ID),
        "",
        "",
        false,
        "",
        false,
        "",
        false,
        "",
        "P1, P2, P3"
    );
  }
}
