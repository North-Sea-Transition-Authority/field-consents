package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.stream.Stream;
import org.jsoup.internal.StringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirection;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionBuilder;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted.HaveSubmittedController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ProjectPurposeController.class)
class ProjectPurposeControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/eia-screening/project-purpose-form";

  @MockBean
  private ProjectPurposeFormValidator validator;

  @MockBean
  private EiaDirectionService eiaDirectionService;

  private static final ApplicationVersion APPLICATION_VERSION = ApplicationTestUtil.getNewApplicationVersionWithType(
      ApplicationType.PRODUCTION);
  private static final Integer APPLICATION_ID = APPLICATION_VERSION.getApplication().getId();
  private EiaDirection eiaDirection;

  @BeforeEach
  void setUp() {
    eiaDirection = EiaDirectionBuilder.newBuilder()
        .withId(1)
        .withApplicationVersion(APPLICATION_VERSION)
        .build();

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        APPLICATION_VERSION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(APPLICATION_VERSION));
  }

  @SecurityTest
  void getForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ProjectPurposeController.class)
            .getForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, APPLICATION_VERSION, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ProjectPurposeController.class)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getForm(boolean forPurposeOfEiaRegs) throws Exception {
    when(eiaDirectionService.findEiaDirection(APPLICATION_VERSION))
        .thenReturn(Optional.of(eiaDirection));

    eiaDirection.setForPurposeOfEiaRegs(forPurposeOfEiaRegs);

    var model = mockMvc.perform(get(ReverseRouter.route(on(ProjectPurposeController.class)
            .getForm(APPLICATION_ID)))
            .with(user(user))
        )
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    var taskListUrl = ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null));

    assertThat(model).contains(
        entry("cancelUrl", taskListUrl),
        entry("backLinkUrl", taskListUrl)
    );

    assertThat((ProjectPurposeForm) model.get("form")).extracting(
        ProjectPurposeForm::forPurposeOfEiaRegs,
        ppf -> ppf.getRationaleForPurposeOfEiaRegs().getInputValue(),
        ppf -> ppf.getRationaleNotForPurposeOfEiaRegs().getInputValue()
    ).containsExactly(
        forPurposeOfEiaRegs,
        null,
        null
    );
  }

  @SecurityTest
  void saveForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ProjectPurposeController.class)
            .saveForm(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, APPLICATION_VERSION, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ProjectPurposeController.class)
            .saveForm(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @MethodSource("saveFormParams")
  void saveForm(boolean forPurposeOfEiaRegs, String redirectUrl) throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ProjectPurposeController.class)
            .saveForm(APPLICATION_ID, null, null)))
            .param("forPurposeOfEiaRegs", String.valueOf(forPurposeOfEiaRegs))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(redirectUrl));

    verify(validator).validate(
        argThat(
            (ProjectPurposeForm projectPurposeForm) -> projectPurposeForm.forPurposeOfEiaRegs() == forPurposeOfEiaRegs
                && StringUtil.isBlank(projectPurposeForm.getRationaleForPurposeOfEiaRegs().getInputValue())
                && StringUtil.isBlank(projectPurposeForm.getRationaleNotForPurposeOfEiaRegs().getInputValue())),
        any(BindingResult.class));
    verify(eiaDirectionService).updateEiaDirection(eq(APPLICATION_VERSION), any(ProjectPurposeForm.class));
  }

  @ParameterizedTest
  @MethodSource("saveFormParams")
  void saveForm_withValidationErrors(boolean forPurposeOfEiaRegs) throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("forPurposeOfEiaRegs", "invalid", "Validation message");
      return null;
    })
        .when(validator)
        .validate(
            argThat(
                (ProjectPurposeForm projectPurposeForm) -> projectPurposeForm.forPurposeOfEiaRegs() == forPurposeOfEiaRegs
                    && StringUtil.isBlank(projectPurposeForm.getRationaleForPurposeOfEiaRegs().getInputValue())
                    && StringUtil.isBlank(projectPurposeForm.getRationaleNotForPurposeOfEiaRegs().getInputValue())),
            any(BindingResult.class));

    mockMvc.perform(post(ReverseRouter.route(on(ProjectPurposeController.class)
            .saveForm(APPLICATION_ID, null, null)))
            .param("forPurposeOfEiaRegs", String.valueOf(forPurposeOfEiaRegs))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(eiaDirectionService, never()).updateEiaDirection(any(), any(ProjectPurposeForm.class));
  }

  private static Stream<Arguments> saveFormParams() {
    return Stream.of(
        Arguments.of(
            true,
            ReverseRouter.route(on(HaveSubmittedController.class).getForm(APPLICATION_ID))
        ),
        Arguments.of(
            false,
            ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null))
        )
    );
  }

}
