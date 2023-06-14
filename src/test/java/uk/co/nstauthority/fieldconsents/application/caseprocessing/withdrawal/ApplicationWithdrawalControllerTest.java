package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verifyNoInteractions;
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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = ApplicationWithdrawalController.class)
class ApplicationWithdrawalControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationWithdrawalService applicationWithdrawalService;

  @MockBean
  private WithdrawalRequestFormValidator withdrawalRequestFormValidator;

  @SecurityTest
  void getApplicationWithdrawalRequest_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getApplicationWithdrawalRequest_checkUserPermissionSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationWithdrawalRequest_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Collections.emptyList());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationWithdrawalRequest_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion)).thenReturn(new WithdrawalRequestForm());

    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(true);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_WITHDRAWAL_REQUEST));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalRequestForm"));
  }


  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationWithdrawalRequest(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalRequestForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion)).thenReturn(form);

    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(true);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_WITHDRAWAL_REQUEST));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalRequestForm"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("submitUrl",
            ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .getApplicationWithdrawalRequest(APPLICATION_ID))))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(IndustryCaseProcessingController.class)
                .getIndustryCaseProcessing(APPLICATION_ID, null))));
  }

  @SecurityTest
  void submitApplicationWithdrawalRequest_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .submitApplicationWithdrawalRequest(APPLICATION_ID, null, null, user, null)))
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitApplicationWithdrawalRequest_withEmptyForm(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalRequestForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(true);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_WITHDRAWAL_REQUEST));

    doCallRealMethod().when(withdrawalRequestFormValidator).validate(any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalRequest(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalRequestForm"));

    verifyNoInteractions(applicationWithdrawalService);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitApplicationWithdrawalRequest_withNonEmptyForm(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalRequestForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))).thenReturn(true);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_WITHDRAWAL_REQUEST));

    doCallRealMethod().when(withdrawalRequestFormValidator).validate(any(), any());

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Withdrawal request sent")
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalRequest(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
                .param("requestText", "test")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
