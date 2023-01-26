package uk.co.nstauthority.fieldconsents.application.assetlicences;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;

@Entity
@Table(name = "application_asset_licences")
public class ApplicationAssetLicence {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @ManyToOne
  @JoinColumn(name = "application_asset_id")
  private ApplicationAsset applicationAsset;

  private Integer licenceId;

  private String cachedLicenceRef;

  public ApplicationAssetLicence() {
  }

  public ApplicationAssetLicence(ApplicationVersion applicationVersion,
                                 ApplicationAsset applicationAsset,
                                 Integer licenceId,
                                 String cachedLicenceRef) {
    this.applicationVersion = applicationVersion;
    this.applicationAsset = applicationAsset;
    this.licenceId = licenceId;
    this.cachedLicenceRef = cachedLicenceRef;
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

  public ApplicationAsset getApplicationAsset() {
    return applicationAsset;
  }

  public void setApplicationAsset(ApplicationAsset applicationAsset) {
    this.applicationAsset = applicationAsset;
  }

  public Integer getLicenceId() {
    return licenceId;
  }

  public void setLicenceId(Integer licenceId) {
    this.licenceId = licenceId;
  }

  public String getCachedLicenceRef() {
    return cachedLicenceRef;
  }

  public void setCachedLicenceRef(String cachedLicenceRef) {
    this.cachedLicenceRef = cachedLicenceRef;
  }
}
