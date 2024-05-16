package uk.co.nstauthority.fieldconsents.query;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_NUMBER;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CAM_USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CASE_OFFICER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CASE_USER_WUA_ID;
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
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_NAME_1;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

public class ApplicationDataItemUtil {

  private static final String SUBMITTED_DATE_TIME = "1 Oct 2022 12:00";

  public static final Long TECHNICAL_REVIEWER_WUA_ID = 99L;

  public static EnergyPortalUserDto viewer = EnergyPortalUserDtoTestUtil.Builder().build();
  public static EnergyPortalUserDto submitter = EnergyPortalUserDtoTestUtil.Builder().build();
  public static EnergyPortalUserDto caseOfficer = EnergyPortalUserDtoTestUtil.Builder()
      .withWebUserAccountId(CASE_OFFICER_WUA_ID)
      .build();
  public static EnergyPortalUserDto technicalReviewer = EnergyPortalUserDtoTestUtil.Builder()
      .withWebUserAccountId(TECHNICAL_REVIEWER_WUA_ID)
      .build();
  public static EnergyPortalUserDto camUser = EnergyPortalUserDtoTestUtil.Builder()
      .withWebUserAccountId(CASE_USER_WUA_ID)
      .build();

  public static Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtosMap = Map.of(
      WebUserAccountId.from(submitter.webUserAccountId()), submitter,
      WebUserAccountId.from(caseOfficer.webUserAccountId()), caseOfficer,
      WebUserAccountId.from(technicalReviewer.webUserAccountId()), technicalReviewer,
      WebUserAccountId.from(camUser.webUserAccountId()), camUser
    );

  public static ApplicationDataItemDto getApplicationDataItemDtoForAnnualProductionInProgressForField() {
    return getApplicationDataItemDtoForAnnualProductionInProgressForField(APPLICATION_NO);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForAnnualProductionInProgressForField(
      Integer applicationNo
  ) {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.PRODUCTION,
        0,
        applicationNo,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.IN_PROGRESS,
        AssetType.FIELD,
        FIELD_ID_1,
        FIELD_NAME_1,
        ConsentLengthType.ANNUAL,
        ANNUAL_CONSENT_YEAR,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "P1, P2, P3",
        false,
        null,
        null,
        false
    );
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
        AssetType.FIELD,
        FIELD_ID_1,
        FIELD_NAME_1,
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
        null,
        null,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "P1, P2, P3",
        false,
        null,
        null,
        false);
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
        AssetType.TERMINAL,
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
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "",
        false,
        null,
        null,
        false);
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
        AssetType.TERMINAL,
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
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "",
        false,
        null,
        null,
        false);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForShortVentAssignedToCaseOfficerForTerminal() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.VENT,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        AssetType.TERMINAL,
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
        null,
        null,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "",
        false,
        null,
        null,
        false);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForShortVentAssignedToCamForTerminal() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.VENT,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        AssetType.TERMINAL,
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
        CASE_USER_WUA_ID,
        null,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "",
        false,
        null,
        null,
        false);
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
        AssetType.TERMINAL,
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
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null,
        "",
        false,
        null,
        null,
        false);
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
        AssetType.FIELD,
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
        null,
        null,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "P1, P2, P3",
        false,
        null,
        null,
        false);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForLongFlareConsentedForField() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.FLARE,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.CONSENTED,
        AssetType.FIELD,
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
        CASE_OFFICER_WUA_ID,
        CAM_USER_WUA_ID,
        RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER,
        false,
        TECHNICAL_REVIEWER_WUA_ID,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "P1, P2, P3",
        false,
        LocalDate.now().plusMonths(1),
        LocalDate.now().plusYears(2),
        true);
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
        AssetType.FIELD,
        FIELD_ID_1,
        FIELD_NAME_1,
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
        null,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        true,
        Instant.now().plusSeconds(2*60*60),
        null,
        "P1, P2, P3",
        false,
        null,
        null,
        false);
  }

  public static ApplicationDataItemDto getApplicationDataItemDtoForLongFlareSubmittedForTerminalWithOpenWithdrawalRequest() {
    return new ApplicationDataItemDto(
        APPLICATION_ID,
        APPLICATION_VERSION_ID,
        ApplicationType.FLARE,
        0,
        APPLICATION_NO,
        APPLICATION_VERSION_NUMBER,
        PRIMARY_OPERATOR_OU_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        AssetType.TERMINAL,
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
        null,
        null,
        true,
        null,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        "",
        false,
        null,
        null,
        false
    );
  }

  public static ApplicationDataItemView getApplicationDataItemView() {
    return new ApplicationDataItemView(
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
        "",
        false,
        "",
        false,
        "",
        false,
        "",
        false,
        "",
        false,
        "P1, P2, P3",
        false,
        false,
        false,
        false
    );
  }
}
