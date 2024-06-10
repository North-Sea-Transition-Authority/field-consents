package uk.co.nstauthority.fieldconsents.application.consentlength;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Audited
@Table(name = "consent_lengths")
public class ConsentLengthDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Enumerated(EnumType.STRING)
  private ConsentLengthType consentLength;

  private Integer annualConsentYear;

  private LocalDate shortTermStartDate;

  private LocalDate shortTermEndDate;

  private Integer longTermStartYear;

  private Integer longTermEndYear;

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public ConsentLengthType getConsentLength() {
    return consentLength;
  }

  public void setConsentLength(ConsentLengthType consentLength) {
    this.consentLength = consentLength;
  }

  public Integer getAnnualConsentYear() {
    return annualConsentYear;
  }

  public void setAnnualConsentYear(Integer annualConsentYear) {
    this.annualConsentYear = annualConsentYear;
  }

  public LocalDate getShortTermStartDate() {
    return shortTermStartDate;
  }

  public void setShortTermStartDate(LocalDate shortTermStartDate) {
    this.shortTermStartDate = shortTermStartDate;
  }

  public LocalDate getShortTermEndDate() {
    return shortTermEndDate;
  }

  public void setShortTermEndDate(LocalDate shortTermEndDate) {
    this.shortTermEndDate = shortTermEndDate;
  }

  public Integer getLongTermStartYear() {
    return longTermStartYear;
  }

  public void setLongTermStartYear(Integer longTermStartYear) {
    this.longTermStartYear = longTermStartYear;
  }

  public Integer getLongTermEndYear() {
    return longTermEndYear;
  }

  public void setLongTermEndYear(Integer longTermEndYear) {
    this.longTermEndYear = longTermEndYear;
  }
}
