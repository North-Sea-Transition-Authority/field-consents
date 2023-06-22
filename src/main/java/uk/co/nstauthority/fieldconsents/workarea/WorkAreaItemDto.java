package uk.co.nstauthority.fieldconsents.workarea;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

public record WorkAreaItemDto(
    Integer applicationId,
    Integer applicationVersionId,
    ApplicationType type,
    Integer variationNo,
    Integer applicationNo,
    Integer versionNo,
    Integer operatorId,
    ApplicationVersionStatus status,
    Integer fieldId,
    String fieldName,
    Integer terminalId,
    String terminalName,
    ConsentLengthType duration,
    Integer consentYear,
    LocalDate shortTermStartDate,
    LocalDate shortTermEndDate,
    Integer longTermStartYear,
    Integer longTermEndYear,
    Instant submittedDateTime,
    Long submittedByWuaId,
    Boolean aceFlag,
    Long caseOfficerWuaId,
    Boolean withdrawalOpen
) {
}
