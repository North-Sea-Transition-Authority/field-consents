package uk.co.nstauthority.fieldconsents.application.caseprocessing.revision;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ContextConfiguration(classes = ApplicationRevisionController.class)
class ApplicationRevisionControllerTest extends AbstractApplicationControllerTest {

  private static final int APPLICATION_ID = 1;

  @MockitoBean
  private ApplicationService applicationService;

  @MockitoBean
  private ApplicationRevisionService applicationRevisionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void beforeEach() {
    applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getStartRevision_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationRevisionController.class).getStartRevision(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getStartRevision_userDoesNotHaveReviseConsentCaseProcessingActionItem() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationRevisionController.class).getStartRevision(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getStartRevision_regulatorUser() throws Exception {
    var applicationReference = "Test/application/reference";

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CaseProcessingActionItem.REVISE_CONSENT
    )).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    when(teamQueryService.userIsMemberOfTeamType(user, TeamType.REGULATOR)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationRevisionController.class).getStartRevision(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/revision/startRevision"))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("startRevisionUrl", ReverseRouter.route(on(ApplicationRevisionController.class)
            .startRevision(APPLICATION_ID, null))));
  }

  @SecurityTest
  void getStartRevision_industryUser() throws Exception {
    var applicationReference = "Test/application/reference";

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CaseProcessingActionItem.REVISE_CONSENT
    )).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    when(teamQueryService.userIsMemberOfTeamType(user, TeamType.REGULATOR)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationRevisionController.class).getStartRevision(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/revision/startRevision"))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("startRevisionUrl", ReverseRouter.route(on(ApplicationRevisionController.class)
            .startRevision(APPLICATION_ID, null))));
  }

  @SecurityTest
  void startRevision_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationRevisionController.class).startRevision(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void startRevision_userDoesNotHaveReviseConsentCaseProcessingActionItem() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationRevisionController.class).startRevision(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void startRevision() throws Exception {
    var newApplication = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CaseProcessingActionItem.REVISE_CONSENT
    )).thenReturn(true);
    when(applicationRevisionService.startApplicationRevision(applicationVersion, user)).thenReturn(newApplication);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationRevisionController.class).startRevision(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(newApplication.getId(), null))));
  }
}
