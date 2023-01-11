package uk.co.nstauthority.fieldconsents.assets;

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

@Entity
@Table(name = "application_assets")
public class ApplicationAsset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Integer fieldId;

  private String cachedFieldName;

  private Integer terminalId;

  private String cachedTerminalName;

  @Enumerated(EnumType.STRING)
  private AssetRole assetRole;

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Integer getFieldId() {
    return fieldId;
  }

  public void setFieldId(Integer fieldId) {
    this.fieldId = fieldId;
  }

  public String getCachedFieldName() {
    return cachedFieldName;
  }

  public void setCachedFieldName(String fieldName) {
    this.cachedFieldName = fieldName;
  }

  public Integer getTerminalId() {
    return terminalId;
  }

  public void setTerminalId(Integer terminalId) {
    this.terminalId = terminalId;
  }

  public String getCachedTerminalName() {
    return cachedTerminalName;
  }

  public void setCachedTerminalName(String terminalName) {
    this.cachedTerminalName = terminalName;
  }

  public AssetRole getAssetRole() {
    return assetRole;
  }

  public void setAssetRole(AssetRole assetRole) {
    this.assetRole = assetRole;
  }
}
