package uk.co.nstauthority.fieldconsents.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController.PAGE_TITLE;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationSubmissionController.class)
class ApplicationSubmissionControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationSubmissionService applicationSubmissionService;

  @MockBean
  private ApplicationService applicationService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
  }

  @Test
  void submitApplication() throws Exception {
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/submissionConfirmation"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", PAGE_TITLE)
        .containsEntry("applicationReference", APPLICATION_REFERENCE)
        .containsEntry("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)));
  }

  @Test
  void submitApplication_whenNotSubmittable() {
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(false);

    assertThatThrownBy(
        () -> mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
    ).hasMessageContaining("The application with id 1 cannot be submitted!");
  }

  @SecurityTest
  void submitApplication_withUnauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}
