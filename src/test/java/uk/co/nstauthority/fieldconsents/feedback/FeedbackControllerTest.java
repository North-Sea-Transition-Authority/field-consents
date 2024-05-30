package uk.co.nstauthority.fieldconsents.feedback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = FeedbackController.class)
class FeedbackControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private FeedbackService feedbackService;

  @MockBean
  private FeedbackFormValidator feedbackFormValidator;

  @MockBean
  private ApplicationService applicationService;

  private ApplicationVersion applicationVersion;

  private ServiceUserDetail user;

  @BeforeEach
  void setup() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);

    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @SecurityTest
  void getFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(FeedbackController.class).getFeedback(null))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getFeedback_assertModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeedbackController.class)
            .getFeedback(null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/feedback/feedback"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageName", FeedbackController.PAGE_NAME))
        .andExpect(model().attribute("maxCharacterLength", String.valueOf(FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH)))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(FeedbackController.class).submitFeedback(null, null, null))))
        .andExpect(
            model().attribute("serviceRatings", DisplayableEnumOptionUtil.getDisplayableOptions(ServiceFeedbackRating.class)))
        .andReturn().getModelAndView();
  }

  @SecurityTest
  void submitFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
            .submitFeedback(null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void submitFeedback_assertRedirect() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
            .submitFeedback(null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))));

    verify(feedbackService).saveFeedback(any(), any(), eq(user));
  }

  @Test
  void submitFeedback_whenHasErrors_assertOk() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));

      return invocation;
    }).when(feedbackFormValidator).validate(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
            .submitFeedback(null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/feedback/feedback"));

    verify(feedbackService, never()).saveFeedback(any(), any(), any());
  }

  @SecurityTest
  void getApplicationFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeedbackController.class)
            .getApplicationFeedback(APPLICATION_ID, null))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getApplicationFeedback_assertModelProperties() throws Exception {
    when(applicationService.getApplicationById(APPLICATION_ID))
        .thenReturn(applicationVersion.getApplication());

    mockMvc.perform(get(ReverseRouter.route(on(FeedbackController.class)
            .getApplicationFeedback(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/feedback/feedback"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageName", FeedbackController.PAGE_NAME))
        .andExpect(model().attribute("maxCharacterLength", String.valueOf(FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH)))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(FeedbackController.class).submitApplicationFeedback(APPLICATION_ID, null, null, null))
        ))
        .andExpect(model().attribute(
            "serviceRatings",
            DisplayableEnumOptionUtil.getDisplayableOptions(ServiceFeedbackRating.class)
        ));
  }

  @SecurityTest
  void submitApplicationFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
            .submitFeedback(null, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void submitApplicationFeedback_assertRedirect() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
            .submitApplicationFeedback(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))));

    verify(feedbackService).saveFeedback(eq(applicationVersion), any(), any(), eq(user));
  }

  @Test
  void submitApplicationFeedback_whenHasErrors_assertOk() throws Exception {
    when(applicationService.getApplicationById(APPLICATION_ID))
        .thenReturn(applicationVersion.getApplication());

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));

      return invocation;
    }).when(feedbackFormValidator).validate(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
            .submitApplicationFeedback(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

    verify(feedbackService, never()).saveFeedback(any(), any(), any());
  }
}
