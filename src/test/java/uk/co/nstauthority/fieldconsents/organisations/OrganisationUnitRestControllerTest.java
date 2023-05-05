package uk.co.nstauthority.fieldconsents.organisations;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit3Json;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@WithMockUser
@ContextConfiguration(classes = OrganisationUnitRestController.class)
class OrganisationUnitRestControllerTest extends AbstractControllerTest {

  @Test
  void getOrganisationUnitSearchResults_assertHttpOk() throws Exception {
    when(organisationUnitService.searchOrganisationUnits("1",
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE))
        .thenReturn(List.of(orgUnit1Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitSearchResults("1"))))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}]}
         """));
  }

  @Test
  void getOrganisationUnitSearchResults_assertHttpOk_manyOrgUnits() throws Exception {
    when(organisationUnitService.searchOrganisationUnits("OrG UnIt",
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json, orgUnit3Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitSearchResults("OrG UnIt"))))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}, {"id":"2","text":"TEST ORG UNIT 2"}, {"id":"3","text":"TEST ORG UNIT 3"}]}
         """));
  }
}
