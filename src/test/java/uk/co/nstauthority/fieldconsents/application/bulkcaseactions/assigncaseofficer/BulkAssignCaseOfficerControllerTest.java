package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

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
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSearchController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = BulkAssignCaseOfficerController.class)
class BulkAssignCaseOfficerControllerTest extends AbstractControllerTest {

  private static final Class<BulkAssignCaseOfficerController> CONTROLLER_CLASS = BulkAssignCaseOfficerController.class;

  @MockBean
  private BulkCaseActionService bulkCaseActionService;

  @SecurityTest
  void assignCaseOfficer_redirectedToLoginUrlWhenUnauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).assignCaseOfficer(null, null)))).andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void assignCaseOfficer_userDoesNotHavePermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .assignCaseOfficer(null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void assignCaseOfficer() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var session = new MockHttpSession();

    var applicationDataItemWithoutCaseOfficer = applicationDataItemBuilderWithDefaults(1).build();
    var applicationDataItemWithCaseOfficer = applicationDataItemBuilderWithDefaults(2).withCaseOfficer("unit test").build();

    var applicationDataItems = List.of(applicationDataItemWithoutCaseOfficer, applicationDataItemWithCaseOfficer);
    when(bulkCaseActionService.getSelectedApplicationDataItems(session, user)).thenReturn(applicationDataItems);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).assignCaseOfficer(null, null)))
        .session(session)
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/assignCaseOfficer"))
        .andExpect(model().attribute("pageTitle", "Assign case officer"))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(BulkCaseActionSearchController.class).getSearchResults(null, null))))
        .andExpect(model().attribute("applicationDataItems", applicationDataItems))
        .andExpect(model().attributeExists("captionHeadingFunction"))
        .andReturn()
        .getModelAndView();

    var captionHeadingFunction = (Function<ApplicationDataItem, String>) modelAndView.getModel().get("captionHeadingFunction");
    assertThat(captionHeadingFunction.apply(applicationDataItemWithoutCaseOfficer)).isEqualTo("No case officer currently assigned");
    assertThat(captionHeadingFunction.apply(applicationDataItemWithCaseOfficer)).isEqualTo("Current case officer: unit test");
  }

  private ApplicationDataItem.Builder applicationDataItemBuilderWithDefaults(Integer applicationId) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType("")
        .withReference("")
        .withOperator("")
        .withDuration("")
        .withAceFlag("")
        .withAsset("")
        .withGeographicArea("")
        .withStatus("")
        .withCaseOfficer("")
        .withTechnicalReviewer("")
        .withSubmittedDateTime("")
        .withSubmittedBy("");
  }

}
