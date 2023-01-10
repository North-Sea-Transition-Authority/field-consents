package uk.co.nstauthority.fieldconsents.organisations;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

public class OrganisationUnitTestUtil {

  public static OrganisationUnit orgUnit1 =
      OrganisationUnit.newBuilder().organisationUnitId(1).name("OU TEST1").build();

  public static OrganisationUnitJson orgUnit1Json =
      new OrganisationUnitJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName());

  public static OrganisationUnit orgUnit2 =
      OrganisationUnit.newBuilder().organisationUnitId(2).name("OU TEST2").build();

  public static OrganisationUnitJson orgUnit2Json =
      new OrganisationUnitJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName());

  public static OrganisationUnit orgUnit3 =
      OrganisationUnit.newBuilder().organisationUnitId(3).name("OU TEST3").build();

  public static OrganisationUnitJson orgUnit3Json =
      new OrganisationUnitJson(orgUnit3.getOrganisationUnitId(), orgUnit3.getName());

  public static List<OrganisationUnit> orgUnits =
      List.of(orgUnit1, orgUnit2, orgUnit3);

}
