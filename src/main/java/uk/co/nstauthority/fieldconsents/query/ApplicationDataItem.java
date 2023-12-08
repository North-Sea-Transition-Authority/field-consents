package uk.co.nstauthority.fieldconsents.query;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record ApplicationDataItem(
    Integer applicationId,
    String type,
    String duration,
    String reference,
    String operator,
    String asset,
    String geographicArea,
    String status,
    String submittedDateTime,
    String submittedBy,
    String aceFlag,
    String caseOfficer,
    String camUser,
    Boolean withdrawalOpen,
    String technicalReviewer,
    Boolean technicalReviewOpen,
    String technicalReviewDeadline,
    Boolean applicationUpdateOpen,
    String applicationUpdateDeadline,
    Boolean consultationOpen,
    String consultationDeadline,
    Boolean consultationFurtherInformationOpen,
    String licenses
) {

  public static Builder newBuilder() {
    return new ApplicationDataItem.Builder();
  }

  public static class Builder {

    private Integer applicationId;
    private String type;
    private String duration;
    private String reference;
    private String operator;
    private String asset;
    private String geographicArea;
    private String status;
    private String submittedDateTime;
    private String submittedBy;
    private String aceFlag;
    private String caseOfficer;
    private String camUser;
    private Boolean withdrawalOpen;
    private String technicalReviewer;
    private Boolean technicalReviewOpen;
    private String technicalReviewDeadline;
    private Boolean applicationUpdateOpen;
    private String applicationUpdateDeadline;
    private Boolean consultationOpen;
    private String consultationDeadline;
    private Boolean consultationFurtherInformationOpen;
    private String licenses;

    public Builder withApplicationId(Integer applicationId) {
      this.applicationId = applicationId;
      return this;
    }

    public Builder withType(String type) {
      this.type = type;
      return this;
    }

    public Builder withDuration(String duration) {
      this.duration = duration;
      return this;
    }

    public Builder withReference(String reference) {
      this.reference = reference;
      return this;
    }

    public Builder withOperator(String operator) {
      this.operator = operator;
      return this;
    }

    public Builder withAsset(String asset) {
      this.asset = asset;
      return this;
    }

    public Builder withGeographicArea(String geographicArea) {
      this.geographicArea = geographicArea;
      return this;
    }

    public Builder withStatus(String status) {
      this.status = status;
      return this;
    }

    public Builder withSubmittedDateTime(String submittedDateTime) {
      this.submittedDateTime = submittedDateTime;
      return this;
    }

    public Builder withSubmittedBy(String submittedBy) {
      this.submittedBy = submittedBy;
      return this;
    }

    public Builder withAceFlag(String aceFlag) {
      this.aceFlag = aceFlag;
      return this;
    }

    public Builder withCaseOfficer(String caseOfficer) {
      this.caseOfficer = caseOfficer;
      return this;
    }

    public Builder withCamUser(String camUser) {
      this.camUser = camUser;
      return this;
    }

    public Builder withWithdrawalOpen(Boolean withdrawalOpen) {
      this.withdrawalOpen = withdrawalOpen;
      return this;
    }

    public Builder withTechnicalReviewer(String technicalReviewer) {
      this.technicalReviewer = technicalReviewer;
      return this;
    }

    public Builder withTechnicalReviewOpen(Boolean technicalReviewOpen) {
      this.technicalReviewOpen = technicalReviewOpen;
      return this;
    }

    public Builder withTechnicalReviewDeadline(String technicalReviewDeadline) {
      this.technicalReviewDeadline = technicalReviewDeadline;
      return this;
    }

    public Builder withApplicationUpdateOpen(Boolean applicationUpdateOpen) {
      this.applicationUpdateOpen = applicationUpdateOpen;
      return this;
    }

    public Builder withApplicationUpdateDeadline(String applicationUpdateDeadline) {
      this.applicationUpdateDeadline = applicationUpdateDeadline;
      return this;
    }

    public Builder withConsultationOpen(Boolean consultationOpen) {
      this.consultationOpen = consultationOpen;
      return this;
    }

    public Builder withConsultationDeadline(String consultationDeadline) {
      this.consultationDeadline = consultationDeadline;
      return this;
    }

    public Builder withConsultationFurtherInformationOpen(Boolean consultationFurtherInformationOpen) {
      this.consultationFurtherInformationOpen = consultationFurtherInformationOpen;
      return this;
    }

    public Builder withLicences(String licences) {
      this.licenses = licences;
      return this;
    }

    public ApplicationDataItem build() {
      return new ApplicationDataItem(
          applicationId,
          type,
          duration,
          reference,
          operator,
          asset,
          geographicArea,
          status,
          submittedDateTime,
          submittedBy,
          aceFlag,
          caseOfficer,
          camUser,
          withdrawalOpen,
          technicalReviewer,
          technicalReviewOpen,
          technicalReviewDeadline,
          applicationUpdateOpen,
          applicationUpdateDeadline,
          consultationOpen,
          consultationDeadline,
          consultationFurtherInformationOpen,
          licenses
      );
    }
  }


  public String url() {
    return ReverseRouter.route(on(ApplicationSummaryController.class).getApplicationSummary(applicationId, null));
  }
}
