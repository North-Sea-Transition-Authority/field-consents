package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response;

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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_SUBMIT_REVIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_ID_1;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_UPLOAD_COMPONENT_ATTRIBUTES;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = TechnicalReviewResponseController.class)
class TechnicalReviewResponseControllerTest extends AbstractApplicationControllerTest {

  private static final Instant NOW = Instant.now();

  private static final String VIEW_NAME = "fcs/application/review/technicalReviewResponse";
  private static final Class<TechnicalReviewResponseController> CONTROLLER_CLASS = TechnicalReviewResponseController.class;
  private static final Instant TECHNICAL_REVIEW_DEADLINE = NOW.plus(1, ChronoUnit.DAYS);
  private static final String TECHNICAL_REVIEW_REQUEST_TEXT = "Request text";
  private static final String APPLICATION_REFERENCE = "application reference";

  @MockBean
  private TechnicalReviewService technicalReviewService;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @MockBean
  private TechnicalReviewResponseFormValidator validator;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @Captor
  private ArgumentCaptor<TechnicalReviewResponseForm> formCaptor;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    technicalReview = new TechnicalReview();
    technicalReview.setId(TECHNICAL_REVIEW_ID_1);
    technicalReview.setDeadlineDateTime(TECHNICAL_REVIEW_DEADLINE);
    technicalReview.setRequestText(TECHNICAL_REVIEW_REQUEST_TEXT);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of(TECHNICAL_REVIEWER_SUBMIT_REVIEW));
  }

  @SecurityTest
  void getForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID, user))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID, user)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getForm() throws Exception {
    mockGetFormInteractions();

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID, user)))
            .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsAllEntriesOf(Map.of(
            "applicationReference", APPLICATION_REFERENCE,
            "backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(APPLICATION_ID,
                null, null, null
            )),
            "approveRadio", TechnicalReviewResponseType.APPROVE,
            "rejectRadio", TechnicalReviewResponseType.REJECT,
            "fileUploadAttributes", FILE_UPLOAD_COMPONENT_ATTRIBUTES,
            "summarySections", Collections.emptyList(),
            "accordionId", applicationVersion.getId(),
            "wideSummaryDisplay", false,
            "technicalReviewSummaryView", TechnicalReviewSummaryView.from(technicalReview)
        ))
        .extracting(m -> m.get("form"))
        .usingRecursiveComparison()
        .isEqualTo(TechnicalReviewResponseForm.empty());
  }

  @SecurityTest
  void submitForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitForm(APPLICATION_ID, null, null, null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitForm(APPLICATION_ID, null, null, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitForm() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(technicalReviewService.getOpenTechnicalReview(applicationVersion)).thenReturn(technicalReview);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);

    var rejectionReason = "rejection reason";

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
        .submitForm(APPLICATION_ID, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("responseType", TechnicalReviewResponseType.REJECT.toString())
        .param("rejectionReason.inputValue", rejectionReason)
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Technical review submitted for application " + APPLICATION_REFERENCE).build()));

    verify(validator).validate(formCaptor.capture(), any(BindingResult.class));
    assertThat(formCaptor.getValue())
        .extracting(
            TechnicalReviewResponseForm::responseType,
            f -> f.rejectionReason().getInputValue()
        )
        .containsExactly(
            TechnicalReviewResponseType.REJECT,
            rejectionReason
        );

    verify(technicalReviewService).saveTechnicalReviewResponse(
        applicationVersion,
        technicalReview,
        user,
        TechnicalReviewResponseType.REJECT,
        null,
        rejectionReason,
        Collections.emptyList()
    );
  }

  @Test
  void submitForm_validationFailed() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("responseType", "required", "validation message");
      return null;
    })
        .when(validator)
        .validate(any(TechnicalReviewResponseForm.class), any(BindingResult.class));

    mockGetFormInteractions();

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitForm(APPLICATION_ID, null, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME));

    verify(technicalReviewService, never()).saveTechnicalReviewResponse(any(), any(), any(), any(), any(), any(), any());
  }

  private void addTechnicalReviewAttributes(ModelAndView modelAndView) {
    modelAndView
        .addObject("summarySections", Collections.emptyList())
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", false);
  }

  private void mockGetFormInteractions() {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(technicalReviewService.getOpenTechnicalReview(applicationVersion)).thenReturn(technicalReview);
    when(fileControllerHelperService.fileUploadComponentAttributes(eq(Collections.emptyList()), eq(TechnicalReviewResponseFileController.class), any(), any())).thenReturn(FILE_UPLOAD_COMPONENT_ATTRIBUTES);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
    doAnswer(invocation -> {
      addTechnicalReviewAttributes(invocation.getArgument(1, ModelAndView.class));
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class), eq(user));
  }

}
