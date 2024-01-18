package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.Application;

@Entity
@Table(name = "application_consent_data")
public class ConsentData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_id")
  private Application application;

  private LocalDate consentStartDate;

  private LocalDate consentEndDate;

  public ConsentData() {
  }

  public ConsentData(Integer id) {
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

  public LocalDate getConsentStartDate() {
    return consentStartDate;
  }

  public void setConsentStartDate(LocalDate consentStartDate) {
    this.consentStartDate = consentStartDate;
  }

  public LocalDate getConsentEndDate() {
    return consentEndDate;
  }

  public void setConsentEndDate(LocalDate consentEndDate) {
    this.consentEndDate = consentEndDate;
  }
}
