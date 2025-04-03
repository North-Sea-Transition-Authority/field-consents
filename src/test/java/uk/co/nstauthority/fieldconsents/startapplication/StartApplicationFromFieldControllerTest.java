package uk.co.nstauthority.fieldconsents.startapplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@ContextConfiguration(classes = StartApplicationFromFieldController.class)
class StartApplicationFromFieldControllerTest extends AbstractControllerTest {

  private static final Integer FIELD_ID = 1;

  private static final String MANAGE_FIELD_URL_BASE = "/manage-asset/fields/1";

  private static final Set<Role> INDUSTRY_ROLES = EnumSet.of(Role.CREATOR);

  @MockitoBean
  private ApplicationService applicationService;

  @MockitoBean
  private StartApplicationControllerHelperService startApplicationControllerHelperService;

  @MockitoBean
  private StartApplicationFormValidator formValidator;

  @MockitoBean
  private StartApplicationOperatorFormValidator operatorFormValidator;

  @MockitoBean
  private StartApplicationOperatorFormService startApplicationOperatorFormService;

  @MockitoBean
  private AssetService assetService;

  private Map<String, String> applicationTypeMap;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationTypeMap = Arrays.stream(ConsentLengthType.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ConsentLengthType::getDisplayName));

    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(startApplicationControllerHelperService.getApplicationTypesMap(AssetType.FIELD)).thenReturn(applicationTypeMap);
    when(fieldService.findFieldWithOperator(eq(FIELD_ID), anyString())).thenReturn(Optional.of(field1JsonWithOperator));
  }

  @SecurityTest
  void getStartApplicationForm() throws Exception {
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, field1JsonWithOperator, INDUSTRY_ROLES)).thenReturn(true);

    String continueStartApplicationUrl = ReverseRouter.route(on(StartApplicationFromFieldController.class)
        .continueStartApplicationOfType(FIELD_ID, null, null, null, null));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
                .getStartApplicationForm(FIELD_ID, null)))
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/startApplication"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(applicationTypeMap, model.get("applicationTypes"));
    assertEquals(continueStartApplicationUrl, model.get("continueStartApplicationUrl"));
  }

  @SecurityTest
  void getStartApplicationForm_notAuthorized() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationForm(FIELD_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getStartApplicationForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationForm(FIELD_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void continueStartApplicationOfType() throws Exception {
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, field1JsonWithOperator, INDUSTRY_ROLES)).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
        .continueStartApplicationOfType(FIELD_ID, null, null, null, null)))
            .param("applicationType", ApplicationType.FLARE.name())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeCount(1))
        .andExpect(flash().attribute("applicationType", ApplicationType.FLARE))
        .andExpect(view().name("redirect:" + MANAGE_FIELD_URL_BASE + "/start-application/operator"));
  }

  @SecurityTest
  void continueStartApplicationOfType_formErrors() throws Exception {
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, field1JsonWithOperator, INDUSTRY_ROLES)).thenReturn(true);

    doCallRealMethod().when(formValidator).validate(any(), any());

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
                .continueStartApplicationOfType(FIELD_ID, null, null, null, null)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/startApplication"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(applicationTypeMap, model.get("applicationTypes"));
    assertEquals(MANAGE_FIELD_URL_BASE + "/start-application", model.get("continueStartApplicationUrl"));
  }

  @SecurityTest
  void continueStartApplicationOfType_whenUnauthorized() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .continueStartApplicationOfType(FIELD_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void continueStartApplicationOfType_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .continueStartApplicationOfType(FIELD_ID, null, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getStartApplicationOperatorForm() throws Exception {
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, field1JsonWithOperator, INDUSTRY_ROLES)).thenReturn(true);

    var orgUnitRestSearchItem = new RestSearchItem("1", "ORG_NAME");
    when(startApplicationOperatorFormService.getPrefilledOperatorForField(FIELD_ID))
        .thenReturn(orgUnitRestSearchItem);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
                .getStartApplicationOperatorForm(FIELD_ID, null, null)))
                .flashAttr("applicationType", ApplicationType.FLARE)
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/operatorForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("createApplicationUrl", MANAGE_FIELD_URL_BASE + "/start-application/operator"),
            entry("cancelUrl", MANAGE_FIELD_URL_BASE),
            entry("organisationUnitSearchRestUrl",
                ReverseRouter.route(on(OrganisationUnitRestController.class)
                    .getOrganisationUnitsForCreator(null, null))),
            entry("prefilledOperator", orgUnitRestSearchItem)
        );
    var form = (StartApplicationOperatorForm) model.get("form");
    assertThat(form.getApplicationType()).isEqualTo(ApplicationType.FLARE);
    assertThat(form.getOrganisationUnitId().getInputValue()).isNull();
  }

  @SecurityTest
  void getStartApplicationOperatorForm_notAuthorized() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationOperatorForm(FIELD_ID, null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getStartApplicationOperatorForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationOperatorForm(FIELD_ID, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getStartApplicationOperatorForm_whenFieldNotFound() throws Exception {
    when(fieldService.findFieldWithOperator(eq(FIELD_ID), anyString())).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationOperatorForm(FIELD_ID, null, null)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void createNewApplication() throws Exception {
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, field1JsonWithOperator, INDUSTRY_ROLES)).thenReturn(true);

    OrganisationUnitJson operatorOuJson = new OrganisationUnitJson(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1,
        ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1);

    when(organisationUnitService.getOrganisationUnitById(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1,
        "Lookup organisation unit prior to creating a field application"))
        .thenReturn(operatorOuJson);
    when(fieldService.getFieldWithOperatorAndLicences(FIELD_ID, "Lookup field prior to creating a field application"))
        .thenReturn(field1JsonWithOperatorAndLicences);
    when(applicationService.createNewApplicationForField(ApplicationType.FLARE,
        field1JsonWithOperatorAndLicences, operatorOuJson, user))
        .thenReturn(applicationVersion);

    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .createNewApplication(FIELD_ID, null, ReverseRouter.emptyBindingResult(), null)))
            .param("applicationType", ApplicationType.FLARE.name())
            .param("organisationUnitId.inputValue", String.valueOf(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list"));
  }

  @SecurityTest
  void createNewApplication_formErrors() throws Exception {
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, field1JsonWithOperator, INDUSTRY_ROLES)).thenReturn(true);

    doCallRealMethod().when(operatorFormValidator).validate(any(), any());

    when(startApplicationOperatorFormService.getPrefilledOperatorForField(FIELD_ID))
        .thenReturn(EMPTY_REST_SEARCH_ITEM);

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
                .createNewApplication(FIELD_ID, null, ReverseRouter.emptyBindingResult(), null)))
                .param("applicationType", ApplicationType.FLARE.name())
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/operatorForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("createApplicationUrl", MANAGE_FIELD_URL_BASE + "/start-application/operator"),
            entry("cancelUrl", MANAGE_FIELD_URL_BASE)
        );
    var form = (StartApplicationOperatorForm) model.get("form");
    assertThat(form.getApplicationType()).isEqualTo(ApplicationType.FLARE);
    assertThat(form.getOrganisationUnitId().getInputValue()).isNull();
  }

  @SecurityTest
  void createNewApplication_whenUnauthorized() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .createNewApplication(FIELD_ID, null, ReverseRouter.emptyBindingResult(), null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void createNewApplication_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .createNewApplication(FIELD_ID, null, ReverseRouter.emptyBindingResult(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getStartApplicationForm_cannotStartApplicationForField() throws Exception {
    // Required for HasAssetPermissionInterceptor
    when(fieldService.getField(FIELD_ID, "Search field for asset permission")).thenReturn(field1JsonWithOperator);

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
        .when(assetService)
        .throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(new AssetKey(FIELD_ID, AssetType.FIELD), user);

    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationForm(FIELD_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void continueStartApplicationOfType_cannotStartApplicationForField() throws Exception {
    // Required for HasAssetPermissionInterceptor
    when(fieldService.getField(FIELD_ID, "Search field for asset permission")).thenReturn(field1JsonWithOperator);

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
        .when(assetService)
        .throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(new AssetKey(FIELD_ID, AssetType.FIELD), user);

    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .continueStartApplicationOfType(FIELD_ID, null, null, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }
}
