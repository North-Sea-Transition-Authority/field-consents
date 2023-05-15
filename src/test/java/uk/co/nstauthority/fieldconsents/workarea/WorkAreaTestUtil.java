package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_NUMBER;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1;
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
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class WorkAreaTestUtil {

  private static final String SUBMITTED_DATE_TIME = "1 Oct 2022 12:00";

  public static WorkAreaItemDto getWorkAreaItemDtoForAnnualProductionInProgressForField() {
    return new WorkAreaItemDto(
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
        ConsentLengthType.ANNUAL,
        ANNUAL_CONSENT_YEAR,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static WorkAreaItemDto getWorkAreaItemDtoForProductionInProgressForFieldNoDuration() {
    return new WorkAreaItemDto(
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
        ANNUAL_CONSENT_YEAR,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static WorkAreaItemDto getWorkAreaItemDtoForShortVentSubmittedForTerminal() {
    return new WorkAreaItemDto(
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
        TERMINAL_NAME_1,
        ConsentLengthType.SHORT_TERM,
        null,
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE,
        null,
        null,
        Instant.now(),
        1L
    );
  }

  public static WorkAreaItemDto getWorkAreaItemDtoForLongFlareSubmittedForTerminal() {
    return new WorkAreaItemDto(
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
        TERMINAL_NAME_1,
        ConsentLengthType.LONG_TERM,
        null,
        null,
        null,
        LONG_TERM_START_YEAR,
        LONG_TERM_END_YEAR,
        Instant.now(),
        1L
    );
  }

  static WorkAreaItem getWorkAreaItemFromDto(WorkAreaItemDto workAreaItemDto) {
    return new WorkAreaItem(
        workAreaItemDto.applicationId(),
        workAreaItemDto.type().getDisplayName(),
        getDuration(workAreaItemDto),
        getCaseReference(workAreaItemDto),
        getOperator(workAreaItemDto),
        getAsset(workAreaItemDto),
        getSeaLocation(workAreaItemDto),
        workAreaItemDto.status().getDisplayName(),
        getSubmittedDateTime(workAreaItemDto),
        getSubmitter(workAreaItemDto)
    );
  }

  private static String getSubmittedDateTime(WorkAreaItemDto workAreaItemDto) {
    return workAreaItemDto.status().equals(ApplicationVersionStatus.SUBMITTED)
        ? "Submitted %s".formatted(DateUtils.format(workAreaItemDto.submittedDateTime(), DateUtils.DATE_TIME))
        : "";
  }

  private static String getSeaLocation(WorkAreaItemDto workAreaItemDto) {
    return workAreaItemDto.fieldId() != null
        ? field1JsonWithOperator.getGeographicAreaDisplayName()
        : "";
  }

  private static String getAsset(WorkAreaItemDto workAreaItemDto) {
    return workAreaItemDto.fieldId() != null
        ? workAreaItemDto.fieldName()
        : workAreaItemDto.terminalName();
  }

  private static String getOperator(WorkAreaItemDto workAreaItemDto) {
    return workAreaItemDto.fieldId() != null
        ? field1JsonWithOperator.getOperatorName()
        : terminal1JsonWithOperator.getOperatorName();
  }

  static String getDuration(WorkAreaItemDto workAreaItemDto) {
    if (workAreaItemDto.duration() != null) {
      var consentDurationString = workAreaItemDto.duration().getShortDisplayName();

      switch (workAreaItemDto.duration()) {
        case ANNUAL -> {
          return "%s %s".formatted(consentDurationString, workAreaItemDto.consentYear());
        }
        case LONG_TERM -> {
          return "%s %d - %d".formatted(consentDurationString, workAreaItemDto.longTermStartYear(), workAreaItemDto.longTermEndYear());
        }

        case SHORT_TERM -> {
          return "%s %s - %s".formatted(consentDurationString,
              DateUtils.format(workAreaItemDto.shortTermStartDate(), DateUtils.SHORT_DATE),
              DateUtils.format(workAreaItemDto.shortTermEndDate(), DateUtils.SHORT_DATE)
          );
        }
      }
    }

    return "";
  }

  private static String getSubmitter(WorkAreaItemDto workAreaItemDto) {
    if (workAreaItemDto.status().equals(ApplicationVersionStatus.SUBMITTED)) {
      var submitter = EnergyPortalUserDtoTestUtil.Builder().build();
      return "Submitted by %s %s %s"
          .formatted(submitter.title(), submitter.forename(), submitter.surname());
    }

    return "";
  }

  private static String getCaseReference(WorkAreaItemDto workAreaItemDto) {
    return workAreaItemDto.status().equals(ApplicationVersionStatus.SUBMITTED)
        ? "%s/%d/%d (Version %d)".formatted(workAreaItemDto.type().getReferenceMnemonic(),
        workAreaItemDto.applicationNo(),
        workAreaItemDto.variationNo(),
        workAreaItemDto.versionNo())
        : "Resume application";
  }

  public static WorkAreaItem getWorkAreaItem() {
    return new WorkAreaItem(
        APPLICATION_ID,
        ApplicationType.FLARE.getDisplayName(),
        ConsentLengthType.ANNUAL.getDisplayName(),
        APPLICATION_REFERENCE,
        CACHED_PRIMARY_OPERATOR_NAME_1,
        FIELD_NAME_1,
        FIELD_1_GEOGRAPHIC_AREA.geographicAreaDisplayName(),
        ApplicationVersionStatus.SUBMITTED.getDisplayName(),
        SUBMITTED_DATE_TIME,
        String.valueOf(USER_WUA_ID)
        );
  }
}
