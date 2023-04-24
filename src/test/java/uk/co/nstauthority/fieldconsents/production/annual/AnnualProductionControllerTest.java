package uk.co.nstauthority.fieldconsents.production.annual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.PRODUCTION_YEAR;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ContextConfiguration(classes = AnnualProductionController.class)
class AnnualProductionControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationUnitService applicationUnitService;

  @MockBean
  private AnnualProductionService annualProductionService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private AnnualProductionFormValidator annualProductionFormValidator;

  private AnnualProductionForm annualProductionForm;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    annualProductionForm = ProductionTestUtils.getEmptyAnnualProductionForm();

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(ProductionUnit.KSCM_PER_MONTH);
    when(applicationUnitService.getProductionGasUnit(applicationVersion)).thenReturn(ProductionUnit.KSCM_PER_MONTH);
  }

  @Test
  @WithMockUser
  void getAnnualProductionRequestForm() throws Exception {
    when(annualProductionService.getAnnualProductionForm(applicationVersion)).thenReturn(annualProductionForm);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(AnnualProductionController.class).getAnnualProductionRequestForm(APPLICATION_ID)))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/production/annualProductionForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(PRODUCTION_YEAR, model.get("requestYear"));
    assertEquals(ProductionUnit.KSCM_PER_MONTH.getDisplayName(), model.get("oilUnit"));
    assertEquals(ProductionUnit.KSCM_PER_MONTH.getDisplayName(), model.get("gasUnit"));
  }

  @Test
  void getAnnualProductionRequestForm_withUnauthorizedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AnnualProductionController.class).getAnnualProductionRequestForm(APPLICATION_ID))))
            .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void saveAnnualProductionDetails_withValidForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AnnualProductionController.class).saveAnnualProductionDetails(APPLICATION_ID, annualProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/applications/1/task-list/"));
  }

  @Test
  void saveAnnualProductionDetails() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AnnualProductionController.class)
            .saveAnnualProductionDetails(APPLICATION_ID, annualProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}