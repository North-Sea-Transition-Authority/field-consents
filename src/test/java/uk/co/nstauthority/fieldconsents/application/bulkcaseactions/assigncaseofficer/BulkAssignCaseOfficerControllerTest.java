package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_2;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSelectedApplicationsForm;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@ContextConfiguration(classes = BulkAssignCaseOfficerController.class)
class BulkAssignCaseOfficerControllerTest extends AbstractControllerTest {

  private static final Class<BulkAssignCaseOfficerController> CONTROLLER_CLASS = BulkAssignCaseOfficerController.class;
  private static final String SESSION_ATTRIBUTE = "bulkCaseActions-assignCaseOfficer";

  @MockBean
  private BulkAssignCaseOfficerFormValidator validator;

  @MockBean
  private BulkAssignCaseOfficerService bulkAssignCaseOfficerService;

  @MockBean
  private BulkCaseActionService bulkCaseActionService;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  private MockHttpSession session;

  @Mock
  private BulkAssignCaseOfficerSessionContext sessionContext;

  @BeforeEach
  void setUp() {
    session = new MockHttpSession();
    session.setAttribute(SESSION_ATTRIBUTE, sessionContext);
  }

  @SecurityTest
  void assignCaseOfficer_redirectedToLoginUrlWhenUnauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).assignCaseOfficer(null, null)))).andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void assignCaseOfficer_userDoesNotHavePermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .assignCaseOfficer(null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void assignCaseOfficer() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    var availableCaseOfficers = List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2);
    when(bulkAssignCaseOfficerService.getAvailableCaseOfficers()).thenReturn(availableCaseOfficers);

    var form = new BulkCaseActionSelectedApplicationsForm();
    when(sessionContext.getSelectedApplicationsForm()).thenReturn(form);

    var applicationDataItemWithoutCaseOfficer = applicationDataItemBuilderWithDefaults(1).build();
    var applicationDataItemWithCaseOfficer = applicationDataItemBuilderWithDefaults(2).withCaseOfficer("unit test").build();

    var applicationDataItemViews = List.of(applicationDataItemWithoutCaseOfficer, applicationDataItemWithCaseOfficer);
    when(bulkCaseActionService.getSelectedApplicationDataItemViews(form.getSelectedApplicationIds(), user)).thenReturn(applicationDataItemViews);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).assignCaseOfficer(null, null)))
        .session(session)
        .with(user(user)))
        .andExpectAll(modelAndViewResultMatchers(applicationDataItemViews, availableCaseOfficers))
        .andReturn()
        .getModelAndView();

    var captionHeadingFunction = (Function<ApplicationDataItemView, String>) modelAndView.getModel().get("captionHeadingFunction");
    assertThat(captionHeadingFunction.apply(applicationDataItemWithoutCaseOfficer)).isEqualTo("No case officer currently assigned");
    assertThat(captionHeadingFunction.apply(applicationDataItemWithCaseOfficer)).isEqualTo("Current case officer: unit test");
  }

  @Test
  void assignCaseOfficer_submitForm() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    var caseOfficer = ENERGY_PORTAL_USER_1;
    var applicationVersions = List.of(new ApplicationVersion(), new ApplicationVersion(), new ApplicationVersion());

    var caseOfficerWuaId = "1";
    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(caseOfficerWuaId))).thenReturn(caseOfficer);
    when(applicationVersionService.getLatestApplicationVersions(Set.of(10, 11, 12))).thenReturn(applicationVersions);

    var bannerMessage = "Success message";
    when(bulkAssignCaseOfficerService.getNotificationBannerSuccessMessage(3, caseOfficer)).thenReturn(bannerMessage);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).assignCaseOfficer(null, null, null, null, null)))
        .session(session)
        .with(user(user))
        .with(csrf())
        .param("caseOfficerWuaId", caseOfficerWuaId)
        .param("selectedApplicationIds", "10")
        .param("selectedApplicationIds", "11")
        .param("selectedApplicationIds", "12"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent(bannerMessage)
            .build()));

    verify(validator).validate(eq(new BulkAssignCaseOfficerForm(caseOfficerWuaId, Set.of("10", "11", "12"))), any(BindingResult.class));
    verify(bulkAssignCaseOfficerService).assignCaseOfficer(applicationVersions, ServiceUserDetail.from(caseOfficer), user);
    verify(sessionContext).clearSelectedApplications();
  }

  @Test
  void assignCaseOfficer_submitInvalidForm() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    var availableCaseOfficers = List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2);
    when(bulkAssignCaseOfficerService.getAvailableCaseOfficers()).thenReturn(availableCaseOfficers);

    var form = new BulkCaseActionSelectedApplicationsForm();
    when(sessionContext.getSelectedApplicationsForm()).thenReturn(form);

    var applicationDataItemWithoutCaseOfficer = applicationDataItemBuilderWithDefaults(1).build();
    var applicationDataItemWithCaseOfficer = applicationDataItemBuilderWithDefaults(2).withCaseOfficer("unit test").build();

    var applicationDataItemViews = List.of(applicationDataItemWithoutCaseOfficer, applicationDataItemWithCaseOfficer);
    when(bulkCaseActionService.getSelectedApplicationDataItemViews(form.getSelectedApplicationIds(), user)).thenReturn(applicationDataItemViews);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("caseOfficerWuaId", "invalid", "validation message");
      return null;
    })
        .when(validator)
        .validate(eq(new BulkAssignCaseOfficerForm(null, null)), any(BindingResult.class));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).assignCaseOfficer(null, null, null, null, null)))
            .session(session)
            .with(user(user))
            .with(csrf()))
        .andExpectAll(modelAndViewResultMatchers(applicationDataItemViews, availableCaseOfficers));

    verify(bulkAssignCaseOfficerService, never()).assignCaseOfficer(any(), any(), any());
    verify(sessionContext, never()).clearSelectedApplications();
  }

  private ResultMatcher[] modelAndViewResultMatchers(
      Collection<ApplicationDataItemView> applicationDataItemViews,
      Collection<EnergyPortalUserDto> availableCaseOfficers
  ) {
    var caseOfficerOptions = availableCaseOfficers
        .stream()
        .collect(StreamUtils.toLinkedHashMap(
            energyPortalUserDto -> energyPortalUserDto.webUserAccountId().toString(),
            EnergyPortalUserDto::displayName
        ));

    return new ResultMatcher[] {
        status().isOk(),
        view().name("fcs/application/bulk-case-actions/assignCaseOfficer"),
        model().attribute("pageTitle", "Assign case officer"),
        model().attribute("backLinkUrl", ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))),
        model().attribute("applicationDataItemViews", applicationDataItemViews),
        model().attributeExists("captionHeadingFunction"),
        model().attribute("caseOfficerOptions", caseOfficerOptions)
    };
  }

  private ApplicationDataItemView.Builder applicationDataItemBuilderWithDefaults(Integer applicationId) {
    return ApplicationDataItemView.newBuilder()
        .withApplicationId(applicationId)
        .withType("")
        .withReference("")
        .withOperator("")
        .withDuration("")
        .withAceFlag(null)
        .withAsset("")
        .withGeographicArea("")
        .withStatus("")
        .withCaseOfficer("")
        .withTechnicalReviewer("")
        .withSubmittedDateTime("")
        .withSubmittedBy("");
  }

}
