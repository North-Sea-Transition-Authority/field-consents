package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.task;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
class BulkIssueConsentsTask {

  @Id
  @UuidGenerator
  private UUID id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Instant createdAt;

  private Long createdByWuaId;

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

  Instant getCreatedAt() {
    return createdAt;
  }

  void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  Long getCreatedByWuaId() {
    return createdByWuaId;
  }

  void setCreatedByWuaId(Long createdByWuaId) {
    this.createdByWuaId = createdByWuaId;
  }

  Instant getStartedAt() {
    return startedAt;
  }

  void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  Instant getFinishedAt() {
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
