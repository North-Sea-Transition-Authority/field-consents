package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Audited
@Table(name = "flare_report_123_gas_data")
public class FlareReport123GasData {

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

  @Column(name = "category_2_density")
  private BigDecimal category2Density;

  @Column(name = "category_2_inert_percentage")
  private BigDecimal category2InertGasPercentage;

  @Column(name = "category_2_hydro_percentage")
  private BigDecimal category2HydrocarbonPercentage;

  @Column(name = "category_3_density")
  private BigDecimal category3Density;

  @Column(name = "category_3_inert_percentage")
  private BigDecimal category3InertGasPercentage;

  @Column(name = "category_3_hydro_percentage")
  private BigDecimal category3HydrocarbonPercentage;

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

  public BigDecimal getCategory2Density() {
    return category2Density;
  }

  public void setCategory2Density(BigDecimal category2Density) {
    this.category2Density = category2Density;
  }

  public BigDecimal getCategory2InertGasPercentage() {
    return category2InertGasPercentage;
  }

  public void setCategory2InertGasPercentage(BigDecimal category2InertGasPercentage) {
    this.category2InertGasPercentage = category2InertGasPercentage;
  }

  public BigDecimal getCategory2HydrocarbonPercentage() {
    return category2HydrocarbonPercentage;
  }

  public void setCategory2HydrocarbonPercentage(BigDecimal category2HydrocarbonPercentage) {
    this.category2HydrocarbonPercentage = category2HydrocarbonPercentage;
  }

  public BigDecimal getCategory3Density() {
    return category3Density;
  }

  public void setCategory3Density(BigDecimal category3Density) {
    this.category3Density = category3Density;
  }

  public BigDecimal getCategory3InertGasPercentage() {
    return category3InertGasPercentage;
  }

  public void setCategory3InertGasPercentage(BigDecimal category3InertGasPercentage) {
    this.category3InertGasPercentage = category3InertGasPercentage;
  }

  public BigDecimal getCategory3HydrocarbonPercentage() {
    return category3HydrocarbonPercentage;
  }

  public void setCategory3HydrocarbonPercentage(BigDecimal category3HydrocarbonPercentage) {
    this.category3HydrocarbonPercentage = category3HydrocarbonPercentage;
  }
}
