package uk.co.nstauthority.fieldconsents.fee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = FeePeriodController.class)
class FeePeriodControllerTest extends AbstractControllerTest {

  private static final UUID FEE_PERIOD_ID = UUID.randomUUID();

  @MockBean
  private FieldConsentsFeePeriodService fieldConsentsFeePeriodService;

  @MockBean
  private FeePeriodService feePeriodService;

  @MockBean
  private FeePeriodFormValidator feePeriodFormValidator;

  @SecurityTest
  void getFeePeriods_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getFeePeriods())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getFeePeriods_userDoesNotHaveManageFeePeriodsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getFeePeriods()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getFeePeriods() throws Exception {
    var feePeriodSummaryViews = List.of(
        FeePeriodSummaryView.from(
            new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null),
            FeePeriodStatus.ACTIVE,
            false
        )
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(fieldConsentsFeePeriodService.getFeePeriodSummaryViews()).thenReturn(feePeriodSummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getFeePeriods()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/fee/feePeriods"))
        .andExpect(model().attribute("feePeriodSummaryViews", feePeriodSummaryViews))
        .andExpect(model().attribute("createFeePeriodUrl", ReverseRouter.route(on(FeePeriodController.class)
            .getCreateFeePeriod())));
  }

  @SecurityTest
  void getViewFeePeriod_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getViewFeePeriod(FEE_PERIOD_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getViewFeePeriod_userDoesNotHaveManageFeePeriodsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getViewFeePeriod(FEE_PERIOD_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getViewFeePeriod() throws Exception {
    var feePeriodDto = new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null);

    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);
    var feeLineDtos = List.of(feeLineDto);

    var feeLineViews = List.of(FeeLineView.from(feeLineDto));

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getFeePeriodDtoByIdOrThrow(FEE_PERIOD_ID)).thenReturn(feePeriodDto);
    when(feePeriodService.getFeeLineDtosByFeePeriodId(FEE_PERIOD_ID)).thenReturn(feeLineDtos);
    when(fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos)).thenReturn(feeLineViews);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getViewFeePeriod(FEE_PERIOD_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/fee/viewFeePeriod"))
        .andExpect(model().attribute("pageTitle", FeePeriodUtil.getTitle(feePeriodDto)))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())))
        .andExpect(model().attribute("feeLineViews", feeLineViews));
  }

  @SecurityTest
  void getCreateFeePeriod_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getCreateFeePeriod())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getCreateFeePeriod_userDoesNotHaveManageFeePeriodsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getCreateFeePeriod()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getCreateFeePeriod() throws Exception {
    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);
    var feeLineDtos = List.of(feeLineDto);

    var feeLineViews = List.of(FeeLineView.from(feeLineDto));

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getLatestFeePeriodFeeLineDtos()).thenReturn(feeLineDtos);
    when(fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos)).thenReturn(feeLineViews);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class).getCreateFeePeriod()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/fee/createOrEditFeePeriod"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", FeePeriodController.CREATE_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", FeePeriodController.CREATE_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("feeLineViews", feeLineViews))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));
  }

  @SecurityTest
  void createFeePeriod_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .createFeePeriod(null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void createFeePeriod_userDoesNotHaveManageFeePeriodsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .createFeePeriod(null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void createFeePeriod_invalidForm() throws Exception {
    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);
    var feeLineDtos = List.of(feeLineDto);

    var feeLineViews = List.of(FeeLineView.from(feeLineDto));

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getLatestFeePeriodFeeLineDtos()).thenReturn(feeLineDtos);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(3);
      bindingResult.rejectValue("startDateInput.dayInput.inputValue", "code", "message");
      return bindingResult;
    })
        .when(feePeriodFormValidator)
        .validate(any(), isNull(), eq(feeLineDtos), any());

    when(fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos)).thenReturn(feeLineViews);

    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .createFeePeriod(null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/fee/createOrEditFeePeriod"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", FeePeriodController.CREATE_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", FeePeriodController.CREATE_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("feeLineViews", feeLineViews))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));


    verify(fieldConsentsFeePeriodService, never()).createFeePeriod(any(), any());
  }

  @Test
  void createFeePeriod() throws Exception {
    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);
    var feeLineDtos = List.of(feeLineDto);

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getLatestFeePeriodFeeLineDtos()).thenReturn(feeLineDtos);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Fee period created")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .createFeePeriod(null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));

    verify(fieldConsentsFeePeriodService).createFeePeriod(any(), eq(user));
  }

  @SecurityTest
  void getEditFeePeriod_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class)
            .getEditFeePeriod(FEE_PERIOD_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getEditFeePeriod_userDoesNotHaveManageFeePeriodsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class)
            .getEditFeePeriod(FEE_PERIOD_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getEditFeePeriod_feePeriodNotEditable() throws Exception {
    var feePeriodDto = new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null);

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getFeePeriodDtoByIdOrThrow(FEE_PERIOD_ID)).thenReturn(feePeriodDto);
    when(fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto)).thenReturn(false);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.INFO)
        .withTitle("Fee period is not editable")
        .withHeadingContent("Fee periods which have started cannot be edited")
        .build();

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class)
            .getEditFeePeriod(FEE_PERIOD_ID, null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));
  }

  @Test
  void getEditFeePeriod() throws Exception {
    var feePeriodDto = new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null);

    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);
    var feeLineDtos = List.of(feeLineDto);

    var prefilledForm = new FeePeriodForm();

    var feeLineViews = List.of(FeeLineView.from(feeLineDto));

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getFeePeriodDtoByIdOrThrow(FEE_PERIOD_ID)).thenReturn(feePeriodDto);
    when(fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto)).thenReturn(true);
    when(feePeriodService.getFeeLineDtosByFeePeriodId(FEE_PERIOD_ID)).thenReturn(feeLineDtos);
    when(fieldConsentsFeePeriodService.getPrefilledFeePeriodForm(feePeriodDto, feeLineDtos))
        .thenReturn(prefilledForm);
    when(fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos)).thenReturn(feeLineViews);

    mockMvc.perform(get(ReverseRouter.route(on(FeePeriodController.class)
            .getEditFeePeriod(FEE_PERIOD_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/fee/createOrEditFeePeriod"))
        .andExpect(model().attribute("form", prefilledForm))
        .andExpect(model().attribute("pageTitle", FeePeriodController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", FeePeriodController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("feeLineViews", feeLineViews))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));
  }

  @SecurityTest
  void editFeePeriod_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .editFeePeriod(FEE_PERIOD_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void editFeePeriod_userDoesNotHaveManageFeePeriodsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .editFeePeriod(FEE_PERIOD_ID, null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void editFeePeriod_feePeriodNotEditable() throws Exception {
    var feePeriodDto = new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null);

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getFeePeriodDtoByIdOrThrow(FEE_PERIOD_ID)).thenReturn(feePeriodDto);
    when(fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto)).thenReturn(false);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.INFO)
        .withTitle("Fee period is not editable")
        .withHeadingContent("Fee periods which have started cannot be edited")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .editFeePeriod(FEE_PERIOD_ID, null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));

    verify(fieldConsentsFeePeriodService, never()).editFeePeriod(any(), any(), any());
  }

  @Test
  void editFeePeriod_invalidForm() throws Exception {
    var feePeriodDto = new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null);

    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);
    var feeLineDtos = List.of(feeLineDto);

    var feeLineViews = List.of(FeeLineView.from(feeLineDto));

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getFeePeriodDtoByIdOrThrow(FEE_PERIOD_ID)).thenReturn(feePeriodDto);
    when(fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto)).thenReturn(true);
    when(feePeriodService.getFeeLineDtosByFeePeriodId(FEE_PERIOD_ID)).thenReturn(feeLineDtos);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(3);
      bindingResult.rejectValue("startDateInput.dayInput.inputValue", "code", "message");
      return bindingResult;
    })
        .when(feePeriodFormValidator)
        .validate(any(), eq(feePeriodDto), eq(feeLineDtos), any());

    when(fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos)).thenReturn(feeLineViews);

    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .editFeePeriod(FEE_PERIOD_ID, null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/fee/createOrEditFeePeriod"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", FeePeriodController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", FeePeriodController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("feeLineViews", feeLineViews))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));

    verify(fieldConsentsFeePeriodService, never()).editFeePeriod(any(), any(), any());
  }

  @Test
  void editFeePeriod() throws Exception {
    var feePeriodDto = new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null);

    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);
    var feeLineDtos = List.of(feeLineDto);

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_FEE_PERIODS))).thenReturn(true);
    when(feePeriodService.getFeePeriodDtoByIdOrThrow(FEE_PERIOD_ID)).thenReturn(feePeriodDto);
    when(fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto)).thenReturn(true);
    when(feePeriodService.getFeeLineDtosByFeePeriodId(FEE_PERIOD_ID)).thenReturn(feeLineDtos);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Fee period edited")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(FeePeriodController.class)
            .editFeePeriod(FEE_PERIOD_ID, null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(FeePeriodController.class)
            .getFeePeriods())));

    verify(fieldConsentsFeePeriodService).editFeePeriod(eq(feePeriodDto), any(), eq(user));
  }
}
