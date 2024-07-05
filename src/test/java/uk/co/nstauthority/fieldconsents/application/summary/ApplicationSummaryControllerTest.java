package uk.co.nstauthority.fieldconsents.application.summary;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController.REGULATOR_PROCESSING_REQUIRED_PERMISSIONS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController.CONSULTEE_PROCESSING_REQUIRED_PERMISSIONS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController.INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationSummaryController.class)
class ApplicationSummaryControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationService applicationService;

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenUserIsRegulatorAndHasNoProcessingRequiredPermission_thenSummaryCannotBeViewed(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, REGULATOR_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(false);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenUserIsRegulatorAndHasProcessingRequiredPermission_thenRedirectToApplicationCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, REGULATOR_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(true);
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenUserIsConsulteeAndHasNoProcessingRequiredPermission_thenSummaryCannotBeViewed(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, CONSULTEE_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(false);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(teamService.isConsulteeUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenUserIsConsulteeAndHasProcessingRequiredPermissions_thenRedirectToConsulteeCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, CONSULTEE_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(true);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(teamService.isConsulteeUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedAndUserIsIndustryAndHasNoProcessingRequiredPermissions_thenSummaryCannotBeViewed(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(false);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedAndUserIsIndustryAndHasProcessingRequiredPermissions_thenRedirectToIndustryCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(true);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null))));
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getApplicationSummary_whenInProgressAndUserIsIndustryAndHasProcessingRequiredPermissions_thenRedirectToTaskList(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(true);
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null))));
  }

  @ParameterizedTest
  @MethodSource("getInProgressV2ApplicationVersions")
  void getApplicationSummary_whenInProgressV2AndUserIsIndustryAndHasNoProcessingRequiredPermissions_thenSummaryCannotBeViewed(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(true);
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null))));
  }

  @ParameterizedTest
  @MethodSource("getInProgressV2ApplicationVersions")
  void getApplicationSummary_whenInProgressV2AndUserIsIndustryAndHasProcessingRequiredPermissions_thenRedirectToIndustryCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getApplicationSummary_whenDeletedApplication_thenSummaryCannotBeViewed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithTypeAndStatus(ApplicationType.PRODUCTION, ApplicationVersionStatus.DELETED);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @SecurityTest
  void getApplicationSummary_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  private static Stream<Arguments> getInProgressApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT))
    );
  }

  private static Stream<Arguments> getInProgressV2ApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 2)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.FLARE, 2, 2)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.VENT, 2, 2))
    );
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
