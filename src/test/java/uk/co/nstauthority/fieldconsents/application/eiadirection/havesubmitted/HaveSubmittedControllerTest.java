package uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirection;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionBuilder;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose.ProjectPurposeController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationJson;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;

@ContextConfiguration(classes = HaveSubmittedController.class)
class HaveSubmittedControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/eia-screening/have-submitted-form";
  private static final String PETS_URL = "/pets";
  private static final int SAT_ID = 123;

  @MockBean
  private HaveSubmittedFormValidator validator;

  @MockBean
  private EiaDirectionService eiaDirectionService;

  @MockBean
  private PetsApplicationService petsApplicationService;

  private ApplicationVersion applicationVersion;
  private Integer applicationId;
  private EiaDirection eiaDirection;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationId = applicationVersion.getApplication().getId();
    eiaDirection = EiaDirectionBuilder.newBuilder()
        .withId(1)
        .withApplicationVersion(applicationVersion)
        .build();

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId)).thenReturn(
        applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(applicationId))
        .thenReturn(Optional.ofNullable(applicationVersion));
  }

  @SecurityTest
  void getForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(HaveSubmittedController.class)
            .getForm(applicationId))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(HaveSubmittedController.class)
            .getForm(applicationId)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getForm() throws Exception {
    when(eiaDirectionService.findEiaDirection(applicationVersion))
        .thenReturn(Optional.of(eiaDirection));

    when(eiaDirectionService.getEiaDirectionRestUrl())
        .thenReturn(PETS_URL);

    eiaDirection.setHaveSubmittedEiaDirection(true);
    eiaDirection.setSatId(SAT_ID);

    var model = mockMvc.perform(get(ReverseRouter.route(on(HaveSubmittedController.class)
            .getForm(applicationId)))
            .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model).contains(
        entry("form", new HaveSubmittedForm(true, SAT_ID)),
        entry("backLinkUrl", ReverseRouter.route(on(ProjectPurposeController.class).getForm(applicationId))),
        entry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null))),
        entry("petsSearchRestUrl", PETS_URL),
        entry("prefilledEiaDirectionRef", RestSearchItem.EMPTY_REST_SEARCH_ITEM)
    );
  }

  @SecurityTest
  void saveForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(HaveSubmittedController.class)
            .saveForm(applicationId, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(HaveSubmittedController.class)
            .saveForm(applicationId, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void saveForm() throws Exception {
    var petsApplication = PetsApplicationJson.fromCachedInformation(SAT_ID, "ref");
    when(petsApplicationService.getEiaDirectionById(eq(SAT_ID), anyString()))
        .thenReturn(petsApplication);

    mockMvc.perform(post(ReverseRouter.route(on(HaveSubmittedController.class)
            .saveForm(applicationId, null, null)))
            .param("haveSubmittedEiaDirection", "true")
            .param("satId", String.valueOf(SAT_ID))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null))));

    verify(validator).validate(
        eq(new HaveSubmittedForm(true, SAT_ID)),
        any(BindingResult.class)
    );
    verify(eiaDirectionService).updateEiaDirection(applicationVersion, true, petsApplication);
  }

  @Test
  void saveForm_withValidationError() throws Exception {
    var petsApplication = PetsApplicationJson.fromCachedInformation(SAT_ID, "ref");
    when(petsApplicationService.getEiaDirectionById(eq(SAT_ID), anyString()))
        .thenReturn(petsApplication);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("haveSubmittedEiaDirection", "mandatory", "Validation message");
      return null;
    })
        .when(validator)
        .validate(
            eq(new HaveSubmittedForm(true, SAT_ID)),
            any(BindingResult.class)
        );

    when(eiaDirectionService.getEiaDirectionRestUrl()).thenReturn(PETS_URL);

    mockMvc.perform(post(ReverseRouter.route(on(HaveSubmittedController.class)
            .saveForm(applicationId, null, null)))
            .param("haveSubmittedEiaDirection", "true")
            .param("satId", String.valueOf(SAT_ID))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(eiaDirectionService, never()).updateEiaDirection(any(), any(), any());
  }

}
