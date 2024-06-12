package uk.co.nstauthority.fieldconsents.epmqmessage;

import java.time.Instant;
import java.util.List;

public class ApplicationSubmittedFieldConsentsEpmqMessage extends FieldConsentsEpmqMessage {

  public static final String TYPE = "APPLICATION_SUBMITTED";

  private Integer applicationVersionId;
  private String applicationReference;
  private Integer primaryOperatorOrganisationUnitId;
  private Integer primaryFieldId;
  private List<Integer> secondaryFieldIds;
  private Integer primaryTerminalId;
  private Instant submittedInstant;
  private ApplicationSubmissionType submissionType;

  public ApplicationSubmittedFieldConsentsEpmqMessage() {
    super(TYPE, null, null);
  }

  private ApplicationSubmittedFieldConsentsEpmqMessage(
      Integer applicationVersionId,
      String applicationReference,
      Integer primaryOperatorOrganisationUnitId,
      Integer primaryFieldId,
      List<Integer> secondaryFieldIds,
      Integer primaryTerminalId,
      Instant submittedInstant,
      ApplicationSubmissionType submissionType,
      String correlationId,
      Instant createdInstant
  ) {
    super(TYPE, correlationId, createdInstant);
    this.applicationVersionId = applicationVersionId;
    this.applicationReference = applicationReference;
    this.primaryOperatorOrganisationUnitId = primaryOperatorOrganisationUnitId;
    this.primaryFieldId = primaryFieldId;
    this.secondaryFieldIds = secondaryFieldIds;
    this.primaryTerminalId = primaryTerminalId;
    this.submittedInstant = submittedInstant;
    this.submissionType = submissionType;
  }

  public Integer getApplicationVersionId() {
    return applicationVersionId;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public Integer getPrimaryOperatorOrganisationUnitId() {
    return primaryOperatorOrganisationUnitId;
  }

  public Integer getPrimaryFieldId() {
    return primaryFieldId;
  }

  public List<Integer> getSecondaryFieldIds() {
    return secondaryFieldIds;
  }

  public Integer getPrimaryTerminalId() {
    return primaryTerminalId;
  }

  public Instant getSubmittedInstant() {
    return submittedInstant;
  }

  public ApplicationSubmissionType getSubmissionType() {
    return submissionType;
  }

  public static Builder builder(String correlationId, Instant createdInstant) {
    return new Builder(correlationId, createdInstant);
  }

  public static class Builder {

    private final String correlationId;
    private final Instant createdInstant;
    private Integer applicationVersionId;
    private String applicationReference;
    private Integer primaryOperatorOrganisationUnitId;
    private Integer primaryFieldId;
    private List<Integer> secondaryFieldIds;
    private Integer primaryTerminalId;
    private Instant submittedInstant;
    private ApplicationSubmissionType submissionType;

    private Builder(String correlationId, Instant createdInstant) {
      this.correlationId = correlationId;
      this.createdInstant = createdInstant;
    }

    public Builder withApplicationVersionId(Integer applicationVersionId) {
      this.applicationVersionId = applicationVersionId;
      return this;
    }

    public Builder withApplicationReference(String applicationReference) {
      this.applicationReference = applicationReference;
      return this;
    }

    public Builder withPrimaryOperatorOrganisationUnitId(Integer primaryOperatorOrganisationUnitId) {
      this.primaryOperatorOrganisationUnitId = primaryOperatorOrganisationUnitId;
      return this;
    }

    public Builder withPrimaryFieldId(Integer primaryFieldId) {
      this.primaryFieldId = primaryFieldId;
      return this;
    }

    public Builder withSecondaryFieldIds(List<Integer> secondaryFieldIds) {
      this.secondaryFieldIds = secondaryFieldIds;
      return this;
    }

    public Builder withPrimaryTerminalId(Integer primaryTerminalId) {
      this.primaryTerminalId = primaryTerminalId;
      return this;
    }

    public Builder withSubmittedInstant(Instant submittedInstant) {
      this.submittedInstant = submittedInstant;
      return this;
    }

    public Builder withSubmissionType(ApplicationSubmissionType submissionType) {
      this.submissionType = submissionType;
      return this;
    }

    public ApplicationSubmittedFieldConsentsEpmqMessage build() {
      return new ApplicationSubmittedFieldConsentsEpmqMessage(
          applicationVersionId,
          applicationReference,
          primaryOperatorOrganisationUnitId,
          primaryFieldId,
          secondaryFieldIds,
          primaryTerminalId,
          submittedInstant,
          submissionType,
          correlationId,
          createdInstant
      );
    }
  }
}
