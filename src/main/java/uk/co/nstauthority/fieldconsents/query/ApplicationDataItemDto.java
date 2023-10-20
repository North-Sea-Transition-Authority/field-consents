package uk.co.nstauthority.fieldconsents.query;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

public class ApplicationDataItemDto {
  private final Integer applicationId;
  private final Integer applicationVersionId;
  private final ApplicationType type;
  private final Integer variationNo;
  private final Integer applicationNo;
  private final Integer versionNo;
  private final Integer operatorId;
  private final ApplicationVersionStatus status;
  private final Integer fieldId;
  private final String fieldName;
  private final Integer terminalId;
  private final String terminalName;
  private final ConsentLengthType duration;
  private final Integer consentYear;
  private final LocalDate shortTermStartDate;
  private final LocalDate shortTermEndDate;
  private final Integer longTermStartYear;
  private final Integer longTermEndYear;
  private final Instant submittedDateTime;
  private final Long submittedByWuaId;
  private final Boolean aceFlag;
  private final Long caseOfficerWuaId;
  private final Boolean withdrawalOpen;
  private final Long technicalReviewerWuaId;
  private final Boolean applicationUpdateOpen;
  private final Instant applicationUpdateDeadline;
  private final Boolean consultationOpen;
  private final Instant consultationDeadline;
  private final FurtherInformationStatus consultationFurtherInformationStatus;

  public ApplicationDataItemDto(Integer applicationId, Integer applicationVersionId, ApplicationType type,
                                Integer variationNo, Integer applicationNo, Integer versionNo, Integer operatorId,
                                ApplicationVersionStatus status, Integer fieldId, String fieldName, Integer terminalId,
                                String terminalName, ConsentLengthType duration, Integer consentYear,
                                LocalDate shortTermStartDate, LocalDate shortTermEndDate, Integer longTermStartYear,
                                Integer longTermEndYear, Instant submittedDateTime, Long submittedByWuaId,
                                Boolean aceFlag, Long caseOfficerWuaId, Boolean withdrawalOpen,
                                Long technicalReviewerWuaId,
                                Boolean applicationUpdateOpen, Instant applicationUpdateDeadline,
                                Boolean consultationOpen, Instant consultationDeadline,
                                FurtherInformationStatus consultationFurtherInformationStatus) {
    this.applicationId = applicationId;
    this.applicationVersionId = applicationVersionId;
    this.type = type;
    this.variationNo = variationNo;
    this.applicationNo = applicationNo;
    this.versionNo = versionNo;
    this.operatorId = operatorId;
    this.status = status;
    this.fieldId = fieldId;
    this.fieldName = fieldName;
    this.terminalId = terminalId;
    this.terminalName = terminalName;
    this.duration = duration;
    this.consentYear = consentYear;
    this.shortTermStartDate = shortTermStartDate;
    this.shortTermEndDate = shortTermEndDate;
    this.longTermStartYear = longTermStartYear;
    this.longTermEndYear = longTermEndYear;
    this.submittedDateTime = submittedDateTime;
    this.submittedByWuaId = submittedByWuaId;
    this.aceFlag = aceFlag;
    this.caseOfficerWuaId = caseOfficerWuaId;
    this.withdrawalOpen = withdrawalOpen;
    this.technicalReviewerWuaId = technicalReviewerWuaId;
    this.applicationUpdateOpen = applicationUpdateOpen;
    this.applicationUpdateDeadline = applicationUpdateDeadline;
    this.consultationOpen = consultationOpen;
    this.consultationDeadline = consultationDeadline;
    this.consultationFurtherInformationStatus = consultationFurtherInformationStatus;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public Integer getApplicationVersionId() {
    return applicationVersionId;
  }

  public ApplicationType getType() {
    return type;
  }

  public Integer getVariationNo() {
    return variationNo;
  }

  public Integer getApplicationNo() {
    return applicationNo;
  }

  public Integer getVersionNo() {
    return versionNo;
  }

  public Integer getOperatorId() {
    return operatorId;
  }

  public ApplicationVersionStatus getStatus() {
    return status;
  }

  public Integer getFieldId() {
    return fieldId;
  }

  public String getFieldName() {
    return fieldName;
  }

  public Integer getTerminalId() {
    return terminalId;
  }

  public String getTerminalName() {
    return terminalName;
  }

  public ConsentLengthType getDuration() {
    return duration;
  }

  public Integer getConsentYear() {
    return consentYear;
  }

  public LocalDate getShortTermStartDate() {
    return shortTermStartDate;
  }

  public LocalDate getShortTermEndDate() {
    return shortTermEndDate;
  }

  public Integer getLongTermStartYear() {
    return longTermStartYear;
  }

  public Integer getLongTermEndYear() {
    return longTermEndYear;
  }

  public Instant getSubmittedDateTime() {
    return submittedDateTime;
  }

  public Long getSubmittedByWuaId() {
    return submittedByWuaId;
  }

  public Boolean getAceFlag() {
    return aceFlag;
  }

  public Long getCaseOfficerWuaId() {
    return caseOfficerWuaId;
  }

  public Boolean getWithdrawalOpen() {
    return withdrawalOpen;
  }

  public Long getTechnicalReviewerWuaId() {
    return technicalReviewerWuaId;
  }

  public Boolean getApplicationUpdateOpen() {
    return applicationUpdateOpen;
  }

  public Instant getApplicationUpdateDeadline() {
    return applicationUpdateDeadline;
  }

  public Boolean getConsultationOpen() {
    return consultationOpen;
  }

  public Instant getConsultationDeadline() {
    return consultationDeadline;
  }

  public FurtherInformationStatus getConsultationFurtherInformationStatus() {
    return consultationFurtherInformationStatus;
  }
}
