package uk.co.nstauthority.fieldconsents.query;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;

public class ApplicationDataFilterForm {

  public static final String APPROVED_FOR_ISSUE_FILTER_OPTION = "APPROVED_FOR_ISSUE";

  String referenceNumber;
  List<ApplicationVersionStatus> statuses;
  List<ApplicationType> applicationTypes;
  List<ConsentLengthType> durationTypes;
  Integer operatorId;
  List<GeographicArea> geographicAreas;
  List<AssetTypeWithShore> assetTypesWithShore;
  String submittedYear;
  String licenceReference;
  Boolean approvedForIssue;

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

  public String getSubmittedYear() {
    return submittedYear;
  }

  public void setSubmittedYear(String submittedYear) {
    this.submittedYear = submittedYear;
  }

  public String getLicenceReference() {
    return licenceReference;
  }

  public void setLicenceReference(String licenceReference) {
    this.licenceReference = licenceReference;
  }

  public Boolean getApprovedForIssue() {
    return approvedForIssue;
  }

  public void setApprovedForIssue(Boolean approvedForIssue) {
    this.approvedForIssue = approvedForIssue;
  }

  public void clearFilter() {
    setReferenceNumber(null);
    setStatuses(null);
    setApplicationTypes(null);
    setDurationTypes(null);
    setOperatorId(null);
    setGeographicAreas(null);
    setAssetTypesWithShore(null);
    setSubmittedYear(null);
    setLicenceReference(null);
    setApprovedForIssue(null);
  }

  public void update(ApplicationDataFilterForm form) {
    setReferenceNumber(form.getReferenceNumber());
    setStatuses(form.getStatuses());
    setApplicationTypes(form.getApplicationTypes());
    setDurationTypes(form.getDurationTypes());
    setOperatorId(form.getOperatorId());
    setGeographicAreas(form.getGeographicAreas());
    setAssetTypesWithShore(form.getAssetTypesWithShore());
    setSubmittedYear(form.getSubmittedYear());
    setLicenceReference(form.getLicenceReference());
    setApprovedForIssue(form.getApprovedForIssue());
  }
}
