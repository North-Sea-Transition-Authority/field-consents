package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import uk.co.nstauthority.fieldconsents.application.Application;

@Audited
@Entity
@Table(name = "application_consent_data_long_term_production_figures")
public class ConsentDataLongTermProductionFigures {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_id")
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  private Application application;

  private Integer year;

  private BigDecimal minOil;

  private BigDecimal maxOil;

  private BigDecimal minGas;

  private BigDecimal maxGas;

  public ConsentDataLongTermProductionFigures() {
  }

  public ConsentDataLongTermProductionFigures(Integer id) {
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

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public BigDecimal getMinOil() {
    return minOil;
  }

  public void setMinOil(BigDecimal minOil) {
    this.minOil = minOil;
  }

  public BigDecimal getMaxOil() {
    return maxOil;
  }

  public void setMaxOil(BigDecimal maxOil) {
    this.maxOil = maxOil;
  }

  public BigDecimal getMinGas() {
    return minGas;
  }

  public void setMinGas(BigDecimal minGas) {
    this.minGas = minGas;
  }

  public BigDecimal getMaxGas() {
    return maxGas;
  }

  public void setMaxGas(BigDecimal maxGas) {
    this.maxGas = maxGas;
  }
}
