package uk.co.nstauthority.fieldconsents.energyportal.organisationgroup;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

public class OrganisationGroupTestUtil {
  private OrganisationGroupTestUtil() {
  }

  public static Integer ORGANISATION_GROUP_ID_1 = 1;

  public static String ORGANISATION_GROUP_NAME_1 = "Organisation group 1";

  public static OrganisationGroup ORGANISATION_GROUP_1 =
      OrganisationGroup.newBuilder()
          .organisationGroupId(ORGANISATION_GROUP_ID_1)
          .name(ORGANISATION_GROUP_NAME_1)
          .organisationUnits(List.of(OrganisationUnitTestUtil.orgUnit1))
          .build();

  public static Integer ORGANISATION_GROUP_ID_2 = 2;

  public static String ORGANISATION_GROUP_NAME_2 = "Organisation group 2";

  public static OrganisationGroup ORGANISATION_GROUP_2 =
      OrganisationGroup.newBuilder()
          .organisationGroupId(ORGANISATION_GROUP_ID_2)
          .name(ORGANISATION_GROUP_NAME_2)
          .organisationUnits(List.of(OrganisationUnitTestUtil.orgUnit2))
          .build();

  public static OrganisationGroupDto createOrganisationGroupDto(int organisationGroupId, String organisationGroupName) {
    var organisationGroupDto = new OrganisationGroupDto();
    organisationGroupDto.setOrganisationGroupId(organisationGroupId);
    organisationGroupDto.setOrganisationGroupName(organisationGroupName);
    return organisationGroupDto;
  }
}
