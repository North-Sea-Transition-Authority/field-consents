package uk.co.nstauthority.fieldconsents.application.unit;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Entity
@Table(name = "application_units")
public class ApplicationUnit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Enumerated(EnumType.STRING)
  private FlareVentUnit flareCategoryUnit;

  @Enumerated(EnumType.STRING)
  private FlareVentUnit ventCategoryUnit;

  @Enumerated(EnumType.STRING)
  private ProductionUnit productionOilUnit;

  @Enumerated(EnumType.STRING)
  private ProductionUnit productionGasUnit;

  @Enumerated(EnumType.STRING)
  private FlareVentUnit flareGasDensityUnit;

  @Enumerated(EnumType.STRING)
  private FlareVentUnit flareGasContentUnit;

  @Enumerated(EnumType.STRING)
  private FlareVentUnit ventGasDensityUnit;

  @Enumerated(EnumType.STRING)
  private FlareVentUnit ventGasContentUnit;

  @Enumerated(EnumType.STRING)
  private EmissionCategoryType emissionCategoryType;

  public ApplicationUnit() {
  }

  public ApplicationUnit(ApplicationVersion applicationVersion, FlareVentUnit flareCategoryUnit,
                         FlareVentUnit ventCategoryUnit, ProductionUnit productionOilUnit,
                         ProductionUnit productionGasUnit, FlareVentUnit flareGasDensityUnit,
                         FlareVentUnit flareGasContentUnit, FlareVentUnit ventGasDensityUnit,
                         FlareVentUnit ventGasContentUnit, EmissionCategoryType emissionCategoryType) {
    this.applicationVersion = applicationVersion;
    this.flareCategoryUnit = flareCategoryUnit;
    this.ventCategoryUnit = ventCategoryUnit;
    this.productionOilUnit = productionOilUnit;
    this.productionGasUnit = productionGasUnit;
    this.flareGasDensityUnit = flareGasDensityUnit;
    this.flareGasContentUnit = flareGasContentUnit;
    this.ventGasDensityUnit = ventGasDensityUnit;
    this.ventGasContentUnit = ventGasContentUnit;
    this.emissionCategoryType = emissionCategoryType;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public FlareVentUnit getFlareCategoryUnit() {
    return flareCategoryUnit;
  }

  public void setFlareCategoryUnit(FlareVentUnit flareCategoryUnit) {
    this.flareCategoryUnit = flareCategoryUnit;
  }

  public FlareVentUnit getVentCategoryUnit() {
    return ventCategoryUnit;
  }

  public void setVentCategoryUnit(FlareVentUnit ventCategoryUnit) {
    this.ventCategoryUnit = ventCategoryUnit;
  }

  public ProductionUnit getProductionOilUnit() {
    return productionOilUnit;
  }

  public void setProductionOilUnit(ProductionUnit productionOilUnit) {
    this.productionOilUnit = productionOilUnit;
  }

  public ProductionUnit getProductionGasUnit() {
    return productionGasUnit;
  }

  public void setProductionGasUnit(ProductionUnit productionGasUnit) {
    this.productionGasUnit = productionGasUnit;
  }

  public FlareVentUnit getFlareGasDensityUnit() {
    return flareGasDensityUnit;
  }

  public void setFlareGasDensityUnit(FlareVentUnit gasDataDensity) {
    this.flareGasDensityUnit = gasDataDensity;
  }

  public FlareVentUnit getFlareGasContentUnit() {
    return flareGasContentUnit;
  }

  public FlareVentUnit getVentGasDensityUnit() {
    return ventGasDensityUnit;
  }

  public void setVentGasDensityUnit(FlareVentUnit ventGasDataDensityUnit) {
    this.ventGasDensityUnit = ventGasDataDensityUnit;
  }

  public FlareVentUnit getVentGasContentUnit() {
    return ventGasContentUnit;
  }

  public void setVentGasContentUnit(FlareVentUnit ventGasDataContentUnit) {
    this.ventGasContentUnit = ventGasDataContentUnit;
  }

  public void setFlareGasContentUnit(FlareVentUnit gasDataMass) {
    this.flareGasContentUnit = gasDataMass;
  }

  public EmissionCategoryType getEmissionCategoryType() {
    return emissionCategoryType;
  }

  public void setEmissionCategoryType(EmissionCategoryType emissionCategoryType) {
    this.emissionCategoryType = emissionCategoryType;
  }
}
