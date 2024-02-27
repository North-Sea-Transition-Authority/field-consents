package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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

  private LocalDate longTermProductionConsentScheduleStartDate;

  private BigDecimal shortTermOrAnnualProductionMinOil;

  private BigDecimal shortTermOrAnnualProductionMaxOil;

  private BigDecimal shortTermOrAnnualProductionMinGas;

  private BigDecimal shortTermOrAnnualProductionMaxGas;

  private BigDecimal emissionDailyAverage;

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

  public LocalDate getLongTermProductionConsentScheduleStartDate() {
    return longTermProductionConsentScheduleStartDate;
  }

  public void setLongTermProductionConsentScheduleStartDate(LocalDate longTermProductionConsentScheduleStartDate) {
    this.longTermProductionConsentScheduleStartDate = longTermProductionConsentScheduleStartDate;
  }

  public BigDecimal getShortTermOrAnnualProductionMinOil() {
    return shortTermOrAnnualProductionMinOil;
  }

  public void setShortTermOrAnnualProductionMinOil(BigDecimal shortTermOrAnnualProductionMinOil) {
    this.shortTermOrAnnualProductionMinOil = shortTermOrAnnualProductionMinOil;
  }

  public BigDecimal getShortTermOrAnnualProductionMaxOil() {
    return shortTermOrAnnualProductionMaxOil;
  }

  public void setShortTermOrAnnualProductionMaxOil(BigDecimal shortTermOrAnnualProductionMaxOil) {
    this.shortTermOrAnnualProductionMaxOil = shortTermOrAnnualProductionMaxOil;
  }

  public BigDecimal getShortTermOrAnnualProductionMinGas() {
    return shortTermOrAnnualProductionMinGas;
  }

  public void setShortTermOrAnnualProductionMinGas(BigDecimal shortTermOrAnnualProductionMinGas) {
    this.shortTermOrAnnualProductionMinGas = shortTermOrAnnualProductionMinGas;
  }

  public BigDecimal getShortTermOrAnnualProductionMaxGas() {
    return shortTermOrAnnualProductionMaxGas;
  }

  public void setShortTermOrAnnualProductionMaxGas(BigDecimal shortTermOrAnnualProductionMaxGas) {
    this.shortTermOrAnnualProductionMaxGas = shortTermOrAnnualProductionMaxGas;
  }

  public BigDecimal getEmissionDailyAverage() {
    return emissionDailyAverage;
  }

  public void setEmissionDailyAverage(BigDecimal emissionDailyAverage) {
    this.emissionDailyAverage = emissionDailyAverage;
  }
}
