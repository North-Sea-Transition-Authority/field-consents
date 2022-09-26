package uk.co.nstauthority.fieldconsents.production.annual;

import java.math.BigDecimal;
import java.time.Month;
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
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Entity
@Table(name = "annual_production_months")
public class AnnualProductionMonth {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Integer year;

  @Enumerated(EnumType.STRING)
  private Month month;

  @Enumerated(EnumType.STRING)
  private ProductionUnit oilMinUnit;

  private BigDecimal oilMinValue;

  @Enumerated(EnumType.STRING)
  private ProductionUnit oilMaxUnit;

  private BigDecimal oilMaxValue;

  @Enumerated(EnumType.STRING)
  private ProductionUnit gasMinUnit;

  private BigDecimal gasMinValue;

  @Enumerated(EnumType.STRING)
  private ProductionUnit gasMaxUnit;

  private BigDecimal gasMaxValue;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Month getMonth() {
    return month;
  }

  public void setMonth(Month month) {
    this.month = month;
  }

  public ProductionUnit getOilMinUnit() {
    return oilMinUnit;
  }

  public void setOilMinUnit(ProductionUnit oilMinUnit) {
    this.oilMinUnit = oilMinUnit;
  }

  public BigDecimal getOilMinValue() {
    return oilMinValue;
  }

  public void setOilMinValue(BigDecimal oilMinValue) {
    this.oilMinValue = oilMinValue;
  }

  public ProductionUnit getOilMaxUnit() {
    return oilMaxUnit;
  }

  public void setOilMaxUnit(ProductionUnit oilMaxUnit) {
    this.oilMaxUnit = oilMaxUnit;
  }

  public BigDecimal getOilMaxValue() {
    return oilMaxValue;
  }

  public void setOilMaxValue(BigDecimal oilMaxValue) {
    this.oilMaxValue = oilMaxValue;
  }

  public ProductionUnit getGasMinUnit() {
    return gasMinUnit;
  }

  public void setGasMinUnit(ProductionUnit gasMinUnit) {
    this.gasMinUnit = gasMinUnit;
  }

  public BigDecimal getGasMinValue() {
    return gasMinValue;
  }

  public void setGasMinValue(BigDecimal gasMinValue) {
    this.gasMinValue = gasMinValue;
  }

  public ProductionUnit getGasMaxUnit() {
    return gasMaxUnit;
  }

  public void setGasMaxUnit(ProductionUnit gasMaxUnit) {
    this.gasMaxUnit = gasMaxUnit;
  }

  public BigDecimal getGasMaxValue() {
    return gasMaxValue;
  }

  public void setGasMaxValue(BigDecimal gasMaxValue) {
    this.gasMaxValue = gasMaxValue;
  }
}
