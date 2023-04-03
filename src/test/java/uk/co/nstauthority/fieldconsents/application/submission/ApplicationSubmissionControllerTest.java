package uk.co.nstauthority.fieldconsents.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController.PAGE_TITLE;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationSubmissionController.class)
class ApplicationSubmissionControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ApplicationSubmissionService applicationSubmissionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
  }

  @Test
  @WithMockUser
  void submitApplication() throws Exception {
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/submissionConfirmation"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", PAGE_TITLE)
        .containsEntry("caseReference", "CASE_REF")
        .containsEntry("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
  }

  @Test
  @WithMockUser
  void submitApplication_whenNotSubmittable() {
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(false);

    var exception = Assertions.assertThrows(
        Exception.class,
        () -> mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID)))
            .with(csrf()))
    );

    Assertions.assertEquals("Request processing failed; nested exception is java.lang.RuntimeException: The application with id 1 cannot be submitted!", exception.getMessage());
  }

  @Test
  void submitApplication_withUnauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID)))
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}