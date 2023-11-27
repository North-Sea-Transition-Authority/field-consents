package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = BulkCaseActionSearchController.class)
class BulkCaseActionSearchControllerTest extends AbstractControllerTest {

  private static final Class<BulkCaseActionSearchController> CONTROLLER_CLASS = BulkCaseActionSearchController.class;

  @MockBean
  private BulkCaseActionService bulkCaseActionService;

  @Test
  void pageTitle() {
    assertThat(BulkCaseActionSearchController.PAGE_TITLE).isEqualTo("Bulk case actions");
  }

  @SecurityTest
  void getSearchResults_redirectedToLoginUrlWhenUnauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null)))).andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getSearchResults_userDoesNotHavePermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
        .getSearchResults(null)))
        .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getSearchResults() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());
    when(bulkCaseActionService.getApplicationDataItems(user)).thenReturn(applicationDataItems);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"))
        .andExpect(model().attribute("pageTitle", BulkCaseActionSearchController.PAGE_TITLE))
        .andExpect(model().attribute("applicationDataItems", applicationDataItems));
  }
}
