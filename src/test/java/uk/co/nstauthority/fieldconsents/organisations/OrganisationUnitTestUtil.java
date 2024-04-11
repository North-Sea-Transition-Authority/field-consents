package uk.co.nstauthority.fieldconsents.organisations;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;

public class OrganisationUnitTestUtil {

  public static Integer ORG_GROUP_ID_1 = 1;

  public static String ORG_GROUP_NAME_1 = "Org group 1";

  public static OrganisationGroup ORG_GROUP_1 =
      OrganisationGroup.newBuilder()
          .organisationGroupId(ORG_GROUP_ID_1)
          .name(ORG_GROUP_NAME_1)
          .build();

  public static OrganisationGroup ORG_GROUP_1_WITH_EMPTY_OUS =
      OrganisationGroup.newBuilder()
          .organisationGroupId(ORG_GROUP_ID_1)
          .name(ORG_GROUP_NAME_1)
          .organisationUnits(Collections.emptyList())
          .build();

  public static Integer ORG_GROUP_ID_2 = 2;

  public static String ORG_GROUP_NAME_2 = "Org group 2";

  public static OrganisationGroup ORG_GROUP_2 =
      OrganisationGroup.newBuilder()
          .organisationGroupId(ORG_GROUP_ID_2)
          .name(ORG_GROUP_NAME_2)
          .build();


  public static OrganisationUnit orgUnit1 =
      OrganisationUnit.newBuilder()
          .organisationUnitId(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1)
          .name(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
          .organisationGroups(List.of(ORG_GROUP_1))
          .build();

  public static OrganisationUnitJson orgUnit1Json =
      new OrganisationUnitJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName());

  public static OrganisationUnitWithGroupsJson orgUnit1WithGroupsJson =
      new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
          List.of(OrganisationGroupDto.from(ORG_GROUP_1)));

  public static OrganisationUnitWithGroupsJson orgUnit1With2GroupsJson =
      new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
          List.of(OrganisationGroupDto.from(ORG_GROUP_1), OrganisationGroupDto.from(ORG_GROUP_2)));

  public static OrganisationUnit orgUnit2 =
      OrganisationUnit.newBuilder()
          .organisationUnitId(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_2)
          .name(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_2).build();

  public static OrganisationUnitJson orgUnit2Json =
      new OrganisationUnitJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName());

  public static OrganisationUnitWithGroupsJson orgUnit2WithGroupsJsonNoGroups =
      new OrganisationUnitWithGroupsJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName(), Collections.emptyList());

  public static OrganisationUnit orgUnit3 =
      OrganisationUnit.newBuilder()
          .organisationUnitId(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_3)
          .name(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_3).build();

  public static OrganisationUnitJson orgUnit3Json =
      new OrganisationUnitJson(orgUnit3.getOrganisationUnitId(), orgUnit3.getName());

  public static List<OrganisationUnit> orgUnits =
      List.of(orgUnit1, orgUnit2, orgUnit3);

  public static OrganisationUnit orgUnit4 =
      OrganisationUnit.newBuilder()
          .organisationUnitId(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1)
          .name(ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)
          .organisationGroups(List.of(ORG_GROUP_1, ORG_GROUP_2))
          .build();

}
