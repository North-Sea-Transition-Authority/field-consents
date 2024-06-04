package uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
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

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@ContextConfiguration(classes = NeedsSubmittingController.class)
class NeedsSubmittingControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/eia-screening/needs-submitting-form";
  private static final int DAY = 23;
  private static final int MONTH = 2;
  private static final int YEAR = 23;

  @MockBean
  private NeedsSubmittingFormValidator validator;

  @MockBean
  private EiaDirectionService eiaDirectionService;

  private ApplicationVersion applicationVersion;
  private Integer applicationId;
  private EiaDirection eiaDirection;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationId = applicationVersion.getApplication().getId();
    eiaDirection = EiaDirectionBuilder.newBuilder()
        .withId(1)
        .withApplicationVersion(applicationVersion)
        .build();

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId))
        .thenReturn(applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(applicationId))
        .thenReturn(Optional.ofNullable(applicationVersion));
  }

  @SecurityTest
  void getForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NeedsSubmittingController.class)
            .getForm(applicationId))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(NeedsSubmittingController.class)
            .getForm(applicationId)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getForm() throws Exception {
    when(eiaDirectionService.findEiaDirection(applicationVersion)).thenReturn(Optional.of(eiaDirection));

    eiaDirection.setHaveEiaDirectionToSubmit(true);
    eiaDirection.setLatestDateToBeSubmitted(LocalDate.of(YEAR, MONTH, DAY));

    var model = mockMvc.perform(get(ReverseRouter.route(on(NeedsSubmittingController.class)
            .getForm(applicationId)))
            .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    var form = new NeedsSubmittingForm(eiaDirection.getHaveEiaDirectionToSubmit(), null, null);
    form.latestDateToBeSubmitted().setDate(eiaDirection.getLatestDateToBeSubmitted());

    assertThat(model).contains(
        entry("backLinkUrl", ReverseRouter.route(on(HaveSubmittedController.class).getForm(applicationId))),
        entry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null)))
    );

    assertThat(model.get("form"))
        .asInstanceOf(type(NeedsSubmittingForm.class))
        .extracting(
            NeedsSubmittingForm::haveEiaDirectionToSubmit,
            f -> f.latestDateToBeSubmitted().getAsLocalDate().orElseThrow().getDayOfMonth(),
            f -> f.latestDateToBeSubmitted().getAsLocalDate().orElseThrow().getMonth(),
            f -> f.latestDateToBeSubmitted().getAsLocalDate().orElseThrow().getYear(),
            f -> f.whyNoEiaDirection().getInputValue()
        ).containsExactly(
            eiaDirection.getHaveEiaDirectionToSubmit(),
            eiaDirection.getLatestDateToBeSubmitted().getDayOfMonth(),
            eiaDirection.getLatestDateToBeSubmitted().getMonth(),
            eiaDirection.getLatestDateToBeSubmitted().getYear(),
            eiaDirection.getWhyNoEiaDirection()
        );
  }

  @SecurityTest
  void saveForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NeedsSubmittingController.class)
            .saveForm(applicationId, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(NeedsSubmittingController.class)
            .saveForm(applicationId, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void saveForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NeedsSubmittingController.class).saveForm(applicationId, null, null)))
            .param("haveEiaDirectionToSubmit", "true")
            .param("latestDateToBeSubmitted.yearInput.inputValue", String.valueOf(YEAR))
            .param("latestDateToBeSubmitted.monthInput.inputValue", String.valueOf(MONTH))
            .param("latestDateToBeSubmitted.dayInput.inputValue", String.valueOf(DAY))
            .param("whyNoEiaDirection.inputValue", "reason")
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null))));

    verify(validator).validate(any(NeedsSubmittingForm.class), any(BindingResult.class));
    verify(eiaDirectionService).updateEiaDirection(eq(applicationVersion), any(NeedsSubmittingForm.class));
  }

  @Test
  void saveForm_withValidationErrors() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("haveEiaDirectionToSubmit", "invalid", "validation message");
      return null;
    })
        .when(validator)
        .validate(any(NeedsSubmittingForm.class), any(BindingResult.class));

    mockMvc.perform(post(ReverseRouter.route(on(NeedsSubmittingController.class).saveForm(applicationId, null, null)))
            .param("haveEiaDirectionToSubmit", "true")
            .param("latestDateToBeSubmitted.yearInput.inputValue", String.valueOf(YEAR))
            .param("latestDateToBeSubmitted.monthInput.inputValue", String.valueOf(MONTH))
            .param("latestDateToBeSubmitted.dayInput.inputValue", String.valueOf(DAY))
            .param("whyNoEiaDirection.inputValue", "reason")
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(validator).validate(any(NeedsSubmittingForm.class), any(BindingResult.class));
    verify(eiaDirectionService, never()).updateEiaDirection(any(), any(NeedsSubmittingForm.class));
  }

}
