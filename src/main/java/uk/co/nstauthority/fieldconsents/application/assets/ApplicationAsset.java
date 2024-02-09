package uk.co.nstauthority.fieldconsents.application.assets;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@Entity
@Table(name = "application_assets")
public class ApplicationAsset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Enumerated(EnumType.STRING)
  private AssetType assetType;

  private Integer assetId;

  private String cachedAssetName;

  @Enumerated(EnumType.STRING)
  private AssetRole assetRole;

  private Integer assetNo;

  private Integer assetOperatorOuId;

  private String cachedAssetOperatorName;

  public Integer getId() {
    return id;
  }

  @VisibleForTesting
  public void setId(Integer id) {
    this.id = id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public AssetType getAssetType() {
    return assetType;
  }

  public void setAssetType(AssetType assetType) {
    this.assetType = assetType;
  }

  public Integer getAssetId() {
    return assetId;
  }

  public void setAssetId(Integer assetId) {
    this.assetId = assetId;
  }

  public String getCachedAssetName() {
    return cachedAssetName;
  }

  public void setCachedAssetName(String cachedAssetName) {
    this.cachedAssetName = cachedAssetName;
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

  public boolean isTerminal() {
    return assetType == AssetType.TERMINAL;
  }

  public boolean isField() {
    return assetType == AssetType.FIELD;
  }

  public boolean isPrimary() {
    return assetRole == AssetRole.PRIMARY;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplicationAsset that)) {
      return false;
    }
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
