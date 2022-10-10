package uk.co.nstauthority.fieldconsents.production.shortterm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.END_DATE;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.PRODUCTION_YEAR;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.START_DATE;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ContextConfiguration(classes = ShortTermProductionController.class)
class ShortTermProductionControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ProductionRowService productionRowService;

  @MockBean
  private ShortTermProductionService shortTermProductionService;

  @MockBean
  private ShortTermProductionFormValidator shortTermProductionFormValidator;

  private ShortTermProductionForm shortTermProductionForm;

  @BeforeEach
  void setUp() {
    shortTermProductionForm = ProductionTestUtils.getEmptyShortTermProductionForm();

    ApplicationVersion applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(
        ApplicationType.PRODUCTION);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        applicationVersion);
  }

  @Test
  @WithMockUser
  void getShortTermProductionRequestForm() throws Exception {
    when(shortTermProductionService.getShortTermProductionForm(
        any(ApplicationVersion.class),
        any(LocalDate.class),
        any(LocalDate.class))
    ).thenReturn(shortTermProductionForm);
    doCallRealMethod().when(productionRowService).addProductionDetailsToModelAndView(any(ModelAndView.class));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ShortTermProductionController.class).getShortTermProductionRequestForm(APPLICATION_ID)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/production/shortTermProductionForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(PRODUCTION_YEAR, model.get("requestYear"));
    assertEquals(DateUtils.format(START_DATE, DateUtils.SHORT_DATE), model.get("startDate"));
    assertEquals(DateUtils.format(END_DATE, DateUtils.SHORT_DATE), model.get("endDate"));
    assertEquals(ProductionUnit.SCM_PER_MONTH.getDisplayName(), model.get("oilUnit"));
    assertEquals(ProductionUnit.KSCM_PER_MONTH.getDisplayName(), model.get("gasUnit"));
  }

  @Test
  void getShortTermProductionRequestForm_withUnauthorizedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ShortTermProductionController.class).getShortTermProductionRequestForm(APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveShortTermProductionDetails_withValidForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ShortTermProductionController.class).saveShortTermProductionDetails(APPLICATION_ID, shortTermProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/production/applicationSubmitted"));
  }

  @Test
  void saveShortTermProductionDetails() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ShortTermProductionController.class).saveShortTermProductionDetails(APPLICATION_ID, shortTermProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }

}