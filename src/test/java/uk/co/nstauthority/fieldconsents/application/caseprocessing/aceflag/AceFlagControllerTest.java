package uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = AceFlagController.class)
class AceFlagControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private AceFlagService aceFlagService;

  private ApplicationVersion applicationVersion;

  private AceFlagForm aceFlagForm;

  private static final String CASE_PROCESSING_URL = "/applications/" + APPLICATION_ID + "/case-processing";

  private static final String CHANGE_ACE_STATUS_VIEW = "fcs/application/changeAceStatus";

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    aceFlagForm = new AceFlagForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getAceFlagForm_noUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AceFlagController.class).getAceFlagForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getAceFlagForm_validUser() throws Exception {
    when(aceFlagService.getAceFlagForm(applicationVersion)).thenReturn(aceFlagForm);

    mockMvc.perform(
        get(ReverseRouter.route(on(AceFlagController.class).getAceFlagForm(APPLICATION_ID)))
            .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(CHANGE_ACE_STATUS_VIEW))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("form", aceFlagForm));
  }

  @SecurityTest
  void saveAceFlagForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AceFlagController.class)
            .saveAceFlagForm(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveAceFlagForm_emptyForm() throws Exception {
    var modelAndView = mockMvc.perform(
        post(ReverseRouter.route(on(AceFlagController.class)
            .saveAceFlagForm(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(view().name(CHANGE_ACE_STATUS_VIEW))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null, null))))
        .andReturn().getModelAndView();

    verifyNoInteractions(aceFlagService);

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat((AceFlagForm) model.get("form"))
        .usingRecursiveComparison()
        .isEqualTo(aceFlagForm);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void saveAceFlagForm_validForm(Boolean aceFlag) throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AceFlagController.class)
            .saveAceFlagForm(APPLICATION_ID, null, null)))
            .param("aceFlag", aceFlag.toString())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + CASE_PROCESSING_URL));

    verify(aceFlagService, times(1))
        .setAceFlag(applicationVersion, aceFlag);
  }
}
