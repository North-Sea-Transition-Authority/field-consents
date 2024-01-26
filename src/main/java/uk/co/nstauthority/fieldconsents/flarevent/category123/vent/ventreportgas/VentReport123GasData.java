package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Table(name = "vent_report_123_gas_data")
public class VentReport123GasData {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Column(name = "category_1_density")
  private BigDecimal category1Density;

  @Column(name = "category_1_inert_percentage")
  private BigDecimal category1InertGasPercentage;

  @Column(name = "category_1_hydro_percentage")
  private BigDecimal category1HydrocarbonPercentage;

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public BigDecimal getCategory1Density() {
    return category1Density;
  }

  public void setCategory1Density(BigDecimal categoryADensity) {
    this.category1Density = categoryADensity;
  }

  public BigDecimal getCategory1InertGasPercentage() {
    return category1InertGasPercentage;
  }

  public void setCategory1InertGasPercentage(BigDecimal category1InertGasPercentage) {
    this.category1InertGasPercentage = category1InertGasPercentage;
  }

  public BigDecimal getCategory1HydrocarbonPercentage() {
    return category1HydrocarbonPercentage;
  }

  public void setCategory1HydrocarbonPercentage(BigDecimal category1HydrocarbonPercentage) {
    this.category1HydrocarbonPercentage = category1HydrocarbonPercentage;
  }
}
