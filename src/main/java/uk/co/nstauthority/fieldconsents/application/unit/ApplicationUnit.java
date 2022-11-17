package uk.co.nstauthority.fieldconsents.application.unit;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Entity
@Table(name = "application_units")
class ApplicationUnit {

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

  public ApplicationUnit() {
  }

  public ApplicationUnit(ApplicationVersion applicationVersion, FlareVentUnit flareCategoryUnit,
                         FlareVentUnit ventCategoryUnit, ProductionUnit productionOilUnit,
                         ProductionUnit productionGasUnit) {
    this.applicationVersion = applicationVersion;
    this.flareCategoryUnit = flareCategoryUnit;
    this.ventCategoryUnit = ventCategoryUnit;
    this.productionOilUnit = productionOilUnit;
    this.productionGasUnit = productionGasUnit;
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
}
