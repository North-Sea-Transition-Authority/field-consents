package uk.co.nstauthority.fieldconsents.production;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

/**
 * This class represents all entities with an id and other details related to oil and gas entered on a production form.
 */
@MappedSuperclass
public class ProductionRow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private BigDecimal oilMinValue;

  private BigDecimal oilMaxValue;

  private BigDecimal gasMinValue;

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

  public BigDecimal getOilMinValue() {
    return oilMinValue;
  }

  public String getOilMinValueString() {
    return DecimalFormatUtils.bigDecimalToFormattedString(oilMinValue);
  }

  public void setOilMinValue(BigDecimal oilMinValue) {
    this.oilMinValue = oilMinValue;
  }

  public BigDecimal getOilMaxValue() {
    return oilMaxValue;
  }

  public String getOilMaxValueString() {
    return DecimalFormatUtils.bigDecimalToFormattedString(oilMaxValue);
  }

  public void setOilMaxValue(BigDecimal oilMaxValue) {
    this.oilMaxValue = oilMaxValue;
  }

  public BigDecimal getGasMinValue() {
    return gasMinValue;
  }

  public String getGasMinValueString() {
    return DecimalFormatUtils.bigDecimalToFormattedString(gasMinValue);
  }

  public void setGasMinValue(BigDecimal gasMinValue) {
    this.gasMinValue = gasMinValue;
  }

  public BigDecimal getGasMaxValue() {
    return gasMaxValue;
  }

  public String getGasMaxValueString() {
    return DecimalFormatUtils.bigDecimalToFormattedString(gasMaxValue);
  }

  public void setGasMaxValue(BigDecimal gasMaxValue) {
    this.gasMaxValue = gasMaxValue;
  }
}
