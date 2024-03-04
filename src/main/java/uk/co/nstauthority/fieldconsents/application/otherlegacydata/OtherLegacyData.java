package uk.co.nstauthority.fieldconsents.application.otherlegacydata;

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
@Table(name = "application_other_legacy_data")
public class OtherLegacyData {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Boolean increaseInProduction;

  private String esReference;

  private Integer upliftPercentage;

  private String fieldLocation;

  private BigDecimal previousYearConsentHistory;

  private BigDecimal previousYearActuals;

  private String terminalName;

  private String terminalLocation;

  private Boolean projectUnderEiaRegs;

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Boolean getIncreaseInProduction() {
    return increaseInProduction;
  }

  public void setIncreaseInProduction(Boolean increaseInProduction) {
    this.increaseInProduction = increaseInProduction;
  }

  public String getEsReference() {
    return esReference;
  }

  public void setEsReference(String esReference) {
    this.esReference = esReference;
  }

  public Integer getUpliftPercentage() {
    return upliftPercentage;
  }

  public void setUpliftPercentage(Integer upliftPercentage) {
    this.upliftPercentage = upliftPercentage;
  }

  public String getFieldLocation() {
    return fieldLocation;
  }

  public void setFieldLocation(String fieldLocation) {
    this.fieldLocation = fieldLocation;
  }

  public BigDecimal getPreviousYearConsentHistory() {
    return previousYearConsentHistory;
  }

  public void setPreviousYearConsentHistory(BigDecimal previousYearConsentHistory) {
    this.previousYearConsentHistory = previousYearConsentHistory;
  }

  public BigDecimal getPreviousYearActuals() {
    return previousYearActuals;
  }

  public void setPreviousYearActuals(BigDecimal previousYearActuals) {
    this.previousYearActuals = previousYearActuals;
  }

  public String getTerminalName() {
    return terminalName;
  }

  public void setTerminalName(String terminalName) {
    this.terminalName = terminalName;
  }

  public String getTerminalLocation() {
    return terminalLocation;
  }

  public void setTerminalLocation(String terminalLocation) {
    this.terminalLocation = terminalLocation;
  }

  public Boolean getProjectUnderEiaRegs() {
    return projectUnderEiaRegs;
  }

  public void setProjectUnderEiaRegs(Boolean projectUnderEiaRegs) {
    this.projectUnderEiaRegs = projectUnderEiaRegs;
  }
}
