package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaController.WORK_AREA_TITLE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  static final String WORK_AREA_VIEW_NAME = "fcs/workarea/workArea";

  @MockBean
  private WorkAreaService workAreaService;

  @MockBean
  private WorkAreaFilterFormService workAreaFormService;

  @MockBean
  private WorkAreaFilterService workAreaFilterService;

  @MockBean
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  @MockBean
  private CaseAssignmentService caseAssignmentService;

  @MockBean
  private TechnicalReviewAssignmentService technicalReviewAssignmentService;

  private WorkAreaFilter filter;

  private WorkAreaFilterForm form;

  private List<ApplicationDataItemView> workAreaItemViews;

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
    form = ApplicationDataFilterFormTestUtil.getWorkAreaFormForDefaultFilter();
    when(workAreaFilterService.getDefaultFilter(user)).thenReturn(filter);
    when(workAreaFormService.getFromFilter(any(WorkAreaFilter.class))).thenReturn(form);
    workAreaItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());
    orgUnitRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);
    assetRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD1_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(any())).thenReturn(assetRestSearchItem);
  }

  @Test
  void getWorkArea_IndustryUser() throws Exception {
    filter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    when(workAreaService.getIndustryWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class))).thenReturn(
        workAreaItemViews);
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model, null);
    assertThat(model)
        .containsEntry("isWorkAreaWithTabs", false)
        .containsEntry("workAreaTabs", Collections.emptyList());

    var actualForm = (WorkAreaFilterForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  @SecurityTest
  void getWorkAreaCaseOfficerApplications_whenUserDoesNotHaveProcessFcsPermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaCaseOfficerMyApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaCaseOfficerApplications_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaCaseOfficerMyApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaMyTechnicalReviews_whenUserDoesNotHaveTechnicalReviewFcsPermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaMyTechnicalReviews(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaMyTechnicalReviews_whenUserDoesHaveTechnicalReviewFcsPermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaMyTechnicalReviews(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaCaseOfficerUnassignedApplications_whenUserDoesNotHaveProcessFcsPermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaCaseOfficerUnassignedApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaCaseOfficerUnassignedApplications_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, Set.of(Role.CASE_OFFICER, Role.CASE_MANAGER)))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaCaseOfficerUnassignedApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaAllTechnicalReviews_whenUserDoesNotHaveTechnicalReviewFcsPermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaAllTechnicalReviews(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaAllTechnicalReviews_whenUserDoesHaveTechnicalReviewFcsPermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaAllTechnicalReviews(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaRegulatorAllApplications_whenUserDoesNotHaveAssignFcsPermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaRegulatorAllApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaRegulatorAllApplications_whenUserDoesHaveAssignFcsPermission() throws Exception {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, Set.of(Role.CASE_MANAGER, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaRegulatorAllApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaAllConsultations_whenUserDoesNotHavePermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaAllConsultations(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaAllConsultations_whenUserDoesHavePermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.CONSULTEE, Role.ALLOCATOR))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaAllConsultations(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaUnassignedConsultations_whenUserDoesNotHavePermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaUnassignedConsultations(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaUnassignedConsultations_whenUserDoesHavePermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.CONSULTEE, Role.ALLOCATOR))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaUnassignedConsultations(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaMyConsultations_whenUserDoesNotHavePermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaMyConsultations(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaMyConsultations_whenUserDoesHavePermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.CONSULTEE, Role.RESPONDER))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaMyConsultations(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getWorkAreaCamUserApplications_whenUserDoesNotHaveManageFeePeriodsPermission() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaCamMyApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getWorkAreaCamUserApplications_whenUserDoesHaveManageFeePeriodsPermission() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkAreaCamMyApplications(filter, user)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @Test
  void getWorkArea_RegulatorUser_CaseOfficer() throws Exception {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .withRole(Role.CASE_OFFICER)
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

    when(workAreaService.getRegulatorWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class), any(WorkAreaTab.class))).thenReturn(
        workAreaItemViews);
    var caseOfficerTabs = List.of(WorkAreaTab.MY_APPLICATIONS, WorkAreaTab.UNASSIGNED_APPLICATIONS);
    when(workAreaService.getTabsAvailableToUser(user)).thenReturn(caseOfficerTabs);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model, WorkAreaTab.MY_APPLICATIONS);
    assertThat(model)
        .containsEntry("isWorkAreaWithTabs", true)
        .containsEntry("selectedTab", WorkAreaTab.MY_APPLICATIONS.getValue())
        .containsEntry("workAreaTabs", caseOfficerTabs);

    var actualForm = (WorkAreaFilterForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  @Test
  void getWorkArea_RegulatorUser_CaseManager() throws Exception {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .withRole(Role.CASE_MANAGER)
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

    when(workAreaService.getRegulatorWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class), any(WorkAreaTab.class)))
        .thenReturn(workAreaItemViews);

    var caseManagerTabs = List.of(WorkAreaTab.ALL_APPLICATIONS, WorkAreaTab.UNASSIGNED_APPLICATIONS);
    when(workAreaService.getTabsAvailableToUser(user)).thenReturn(caseManagerTabs);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model, WorkAreaTab.ALL_APPLICATIONS);
    assertThat(model)
        .containsEntry("isWorkAreaWithTabs", true)
        .containsEntry("selectedTab", WorkAreaTab.ALL_APPLICATIONS.getValue())
        .containsEntry("workAreaTabs", caseManagerTabs);

    var actualForm = (WorkAreaFilterForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  @Test
  void getWorkArea_RegulatorUser_TechnicalReviewer() throws Exception {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .withRole(Role.TECHNICAL_REVIEWER)
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

    when(workAreaService.getRegulatorWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class), any(WorkAreaTab.class)))
        .thenReturn(workAreaItemViews);
    var technicalReviewerTabs = List.of(WorkAreaTab.MY_TECHNICAL_REVIEWS, WorkAreaTab.ALL_TECHNICAL_REVIEWS);
    when(workAreaService.getTabsAvailableToUser(user)).thenReturn(technicalReviewerTabs);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andExpect(model().attribute("selectedTab", WorkAreaTab.MY_TECHNICAL_REVIEWS.getValue()))
        .andExpect(model().attribute("isWorkAreaWithTabs", true))
        .andExpect(model().attribute("workAreaTabs", technicalReviewerTabs))
        .andExpect(model().attribute("form", form))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model, WorkAreaTab.MY_TECHNICAL_REVIEWS);
  }

  @Test
  void getWorkArea_ConsulteeUser_Allocator() throws Exception {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.CONSULTEE)
                .build())
            .withRole(Role.ALLOCATOR)
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

    when(workAreaService.getConsulteeWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class), any(WorkAreaTab.class)))
        .thenReturn(workAreaItemViews);
    var consulteeAllocatorTabs = List.of(WorkAreaTab.ALL_CONSULTATIONS, WorkAreaTab.UNASSIGNED_CONSULTATIONS);
    when(workAreaService.getTabsAvailableToUser(user)).thenReturn(consulteeAllocatorTabs);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andExpect(model().attribute("selectedTab", WorkAreaTab.UNASSIGNED_CONSULTATIONS.getValue()))
        .andExpect(model().attribute("isWorkAreaWithTabs", true))
        .andExpect(model().attribute("workAreaTabs", consulteeAllocatorTabs))
        .andExpect(model().attribute("form", form))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model, WorkAreaTab.UNASSIGNED_CONSULTATIONS);
  }

  @Test
  void getWorkArea_regulatorUserAndConsulteeUser_withoutPermissions() throws Exception {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .withRole(null)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.CONSULTEE)
                .build())
            .withRole(null)
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

    when(workAreaService.getIndustryWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class)))
        .thenReturn(workAreaItemViews);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andExpect(model().attribute("isWorkAreaWithTabs", false))
        .andExpect(model().attribute("workAreaTabs", Collections.emptyList()))
        .andExpect(model().attribute("form", form))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model, null);
  }

  @Test
  void getWorkArea_RegulatorUser_CamUser() throws Exception {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .withRole(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

    when(workAreaService.getRegulatorWorkAreaItems(any(WorkAreaFilter.class), any(ServiceUserDetail.class), any(WorkAreaTab.class))).thenReturn(
        workAreaItemViews);
    var camTabs = List.of(WorkAreaTab.MY_CAM_APPLICATIONS, WorkAreaTab.ALL_APPLICATIONS);
    when(workAreaService.getTabsAvailableToUser(user)).thenReturn(camTabs);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertWorkAreaModel(model, WorkAreaTab.MY_CAM_APPLICATIONS);
    assertThat(model)
        .containsEntry("isWorkAreaWithTabs", true)
        .containsEntry("selectedTab", WorkAreaTab.MY_CAM_APPLICATIONS.getValue())
        .containsEntry("workAreaTabs", camTabs);

    var actualForm = (WorkAreaFilterForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  private void assertWorkAreaModel(Map<String, Object> model, WorkAreaTab expectedTab) {
    assertThat(model)
        .containsEntry("workAreaItems", workAreaItemViews)
        .containsEntry("clearFiltersUrl", ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilter(expectedTab, null, null)))
        .containsEntry("appStatuses", ApplicationVersionStatus.getWorkAreaOptions())
        .containsEntry("appTypes", ApplicationType.getDisplayableOptions())
        .containsEntry("durationTypes", ConsentLengthType.getConsentLengthOptions())
        .containsEntry("prefilledAsset", assetRestSearchItem)
        .containsEntry("assetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser(null, null)))
        .containsEntry("prefilledOperator", orgUnitRestSearchItem)
        .containsEntry("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null)))
        .containsEntry("pageTitle", WORK_AREA_TITLE)
        .containsEntry("filterResultsUrl", ReverseRouter.route(on(WorkAreaController.class).filterWorkArea(expectedTab, null, null)));
  }

  @Test
  void filterWorkArea() throws Exception {
    var form = new WorkAreaFilterForm();
    form.setStatuses(Collections.singletonList(ApplicationVersionStatus.IN_PROGRESS));
    form.setApplicationTypes(Collections.singletonList(ApplicationType.PRODUCTION));
    form.setDurationTypes(Collections.singletonList(ConsentLengthType.LONG_TERM));
    var filter = new WorkAreaFilter();
    var expectedRedirectUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null));

    mockMvc.perform(
        post(ReverseRouter.route(on(WorkAreaController.class).filterWorkArea(null, null, null)))
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
    var form = new WorkAreaFilterForm();
    form.setStatuses(Collections.singletonList(ApplicationVersionStatus.IN_PROGRESS));
    form.setApplicationTypes(Collections.singletonList(ApplicationType.PRODUCTION));
    form.setDurationTypes(Collections.singletonList(ConsentLengthType.LONG_TERM));

    var filter = new WorkAreaFilter();
    filter.update(form);

    var session = new MockHttpSession();
    session.setAttribute("workAreaFilter", filter);

    var expectedRedirectUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null));

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilter(null, null, null)))
            .with(user(user))
            .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(expectedRedirectUrl));

    assertThat(session.getAttribute("workAreaFilter")).isNull();
  }

  @Test
  void clearWorkAreaFilter_fromTab() throws Exception {
    var form = new WorkAreaFilterForm();
    form.setStatuses(Collections.singletonList(ApplicationVersionStatus.IN_PROGRESS));
    form.setApplicationTypes(Collections.singletonList(ApplicationType.PRODUCTION));
    form.setDurationTypes(Collections.singletonList(ConsentLengthType.LONG_TERM));

    var filter = new WorkAreaFilter();
    filter.update(form);

    var session = new MockHttpSession();
    session.setAttribute("workAreaFilter", filter);

    var tab = WorkAreaTab.ALL_TECHNICAL_REVIEWS;

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilter(tab, null, null)))
                .with(user(user))
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(tab.getUrl()));

    assertThat(session.getAttribute("workAreaFilter")).isNull();
  }
}
