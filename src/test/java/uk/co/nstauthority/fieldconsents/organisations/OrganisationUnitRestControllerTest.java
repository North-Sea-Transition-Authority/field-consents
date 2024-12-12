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
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ContextConfiguration(classes = OrganisationUnitRestController.class)
class OrganisationUnitRestControllerTest extends AbstractControllerTest {

  @MockBean
  private OrganisationUnitSearchService organisationUnitSearchService;

  @SecurityTest
  void getOrganisationUnitsForCreator_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForCreator("test", null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getOrganisationUnitsForCreator_assertHttpOk() throws Exception {
    var searchTerm = "1";
    when(organisationUnitSearchService.searchOrganisationUnitsForUser(searchTerm,
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE, user, Set.of(Role.CREATOR)))
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
    when(organisationUnitSearchService.searchOrganisationUnitsForUser(searchTerm,
        OrganisationUnitRestController.ORG_UNIT_SEARCH_PURPOSE, user, Set.of(Role.CREATOR)))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json, orgUnit3Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForCreator(searchTerm, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}, {"id":"2","text":"TEST ORG UNIT 2"}, {"id":"3","text":"TEST ORG UNIT 3"}]}
         """));
  }

  @SecurityTest
  void getOrganisationUnitsForViewer_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForViewer("test", null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getOrganisationUnitsForViewer_assertHttpOk() throws Exception {
    var searchTerm = "1";
    when(organisationUnitSearchService.searchOrganisationUnitsForUser(
            searchTerm,
            OrganisationUnitRestController.ORG_UNIT_WORK_AREA_PURPOSE,
            user,
            RoleGroup.union(
                RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES,
                RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES,
                Set.of(Role.VIEWER, Role.ALLOCATOR, Role.RESPONDER)
            )
        )).thenReturn(List.of(orgUnit1Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForViewer(searchTerm, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}]}
         """));
  }

  @Test
  void getOrganisationUnitsForViewer_assertHttpOk_manyOrgUnits() throws Exception {
    var searchTerm = "OrG UnIt";
    when(organisationUnitSearchService.searchOrganisationUnitsForUser(
        searchTerm,
        OrganisationUnitRestController.ORG_UNIT_WORK_AREA_PURPOSE,
        user,
        RoleGroup.union(
            RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES,
            RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES,
            Set.of(Role.VIEWER, Role.ALLOCATOR, Role.RESPONDER)
        )
    )).thenReturn(List.of(orgUnit1Json, orgUnit2Json, orgUnit3Json));

    mockMvc.perform(get(ReverseRouter.route(on(OrganisationUnitRestController.class)
            .getOrganisationUnitsForViewer(searchTerm, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"TEST ORG UNIT 1"}, {"id":"2","text":"TEST ORG UNIT 2"}, {"id":"3","text":"TEST ORG UNIT 3"}]}
         """));
  }
}
