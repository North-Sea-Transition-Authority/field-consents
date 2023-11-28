package uk.co.nstauthority.fieldconsents.search;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDto;

public class SearchResultItemDto extends ApplicationDataItemDto {

  private final String licences;

  public SearchResultItemDto(Integer applicationId, Integer applicationVersionId,
                             ApplicationType type, Integer variationNo,
                             Integer applicationNo, Integer versionNo, Integer operatorId,
                             ApplicationVersionStatus status,
                             AssetType assetType, Integer assetId, String assetName,
                             ConsentLengthType duration,
                             Integer consentYear, LocalDate shortTermStartDate,
                             LocalDate shortTermEndDate, Integer longTermStartYear, Integer longTermEndYear,
                             Instant submittedDateTime, Long submittedByWuaId, Boolean aceFlag,
                             Long caseOfficerWuaId, Boolean withdrawalOpen, Long technicalReviewerWuaId,
                             Boolean technicalReviewOpen, Instant technicalReviewDeadline,
                             Boolean applicationUpdateOpen, Instant applicationUpdateDeadline,
                             Boolean consultationOpen, Instant consultationDeadline,
                             FurtherInformationStatus furtherInformationStatus,
                             String licences) {

    super(applicationId, applicationVersionId, type, variationNo, applicationNo, versionNo, operatorId, status, assetType,
        assetId, assetName, duration, consentYear, shortTermStartDate, shortTermEndDate,
        longTermStartYear, longTermEndYear, submittedDateTime, submittedByWuaId, aceFlag, caseOfficerWuaId,
        withdrawalOpen, technicalReviewerWuaId, technicalReviewOpen, technicalReviewDeadline,
        applicationUpdateOpen, applicationUpdateDeadline, consultationOpen,
        consultationDeadline, furtherInformationStatus);

    this.licences = licences;
  }

  public String getLicences() {
    return licences;
  }
}
