package uk.co.nstauthority.fieldconsents.production;

import java.math.BigDecimal;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

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

  public void setOilMinValue(BigDecimal oilMinValue) {
    this.oilMinValue = oilMinValue;
  }

  public BigDecimal getOilMaxValue() {
    return oilMaxValue;
  }

  public void setOilMaxValue(BigDecimal oilMaxValue) {
    this.oilMaxValue = oilMaxValue;
  }

  public BigDecimal getGasMinValue() {
    return gasMinValue;
  }

  public void setGasMinValue(BigDecimal gasMinValue) {
    this.gasMinValue = gasMinValue;
  }

  public BigDecimal getGasMaxValue() {
    return gasMaxValue;
  }

  public void setGasMaxValue(BigDecimal gasMaxValue) {
    this.gasMaxValue = gasMaxValue;
  }
}
