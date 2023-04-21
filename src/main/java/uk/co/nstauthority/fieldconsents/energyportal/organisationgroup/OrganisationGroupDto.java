package uk.co.nstauthority.fieldconsents.energyportal.organisationgroup;

import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public class OrganisationGroupDto implements SearchSelectable {
  private Integer organisationGroupId;
  private String organisationGroupName;

  public Integer getOrganisationGroupId() {
    return organisationGroupId;
  }

  public void setOrganisationGroupId(Integer organisationGroupId) {
    this.organisationGroupId = organisationGroupId;
  }

  public String getOrganisationGroupName() {
    return organisationGroupName;
  }

  public void setOrganisationGroupName(String organisationGroupName) {
    this.organisationGroupName = organisationGroupName;
  }

  @Override
  public String getSelectionId() {
    return organisationGroupId.toString();
  }

  @Override
  public String getSelectionText() {
    return organisationGroupName;
  }
}
