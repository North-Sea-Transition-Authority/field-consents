package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;

@Entity
@Table(name = "application_consent_field_equity_partners")
public class ConsentFieldEquityPartner {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_consent_id")
  private Consent consent;

  private Integer organisationUnitId;

  private String organisationName;

  private String registeredNumber;

  public ConsentFieldEquityPartner() {
  }

  public ConsentFieldEquityPartner(Consent consent,
                                   Integer organisationUnitId,
                                   String organisationName,
                                   String registeredNumber) {
    this.consent = consent;
    this.organisationUnitId = organisationUnitId;
    this.organisationName = organisationName;
    this.registeredNumber = registeredNumber;
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

  public Integer getOrganisationUnitId() {
    return organisationUnitId;
  }

  public void setOrganisationUnitId(Integer organisationUnitId) {
    this.organisationUnitId = organisationUnitId;
  }

  public String getOrganisationName() {
    return organisationName;
  }

  public String getRegisteredNumber() {
    return registeredNumber;
  }
}
