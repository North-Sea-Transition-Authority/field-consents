package uk.co.nstauthority.fieldconsents.application.summary;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionNotFoundException;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ContextConfiguration(classes = ApplicationSummaryController.class)
class ApplicationSummaryControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";
  private static final Set<Role> REGULATOR_ROLES = EnumSet.of(
      Role.CASE_OFFICER,
      Role.CASE_MANAGER,
      Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
      Role.TECHNICAL_REVIEWER,
      Role.VIEWER
  );
  private static final Set<Role> CONSULTEE_ROLES = EnumSet.of(
      Role.ALLOCATOR,
      Role.RESPONDER
  );
  private static final Set<Role> INDUSTRY_ROLES = EnumSet.of(
      Role.CREATOR,
      Role.EDITOR,
      Role.SUBMITTER,
      Role.FINANCE_ADMINISTRATOR,
      Role.VIEWER,
      Role.CONSENT_RECIPIENT
  );

  @MockitoBean
  private ApplicationSummaryService applicationSummaryService;

  @MockitoBean
  private ApplicationService applicationService;

  @ParameterizedSecurityTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenUserDoesNotHaveApplicationAccess_thenSummaryCannotBeViewed(ApplicationVersion applicationVersion) throws Exception {
    // Interceptor mocks
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedSecurityTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenCaseOfficerUserHasApplicationAccess_thenRedirectToApplicationCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // Interceptor mocks
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(fieldConsentsAccessService.userHasAnyRegulatorRole(user, REGULATOR_ROLES)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))));
  }

  @ParameterizedSecurityTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenResponderUserHasApplicationAccess_thenRedirectToConsulteeCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // Interceptor mocks
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(fieldConsentsAccessService.userHasAnyConsulteeRole(user, applicationVersion, CONSULTEE_ROLES)).thenReturn(true);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))));
  }

  @ParameterizedSecurityTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenIndustryUserAttemptsToAccessSubmittedApplication_thenSummaryCannotBeViewed(ApplicationVersion applicationVersion) throws Exception {
    // Interceptor mocks
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());

    when(fieldConsentsAccessService.getApplicationRolesForUser(applicationVersion, user)).thenReturn(Set.of(Role.EDITOR));
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedSecurityTest
  @MethodSource({"getSubmittedApplicationVersions", "getInProgressV2ApplicationVersions"})
  void getApplicationSummary_whenIndustryUserHasEditorRole_thenRedirectToIndustryCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // Interceptor mocks
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, applicationVersion, INDUSTRY_ROLES)).thenReturn(true);
    when(applicationSummaryService.getSummarySections(applicationVersion, user)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null))));
  }

  @Test
  void getApplicationSummary_whenApplicationNotFound_thenSummaryCannotBeViewed() throws Exception {
    // Interceptor mocks
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenThrow(ApplicationVersionNotFoundException.class);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void getApplicationSummary_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
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
