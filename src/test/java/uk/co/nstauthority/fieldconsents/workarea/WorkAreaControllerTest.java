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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;

@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  static final String WORK_AREA_VIEW_NAME = "fcs/workarea/workArea";

  @MockBean
  private WorkAreaService workAreaService;

  @MockBean
  private WorkAreaFormService workAreaFormService;

  @SecurityTest
  void getWorkArea_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getWorkArea_assertHttpOk() throws Exception {
    var filter = new WorkAreaFilter();
    filter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    filter.setApplicationTypes(List.of(ApplicationType.values()));
    var form = WorkAreaFormServiceTestUtil.getWorkAreaFormForFilterWithFieldAndOperator();
    when(workAreaFormService.getFromFilter(any(WorkAreaFilter.class))).thenReturn(form);
    var orgUnitRestSearchItem = WorkAreaFormServiceTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(workAreaFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);
    var assetRestSearchItem = WorkAreaFormServiceTestUtil.FIELD_REST_SEARCH_ITEM;
    when(workAreaFormService.getPrefilledAsset(any())).thenReturn(assetRestSearchItem);
    var workAreaItems = List.of(WorkAreaTestUtil.getWorkAreaItem());
    when(workAreaService.getWorkAreaItems(any(WorkAreaFilter.class))).thenReturn(workAreaItems);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

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

    var actualForm = (WorkAreaForm) model.get("form");
    assertThat(actualForm).usingRecursiveComparison().isEqualTo(form);
  }

  @Test
  void filterWorkArea() throws Exception {
    var form = new WorkAreaForm();
    form.setStatuses(Collections.singletonList(ApplicationVersionStatus.IN_PROGRESS));
    form.setApplicationTypes(Collections.singletonList(ApplicationType.PRODUCTION));
    form.setDurationTypes(Collections.singletonList(ConsentLengthType.LONG_TERM));
    var filter = new WorkAreaFilter();
    var expectedRedirectUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null));

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
    var expectedRedirectUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null));

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