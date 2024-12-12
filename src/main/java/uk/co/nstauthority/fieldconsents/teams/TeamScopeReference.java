package uk.co.nstauthority.fieldconsents.teams;

import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;

public interface TeamScopeReference {

  String ORGANISATION_GROUP_ID = "ORGANISATION_GROUP_ID";

  String getId();

  String getType();

  static TeamScopeReference from(OrganisationGroup organisationGroup) {
    return from(String.valueOf(organisationGroup.getOrganisationGroupId()), ORGANISATION_GROUP_ID);
  }

  static TeamScopeReference from(String id, String type) {
    return new TeamScopeReference() {
      @Override
      public String getId() {
        return id;
      }

      @Override
      public String getType() {
        return type;
      }
    };
  }
}