package uk.co.nstauthority.fieldconsents.query;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;

public class ApplicationDataFilterForm implements Serializable {

  @Serial
  private static final long serialVersionUID = 1463021385744461455L;

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

  public void setApplicationTypes(List<ApplicationType> applicationTypes) {
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

  public String prettyPrint() {
    String prettyString = "{\n";
    prettyString += StringUtils.isEmpty(referenceNumber) ? "" : "referenceNumber: " + referenceNumber + "\n";
    prettyString += CollectionUtils.isEmpty(statuses) ? "" : "statuses: " + statuses + "\n";
    prettyString += CollectionUtils.isEmpty(applicationTypes) ? "" : "applicationTypes: " + applicationTypes + "\n";
    prettyString += CollectionUtils.isEmpty(durationTypes) ? "" : "durationTypes: " + durationTypes + "\n";
    prettyString += operatorId == null ? "" : "operatorId: " + operatorId + "\n";
    prettyString += CollectionUtils.isEmpty(geographicAreas) ? "" : "geographicAreas: " + geographicAreas + "\n";
    prettyString += CollectionUtils.isEmpty(assetTypesWithShore) ? "" :
        "assetTypesWithShore: " + assetTypesWithShore + "\n";
    prettyString += StringUtils.isEmpty(submittedYear) ? "" : "submittedYear: " + submittedYear + "\n";
    prettyString += StringUtils.isEmpty(licenceReference) ? "" : "licenceReference: " + licenceReference + "\n";
    prettyString += approvedForIssue == null ? "" : "approvedForIssue: " + approvedForIssue + "\n";
    prettyString += "}\n";
    return prettyString;
  }
}
