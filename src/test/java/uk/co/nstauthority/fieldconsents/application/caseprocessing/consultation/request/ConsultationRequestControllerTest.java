package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;

@ContextConfiguration(classes = ConsultationRequestController.class)
class ConsultationRequestControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/consultation/requestForm";
  private static final String APPLICATION_REFERENCE = "12345";
  private static final Team TEAM = TeamTestUtil.Builder().withDisplayName("test").build();

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsultationRequestFormValidator validator;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private Clock clock;

  private ApplicationVersion applicationVersion;

  private Integer applicationId;

  private Instant deadline;
  private String deadlineDate;
  private String deadlineHours;
  private String deadlineMinutes;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationId = applicationVersion.getApplication().getId();

    when(clock.instant()).thenReturn(Instant.now());

    deadline = clock.instant().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES);
    var date = Date.from(deadline);
    deadlineDate = DateFormatUtils.format(date, "dd/MM/yyyy");
    deadlineHours = DateFormatUtils.format(date, "HH");
    deadlineMinutes = DateFormatUtils.format(date, "mm");

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId)).thenReturn(applicationVersion);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(
        Optional.of(applicationVersion));
  }

  @SecurityTest
  void getConsultationRequestForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsultationRequestController.class)
            .getConsultationRequestForm(applicationId))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getConsultationRequestForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(ConsultationRequestController.class)
            .getConsultationRequestForm(applicationId)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getConsultationRequestForm() throws Exception {
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
    when(consultationService.getConsultationTeam()).thenReturn(TEAM);

    var model = mockMvc.perform(get(ReverseRouter.route(on(ConsultationRequestController.class)
            .getConsultationRequestForm(applicationId)))
            .with(user(user)))
        .andExpect(view().name(VIEW_NAME))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsEntry("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(applicationId, null,null)))
        .containsEntry("pageTitle", "Request consultation from %s".formatted(TEAM.getDisplayName()))
        .containsEntry("applicationReference", APPLICATION_REFERENCE)
        .containsEntry("form", ConsultationRequestForm.empty());
  }

  @SecurityTest
  void submitConsultationRequestForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
            .submitConsultationRequestForm(applicationId, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitConsultationRequestForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Set.of());

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
            .submitConsultationRequestForm(applicationId, null, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitConsultationRequestForm() throws Exception {
    when(consultationService.getConsultationTeam()).thenReturn(TEAM);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Consultation request has been sent to %s".formatted(TEAM.getDisplayName()))
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
            .submitConsultationRequestForm(applicationId, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("deadlineDate", deadlineDate)
            .param("deadlineHours", deadlineHours)
            .param("deadlineMinutes", deadlineMinutes))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsultationController.class).getConsultations(APPLICATION_ID, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(validator).validate(eq(new ConsultationRequestForm(deadlineDate, deadlineHours, deadlineMinutes)), any(BindingResult.class));
    verify(consultationService).requestConsultation(applicationVersion, deadline, user);
  }

  @Test
  void submitConsultationRequestForm_validationFailed() throws Exception {
    when(consultationService.getConsultationTeam()).thenReturn(TEAM);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("deadlineDate", "invalid", "This date is invalid");
      return null;
    }).when(validator).validate(any(ConsultationRequestForm.class), any(BindingResult.class));

    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
    when(consultationService.getConsultationTeam()).thenReturn(TEAM);

    var model = mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
            .submitConsultationRequestForm(applicationId, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("deadlineDate", deadlineDate)
            .param("deadlineHours", deadlineHours)
            .param("deadlineMinutes", deadlineMinutes))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    var form = new ConsultationRequestForm(deadlineDate, deadlineHours, deadlineMinutes);

    assertThat(model)
        .containsEntry("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(applicationId, null, null)))
        .containsEntry("pageTitle", "Request consultation from %s".formatted(TEAM.getDisplayName()))
        .containsEntry("applicationReference", APPLICATION_REFERENCE)
        .containsEntry("form", form);

    verify(validator).validate(eq(form), any(BindingResult.class));
    verify(consultationService, never()).requestConsultation(any(), any(), any());
  }

}
