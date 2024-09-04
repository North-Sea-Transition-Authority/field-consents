package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.envers.Audited;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;

@Audited
@Entity
@Table(name = "bulk_issue_consent_runs")
public class BulkIssueConsentRun implements DomainReference {

  @Id
  @UuidGenerator
  private UUID id;

  private Long issuedByWuaId;

  public BulkIssueConsentRun() {
  }

  BulkIssueConsentRun(Long issuedByWuaId) {
    this.issuedByWuaId = issuedByWuaId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Long getIssuedByWuaId() {
    return issuedByWuaId;
  }

  public void setIssuedByWuaId(Long issuedByWuaId) {
    this.issuedByWuaId = issuedByWuaId;
  }

  @Override
  public String getDomainId() {
    return String.valueOf(id);
  }

  @Override
  public String getDomainType() {
    return "BULK_ISSUE_CONSENT_RUN";
  }
}
