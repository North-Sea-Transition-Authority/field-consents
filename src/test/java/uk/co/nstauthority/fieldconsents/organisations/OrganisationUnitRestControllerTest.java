package uk.co.nstauthority.fieldconsents.organisations;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit3Json;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.CREATE_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = OrganisationUnitRestController.class)
class OrganisationUnitRestControllerTest extends AbstractControllerTest {

  @Test
  void getOrganisationUnitsForCreator_assertHttpOk() throws Exception {
    var searchTerm = "1";
    when(organisationUnitService.searchOrganisationUnitsForUser(searchTerm,
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE, user, CREATE_FCS_APPLICATIONS))
        .thenReturn(List.of(orgUnit1Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForCreator(searchTerm, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}]}
         """));
  }

  @Test
  void getOrganisationUnitsForCreator_assertHttpOk_manyOrgUnits() throws Exception {
    var searchTerm = "OrG UnIt";
    when(organisationUnitService.searchOrganisationUnitsForUser(searchTerm,
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE, user, CREATE_FCS_APPLICATIONS))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json, orgUnit3Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForCreator(searchTerm, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}, {"id":"2","text":"TEST ORG UNIT 2"}, {"id":"3","text":"TEST ORG UNIT 3"}]}
         """));
  }

  @Test
  void getOrganisationUnitsForEditor_assertHttpOk() throws Exception {
    var searchTerm = "1";
    when(organisationUnitService.searchOrganisationUnitsForUser(searchTerm,
        OrganisationUnitRestController.ORG_UNIT_WORK_AREA_PURPOSE, user, EDIT_FCS_APPLICATIONS))
        .thenReturn(List.of(orgUnit1Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForEditor(searchTerm, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}]}
         """));
  }

  @Test
  void getOrganisationUnitsForEditor_assertHttpOk_manyOrgUnits() throws Exception {
    var searchTerm = "OrG UnIt";
    when(organisationUnitService.searchOrganisationUnitsForUser(searchTerm,
        OrganisationUnitRestController.ORG_UNIT_WORK_AREA_PURPOSE, user, EDIT_FCS_APPLICATIONS))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json, orgUnit3Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForEditor(searchTerm, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}, {"id":"2","text":"TEST ORG UNIT 2"}, {"id":"3","text":"TEST ORG UNIT 3"}]}
         """));
  }
}
