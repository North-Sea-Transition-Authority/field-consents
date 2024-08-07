package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;

@Audited
@Entity
@Table(name = "application_consent_breaches")
public class ConsentBreach {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "consent_id")
  private Consent consent;

  private Long addedByWuaId;

  private Instant addedDateTime;

  private String breachText;

  public ConsentBreach() {

  }

  public ConsentBreach(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public Consent getConsent() {
    return consent;
  }

  public void setConsent(Consent consent) {
    this.consent = consent;
  }

  public Long getAddedByWuaId() {
    return addedByWuaId;
  }

  public void setAddedByWuaId(Long addedByWuaId) {
    this.addedByWuaId = addedByWuaId;
  }

  public Instant getAddedDateTime() {
    return addedDateTime;
  }

  public void setAddedDateTime(Instant addedDateTime) {
    this.addedDateTime = addedDateTime;
  }

  public String getBreachText() {
    return breachText;
  }

  public void setBreachText(String breachText) {
    this.breachText = breachText;
  }
}
