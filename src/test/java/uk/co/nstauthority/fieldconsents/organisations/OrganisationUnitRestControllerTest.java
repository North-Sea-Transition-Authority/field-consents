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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@WithMockUser
@ContextConfiguration(classes = OrganisationUnitRestController.class)
class OrganisationUnitRestControllerTest extends AbstractControllerTest {

  @MockBean
  OrganisationUnitService organisationUnitService;

  @Test
  void getOrganisationUnitSearchResults_assertHttpOk() throws Exception {
    when(organisationUnitService.searchOrganisationUnits("1",
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE))
        .thenReturn(List.of(orgUnit1Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitSearchResults("1"))))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"OU TEST1"}]}
         """));
  }

  @Test
  void getOrganisationUnitSearchResults_assertHttpOk_manyOrgUnits() throws Exception {
    when(organisationUnitService.searchOrganisationUnits("oU",
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json, orgUnit3Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitSearchResults("oU"))))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"OU TEST1"}, {"id":"2","text":"OU TEST2"}, {"id":"3","text":"OU TEST3"}]}
         """));
  }
}
