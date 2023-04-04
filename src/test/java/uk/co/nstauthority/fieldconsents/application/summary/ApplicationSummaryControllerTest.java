package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationSummaryController.class)
class ApplicationSummaryControllerTest extends AbstractControllerTest {

  private static final String PAGE_TITLE = "Check your answers before submitting";

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationSubmissionService applicationSubmissionService;

  @WithMockUser
  @ParameterizedTest
  @MethodSource("getApplicationVersions")
  void getSummary(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(false);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getSummary(APPLICATION_ID)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationSummary"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", PAGE_TITLE)
        .containsEntry("accordionId", applicationVersion.getId())
        .containsKey("summarySections")
        .containsEntry("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID)))
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
        .containsEntry("isSubmittable", false);
  }

  private static Stream<Arguments> getApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT))
    );
  }

  @Test
  void getSummary_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getSummary(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }
}