package uk.co.nstauthority.fieldconsents.workarea;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;

public class WorkAreaForm {

  String referenceNumber;
  List<ApplicationVersionStatus> statuses;
  List<ApplicationType> applicationTypes;
  List<ConsentLengthType> durationTypes;
  Integer operatorId;
  String assetKey;
  List<GeographicArea> geographicAreas;
  List<AssetTypeWithShore> assetTypesWithShore;

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(String reference) {
    this.referenceNumber = reference;
  }

  public List<ApplicationVersionStatus> getStatuses() {
    return statuses;
  }

  public void setStatuses(List<ApplicationVersionStatus> statuses) {
    this.statuses = statuses;
  }

  public List<ApplicationType> getApplicationTypes() {
    return applicationTypes;
  }

  public void setApplicationTypes(
      List<ApplicationType> applicationTypes) {
    this.applicationTypes = applicationTypes;
  }

  public List<ConsentLengthType> getDurationTypes() {
    return durationTypes;
  }

  public void setDurationTypes(List<ConsentLengthType> durationTypes) {
    this.durationTypes = durationTypes;
  }

  public Integer getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Integer operatorId) {
    this.operatorId = operatorId;
  }

  public String getAssetKey() {
    return assetKey;
  }

  public void setAssetKey(String assetKey) {
    this.assetKey = assetKey;
  }

  public List<GeographicArea> getGeographicAreas() {
    return geographicAreas;
  }

  public void setGeographicAreas(List<GeographicArea> geographicArea) {
    this.geographicAreas = geographicArea;
  }

  public List<AssetTypeWithShore> getAssetTypesWithShore() {
    return assetTypesWithShore;
  }

  public void setAssetTypesWithShore(List<AssetTypeWithShore> assetTypes) {
    this.assetTypesWithShore = assetTypes;
  }
}
