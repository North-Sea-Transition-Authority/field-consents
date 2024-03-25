package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.Application;

@Entity
@Table(name = "application_consents")
public class Consent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_id")
  private Application application;

  private long issuedByWuaId;

  @Column(name = "issued_timestamp")
  private Instant issuedInstant;

  public Consent() {
  }

  public Consent(Integer id) {
    this.id = id;
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

  public long getIssuedByWuaId() {
    return issuedByWuaId;
  }

  public void setIssuedByWuaId(long issuedByWuaId) {
    this.issuedByWuaId = issuedByWuaId;
  }

  public Instant getIssuedInstant() {
    return issuedInstant;
  }

  public void setIssuedInstant(Instant issuedInstant) {
    this.issuedInstant = issuedInstant;
  }
}
