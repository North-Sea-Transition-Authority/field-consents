package uk.co.nstauthority.fieldconsents.query;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public class ApplicationDataItem {
  private final Integer applicationId;
  private final String type;
  private final String duration;
  private final String reference;
  private final String operator;
  private final String asset;
  private final String geographicArea;
  private final String status;
  private final String submittedDateTime;
  private final String submittedBy;
  private final String aceFlag;
  private final String caseOfficer;
  private final Boolean withdrawalOpen;
  private final String technicalReviewer;
  private final Boolean applicationUpdateOpen;
  private final String applicationUpdateDeadline;

  public ApplicationDataItem(Integer applicationId, String type, String duration, String reference, String operator,
                             String asset, String geographicArea, String status, String submittedDateTime,
                             String submittedBy, String aceFlag, String caseOfficer, Boolean withdrawalOpen,
                             String technicalReviewer, Boolean applicationUpdateOpen, String applicationUpdateDeadline) {
    this.applicationId = applicationId;
    this.type = type;
    this.duration = duration;
    this.reference = reference;
    this.operator = operator;
    this.asset = asset;
    this.geographicArea = geographicArea;
    this.status = status;
    this.submittedDateTime = submittedDateTime;
    this.submittedBy = submittedBy;
    this.aceFlag = aceFlag;
    this.caseOfficer = caseOfficer;
    this.withdrawalOpen = withdrawalOpen;
    this.technicalReviewer = technicalReviewer;
    this.applicationUpdateOpen = applicationUpdateOpen;
    this.applicationUpdateDeadline = applicationUpdateDeadline;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public String getType() {
    return type;
  }

  public String getDuration() {
    return duration;
  }

  public String getReference() {
    return reference;
  }

  public String getOperator() {
    return operator;
  }

  public String getAsset() {
    return asset;
  }

  public String getGeographicArea() {
    return geographicArea;
  }

  public String getStatus() {
    return status;
  }

  public String getSubmittedDateTime() {
    return submittedDateTime;
  }

  public String getSubmittedBy() {
    return submittedBy;
  }

  public String getAceFlag() {
    return aceFlag;
  }

  public String getCaseOfficer() {
    return caseOfficer;
  }

  public Boolean isWithdrawalOpen() {
    return withdrawalOpen;
  }

  public String getTechnicalReviewer() {
    return technicalReviewer;
  }

  public Boolean isApplicationUpdateOpen() {
    return applicationUpdateOpen;
  }

  public String getApplicationUpdateDeadline() {
    return applicationUpdateDeadline;
  }

  public String url() {
    return ReverseRouter.route(on(ApplicationSummaryController.class).getApplicationSummary(applicationId, null));
  }
}
