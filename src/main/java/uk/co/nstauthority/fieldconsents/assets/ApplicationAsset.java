package uk.co.nstauthority.fieldconsents.assets;

import com.google.common.annotations.VisibleForTesting;
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

  private Integer assetNo;

  private Integer assetOperatorOuId;

  private String cachedAssetOperatorName;

  public ApplicationAsset() {

  }

  @VisibleForTesting
  public ApplicationAsset(Integer id, ApplicationVersion applicationVersion, Integer fieldId, String cachedFieldName,
                          Integer terminalId, String cachedTerminalName, AssetRole assetRole, Integer assetNo,
                          Integer assetOperatorOuId, String cachedAssetOperatorName) {
    this.id = id;
    this.applicationVersion = applicationVersion;
    this.fieldId = fieldId;
    this.cachedFieldName = cachedFieldName;
    this.terminalId = terminalId;
    this.cachedTerminalName = cachedTerminalName;
    this.assetRole = assetRole;
    this.assetNo = assetNo;
    this.assetOperatorOuId = assetOperatorOuId;
    this.cachedAssetOperatorName = cachedAssetOperatorName;
  }

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

  public boolean isField() {
    return this.fieldId != null;
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

  public boolean isTerminal() {
    return this.terminalId != null;
  }

  public AssetRole getAssetRole() {
    return assetRole;
  }

  public void setAssetRole(AssetRole assetRole) {
    this.assetRole = assetRole;
  }

  public Integer getAssetNo() {
    return assetNo;
  }

  public void setAssetNo(Integer assetNo) {
    this.assetNo = assetNo;
  }

  public Integer getAssetOperatorOuId() {
    return assetOperatorOuId;
  }

  public void setAssetOperatorOuId(Integer assetOperatorOuId) {
    this.assetOperatorOuId = assetOperatorOuId;
  }

  public String getCachedAssetOperatorName() {
    return cachedAssetOperatorName;
  }

  public void setCachedAssetOperatorName(String cachedAssetOperatorName) {
    this.cachedAssetOperatorName = cachedAssetOperatorName;
  }
}
