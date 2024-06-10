package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.Application;

@Audited
@Entity
@Table(name = "application_consent_issuing_approvals")
public class ConsentIssuingApproval {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_id")
  private Application application;

  private long approvedByWuaId;

  @Column(name = "approved_timestamp")
  private Instant approvedInstant;

  public ConsentIssuingApproval() {
  }

  public Integer getId() {
    return id;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public Long getApprovedByWuaId() {
    return approvedByWuaId;
  }

  public void setApprovedByWuaId(Long approvedByWuaId) {
    this.approvedByWuaId = approvedByWuaId;
  }

  public Instant getApprovedInstant() {
    return approvedInstant;
  }

  public void setApprovedInstant(Instant approvedInstant) {
    this.approvedInstant = approvedInstant;
  }
}
