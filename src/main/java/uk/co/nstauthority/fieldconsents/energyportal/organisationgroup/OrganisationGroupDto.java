package uk.co.nstauthority.fieldconsents.energyportal.organisationgroup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroupEmailDomain;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public class OrganisationGroupDto implements SearchSelectable {
  private Integer organisationGroupId;
  private String organisationGroupName;
  private List<String> emailDomains;

  public static OrganisationGroupDto from(OrganisationGroup organisationGroup) {
    var orgGroup = new OrganisationGroupDto();
    orgGroup.setOrganisationGroupId(organisationGroup.getOrganisationGroupId());
    orgGroup.setOrganisationGroupName(organisationGroup.getName());
    orgGroup.setEmailDomains(organisationGroup.getEmailDomains());
    return orgGroup;
  }

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

  public List<String> getEmailDomains() {
    return emailDomains;
  }

  public void setEmailDomains(List<OrganisationGroupEmailDomain> emailDomains) {
    this.emailDomains = Optional.ofNullable(emailDomains)
        .orElse(Collections.emptyList())
        .stream()
        .map(OrganisationGroupEmailDomain::getDomain)
        .toList();
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
