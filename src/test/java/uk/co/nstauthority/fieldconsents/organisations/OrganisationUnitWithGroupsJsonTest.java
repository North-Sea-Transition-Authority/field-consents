package uk.co.nstauthority.fieldconsents.organisations;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_2;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1WithGroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2WithGroupsJsonNoGroups;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

class OrganisationUnitWithGroupsJsonTest {

  @Test
  void from_whenOrgGroupsNull() {
    assertThat(OrganisationUnitWithGroupsJson.from(orgUnit2))
        .isEqualTo(orgUnit2WithGroupsJsonNoGroups);
  }

  @Test
  void from_whenOrgGroupsExist() {
    assertThat(OrganisationUnitWithGroupsJson.from(orgUnit1))
        .usingRecursiveComparison()
        .isEqualTo(orgUnit1WithGroupsJson);
  }

  @Test
  void from_whenOrgGroupsEmpty() {
    var organisationUnit = new OrganisationUnit();
    organisationUnit.setOrganisationUnitId(PRIMARY_OPERATOR_OU_ID_2);
    organisationUnit.setName(CACHED_PRIMARY_OPERATOR_NAME_2);
    organisationUnit.setOrganisationGroups(Collections.emptyList());

    assertThat(OrganisationUnitWithGroupsJson.from(organisationUnit))
        .usingRecursiveComparison()
        .isEqualTo(orgUnit2WithGroupsJsonNoGroups);
  }
}