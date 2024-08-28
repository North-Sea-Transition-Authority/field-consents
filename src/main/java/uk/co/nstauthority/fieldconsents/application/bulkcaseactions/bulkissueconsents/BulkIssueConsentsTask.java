package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Audited
@Entity
@Table(name = "bulk_issue_consents_tasks")
public class BulkIssueConsentsTask {

  @Id
  @UuidGenerator
  private UUID id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @ManyToOne
  @JoinColumn(name = "bulk_issue_consent_run_id")
  private BulkIssueConsentRun bulkIssueConsentRun;

  private Instant createdAt;

  private Instant startedAt;

  private Instant finishedAt;

  private String errorDetails;

  public BulkIssueConsentsTask() {
  }

  BulkIssueConsentsTask(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public BulkIssueConsentRun getBulkIssueConsentRun() {
    return bulkIssueConsentRun;
  }

  public void setBulkIssueConsentRun(
      BulkIssueConsentRun bulkIssueConsentRun) {
    this.bulkIssueConsentRun = bulkIssueConsentRun;
  }

  Instant getCreatedAt() {
    return createdAt;
  }

  void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  Instant getStartedAt() {
    return startedAt;
  }

  void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  void setFinishedAt(Instant finishedAt) {
    this.finishedAt = finishedAt;
  }

  String getErrorDetails() {
    return errorDetails;
  }

  void setErrorDetails(String errorDetails) {
    this.errorDetails = errorDetails;
  }

}
