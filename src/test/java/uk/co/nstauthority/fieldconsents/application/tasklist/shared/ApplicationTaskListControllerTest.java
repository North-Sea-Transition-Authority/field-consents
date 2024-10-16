package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.applicationUpdateRequestView;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ProductionConsentCheckResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.licenceexpiry.LicenceExpiryService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.licences.LicenceView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = ApplicationTaskListController.class)
class ApplicationTaskListControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationTaskListService applicationTaskListService;

  @MockBean
  private ApplicationContextService applicationContextService;

  @MockBean
  private ApplicationUpdateService applicationUpdateService;

  @MockBean
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsentService consentService;

  @MockBean
  private LicenceExpiryService licenceExpiryService;

  private List<TaskListSection> flareTaskListSections;

  private ApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    flareTaskListSections = TaskListTestUtil.getFlareTaskListSectionWithItems(APPLICATION_ID);
    applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(FieldTestUtil.field1Json)
        .withPrimaryOperator("Primary operator")
        .withApplicationVersionStatus(ApplicationVersionStatus.IN_PROGRESS)
        .build();
  }

  @SecurityTest
  void getTaskList_withUnauthorizedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getTaskList_withFlareApplication() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var productionConsentCheckResult = ProductionConsentCheckResult.WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Flare application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", true))
        .andExpect(model().attributeDoesNotExist("warning"))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withFlareApplication_fieldWithoutActiveConsent() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var productionConsentCheckResult = ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, true);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Flare application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", true))
        .andExpect(model().attribute("warning", productionConsentCheckResult.getWarning()))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withFlareApplication_facilityWithoutActiveConsent() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var productionConsentCheckResult = ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Flare application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", true))
        .andExpect(model().attributeDoesNotExist("warning"))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withFlareApplication_doesntHavePermissionToDelete() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var productionConsentCheckResult = ProductionConsentCheckResult.WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Flare application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", false))
        .andExpect(model().attributeDoesNotExist("warning"))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withVentApplication() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    var productionConsentCheckResult = ProductionConsentCheckResult.WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Vent application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", true))
        .andExpect(model().attributeDoesNotExist("warning"))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withVentApplication_fieldWithoutActiveConsent() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    var productionConsentCheckResult = ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, true);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Vent application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", true))
        .andExpect(model().attribute("warning", productionConsentCheckResult.getWarning()))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withVentApplication_facilityWithoutActiveConsent() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    var productionConsentCheckResult = ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, true);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Vent application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", true))
        .andExpect(model().attributeDoesNotExist("warning"))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withVentApplication_doesntHavePermissionToDelete() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    var productionConsentCheckResult = ProductionConsentCheckResult.WITHIN_ACTIVE_CONSENT;

    stubBaseServiceCalls(applicationVersion);
    stubProductionConsentCheck(applicationVersion, productionConsentCheckResult);
    stubPermissionCheckToDeleteApplication(applicationVersion, false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("pageTitle", "Vent application"))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("hasPermissionToDeleteApplication", false))
        .andExpect(model().attributeDoesNotExist("warning"))
        .andExpect(model().attributeExists("taskListSections"));
  }

  @Test
  void getTaskList_withProductionApplication() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(
        ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion,
        RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(false);
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("pageTitle", "Production application"),
            entry("applicationContext", applicationContext),
            entry("hasPermissionToDeleteApplication", false)
        )
        .containsKey("taskListSections");
  }

  @Test
  void getTaskList_withApplicationUpdateInProgress() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(
        ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion,
        RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(false);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(true);
    when(applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
        .thenReturn(applicationUpdateRequestView);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("pageTitle", "Production application"),
            entry("applicationContext", applicationContext),
            entry("hasPermissionToDeleteApplication", false)
        )
        .containsEntry("applicationUpdateRequestView", applicationUpdateRequestView)
        .containsKey("taskListSections");
  }

  @Test
  void getTaskList_withLicenceExpiringWithinDuration() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(
        ApplicationType.PRODUCTION);

    var licenceView = new LicenceView("Test123", "25th of December 2024");
    var expiringLicences = List.of(licenceView);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion,
        RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(false);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(false);
    when(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion))
        .thenReturn(expiringLicences);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("expiringLicences", expiringLicences));
  }

  @Test
  void getTaskList_withoutLicenceExpiringWithinDuration() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(
        ApplicationType.PRODUCTION);

    List<LicenceView> expiringLicences = List.of();

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion,
        RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(false);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(false);
    when(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion))
        .thenReturn(expiringLicences);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andExpect(model().attribute("expiringLicences", List.of()));
  }

  private void stubPermissionCheckToDeleteApplication(
      ApplicationVersion applicationVersion,
      boolean canDeleteApplication
  ) {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion,
        RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(canDeleteApplication);
  }

  private void stubProductionConsentCheck(
      ApplicationVersion applicationVersion,
      ProductionConsentCheckResult productionConsentCheckResult
  ) {
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
    when(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion))
        .thenReturn(productionConsentCheckResult);
  }

  private void stubBaseServiceCalls(ApplicationVersion applicationVersion) {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        applicationVersion);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
  }
}
