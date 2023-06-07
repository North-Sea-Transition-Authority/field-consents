package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaController.WORK_AREA_TITLE;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  static final String WORK_AREA_VIEW_NAME = "fcs/workarea/workArea";

  @MockBean
  private WorkAreaService workAreaService;

  @MockBean
  private WorkAreaFormService workAreaFormService;

  @MockBean
  private WorkAreaFilterService workAreaFilterService;

  private WorkAreaFilter filter;

  private WorkAreaForm form;

  private List<WorkAreaItem> workAreaItems;

  private RestSearchItem orgUnitRestSearchItem;

  private RestSearchItem assetRestSearchItem;

  @SecurityTest
  void getWorkArea_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @BeforeEach
  void setUp() {
    filter = new WorkAreaFilter();
    filter.setApplicationTypes(List.of(ApplicationType.values()));
    form = WorkAreaFormServiceTestUtil.getWorkAreaFormForDefaultFilter();
    when(workAreaFilterService.getDefaultFilter(user)).thenReturn(filter);
    when(workAreaFormService.getFromFilter(any(WorkAreaFilter.class))).thenReturn(form);
    workAreaItems = List.of(WorkAreaTestUtil.getWorkAreaItem());
    orgUnitRestSearchItem = WorkAreaFormServiceTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(workAreaFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);
    assetRestSearchItem = WorkAreaFormServiceTestUtil.FIELD_REST_SEARCH_ITEM;
    when(workAreaFormService.getPrefilledAsset(any())).thenReturn(assetRestSearchItem);
  }

  @Test
  void getWorkArea_IndustryUser() throws Exception {
    filter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    when(workAreaService.getIndustryWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class))).thenReturn(workAreaItems);
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model);
    assertThat(model)
        .containsEntry("isRegulatorUser", false)
        .containsEntry("workAreaTabs", Collections.emptyList());

    var actualForm = (WorkAreaForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  @SecurityTest
  void getWorkAreaCaseOfficerApplications_whenUserDoesNotHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS)))
        .thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class)
                .getWorkAreaCaseOfficerMyApplications(filter, user)))
                .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaCaseOfficerApplications_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class)
                .getWorkAreaCaseOfficerMyApplications(filter, user)))
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaRegulatorAllApplications_whenUserDoesNotHaveAssignFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class)
                .getWorkAreaRegulatorAllApplications(filter, user)))
                .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void postWorkAreaRegulatorAllApplications_whenUserDoesNotHaveAssignFcsPermission() throws Exception {
    mockMvc.perform(
            post(ReverseRouter.route(on(WorkAreaController.class)
                .postWorkAreaRegulatorAllApplications(filter, user)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void postWorkAreaRegulatorAllApplications_whenUserDoesHaveAssignFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(WorkAreaController.class)
                .postWorkAreaRegulatorAllApplications(filter, user)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void getWorkAreaCaseOfficerApplications_whenUserDoesHaveAssignFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class)
                .getWorkAreaRegulatorAllApplications(filter, user)))
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaCaseOfficerUnassignedApplications_whenUserDoesNotHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS)))
        .thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class)
                .getWorkAreaCaseOfficerUnassignedApplications(filter, user)))
                .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaCaseOfficerUnassignedApplications_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class)
                .getWorkAreaCaseOfficerUnassignedApplications(filter, user)))
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void postWorkAreaCaseOfficerApplications_whenUserDoesNotHaveProcessFcsPermission() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(WorkAreaController.class)
            .postWorkAreaCaseOfficerMyApplications(filter, user)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void postWorkAreaCaseOfficerApplications_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
        post(ReverseRouter.route(on(WorkAreaController.class)
            .postWorkAreaCaseOfficerMyApplications(filter, user)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void postWorkAreaCaseOfficerUnassignedApplications_whenUserDoesNotHaveProcessFcsPermission() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(WorkAreaController.class)
            .postWorkAreaCaseOfficerUnassignedApplications(filter, user)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void postWorkAreaCaseOfficerUnassignedApplications_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);
    mockMvc.perform(
        post(ReverseRouter.route(on(WorkAreaController.class)
            .postWorkAreaCaseOfficerUnassignedApplications(filter, user)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void getWorkArea_RegulatorUser_CaseOfficer() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(workAreaService.getRegulatorWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class), any(WorkAreaTab.class))).thenReturn(workAreaItems);
    var caseOfficerTabs = List.of(WorkAreaTab.MY_APPLICATIONS, WorkAreaTab.UNASSIGNED_APPLICATIONS);
    when(workAreaService.getTabsAvailableToUser(user)).thenReturn(caseOfficerTabs);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model);
    assertThat(model)
        .containsEntry("isRegulatorUser", true)
        .containsEntry("selectedTab", WorkAreaTab.MY_APPLICATIONS.getValue())
        .containsEntry("workAreaTabs", caseOfficerTabs);

    var actualForm = (WorkAreaForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  @Test
  void getWorkArea_RegulatorUser_CaseManager() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);
    when(workAreaService.getRegulatorWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class), any(WorkAreaTab.class))).thenReturn(workAreaItems);
    var caseManagerTabs = List.of(WorkAreaTab.ALL_APPLICATIONS, WorkAreaTab.UNASSIGNED_APPLICATIONS);
    when(workAreaService.getTabsAvailableToUser(user)).thenReturn(caseManagerTabs);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model);
    assertThat(model)
        .containsEntry("isRegulatorUser", true)
        .containsEntry("selectedTab", WorkAreaTab.ALL_APPLICATIONS.getValue())
        .containsEntry("workAreaTabs", caseManagerTabs);

    var actualForm = (WorkAreaForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  private void assertWorkAreaModel(Map<String, Object> model) {
    assertThat(model)
        .containsEntry("workAreaItems", workAreaItems)
        .containsEntry("clearFiltersUrl", ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilter(null, null)))
        .containsEntry("appStatuses", ApplicationVersionStatus.getWorkAreaOptions())
        .containsEntry("appTypes", ApplicationType.getDisplayableOptions())
        .containsEntry("durationTypes", ConsentLengthType.getWorkAreaOptions())
        .containsEntry("prefilledAsset", assetRestSearchItem)
        .containsEntry("assetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser(null, null)))
        .containsEntry("prefilledOperator", orgUnitRestSearchItem)
        .containsEntry("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForEditor(null, null)))
        .containsEntry("pageTitle", WORK_AREA_TITLE);
  }

  @Test
  void filterWorkArea() throws Exception {
    var form = new WorkAreaForm();
    form.setStatuses(Collections.singletonList(ApplicationVersionStatus.IN_PROGRESS));
    form.setApplicationTypes(Collections.singletonList(ApplicationType.PRODUCTION));
    form.setDurationTypes(Collections.singletonList(ConsentLengthType.LONG_TERM));
    var filter = new WorkAreaFilter();
    var expectedRedirectUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null));

    mockMvc.perform(
        post(ReverseRouter.route(on(WorkAreaController.class).filterWorkArea(null, null)))
            .with(csrf())
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("workAreaFilter", filter))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(expectedRedirectUrl));

    assertThat(filter).extracting(
        WorkAreaFilter::getStatuses,
        WorkAreaFilter::getApplicationTypes,
        WorkAreaFilter::getDurationTypes
    ).containsExactly(
        form.getStatuses(),
        form.getApplicationTypes(),
        form.getDurationTypes()
    );
  }

  @Test
  void clearWorkAreaFilter() throws Exception {
    var form = new WorkAreaForm();
    form.setStatuses(Collections.singletonList(ApplicationVersionStatus.IN_PROGRESS));
    form.setApplicationTypes(Collections.singletonList(ApplicationType.PRODUCTION));
    form.setDurationTypes(Collections.singletonList(ConsentLengthType.LONG_TERM));
    var filter = new WorkAreaFilter();
    filter.update(form);
    var expectedRedirectUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null));

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilter(null, null)))
            .with(user(user))
            .flashAttr("workAreaFilter", filter))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(expectedRedirectUrl));

    assertThat(filter).extracting(
        WorkAreaFilter::getStatuses,
        WorkAreaFilter::getApplicationTypes,
        WorkAreaFilter::getDurationTypes
    ).containsOnlyNulls();
  }
}
