package uk.co.nstauthority.fieldconsents.production.shortterm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.END_DATE;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.PRODUCTION_YEAR;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.START_DATE;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ContextConfiguration(classes = ShortTermProductionController.class)
class ShortTermProductionControllerTest extends AbstractApplicationControllerTest {

  @MockitoBean
  private ApplicationUnitService applicationUnitService;

  @MockitoBean
  private ShortTermProductionService shortTermProductionService;

  @MockitoBean
  private ShortTermProductionFormValidator shortTermProductionFormValidator;

  private ShortTermProductionForm shortTermProductionForm;

  @BeforeEach
  void setUp() {
    shortTermProductionForm = ProductionTestUtils.getEmptyShortTermProductionForm();

    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(
        ApplicationType.PRODUCTION);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(ProductionUnit.KSCM_PER_MONTH);
    when(applicationUnitService.getProductionGasUnit(applicationVersion)).thenReturn(ProductionUnit.KSCM_PER_MONTH);
  }

  @Test
  void getShortTermProductionRequestForm() throws Exception {
    when(shortTermProductionService.getShortTermProductionForm(any(ApplicationVersion.class))).thenReturn(shortTermProductionForm);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ShortTermProductionController.class).getShortTermProductionRequestForm(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/production/shortTermProductionForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(PRODUCTION_YEAR, model.get("requestYear"));
    assertEquals(DateUtils.format(START_DATE, DateUtils.SHORT_DATE), model.get("startDate"));
    assertEquals(DateUtils.format(END_DATE, DateUtils.SHORT_DATE), model.get("endDate"));
    assertEquals(ProductionUnit.KSCM_PER_MONTH.getDisplayName(), model.get("oilUnit"));
    assertEquals(ProductionUnit.KSCM_PER_MONTH.getDisplayName(), model.get("gasUnit"));
  }

  @SecurityTest
  void getShortTermProductionRequestForm_withUnauthorizedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ShortTermProductionController.class).getShortTermProductionRequestForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveShortTermProductionDetails_withValidForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ShortTermProductionController.class).saveShortTermProductionDetails(APPLICATION_ID, shortTermProductionForm, ReverseRouter.emptyBindingResult())))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/applications/1/task-list"));
  }

  @SecurityTest
  void saveShortTermProductionDetails() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ShortTermProductionController.class).saveShortTermProductionDetails(APPLICATION_ID, shortTermProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}
