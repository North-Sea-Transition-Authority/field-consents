package uk.co.nstauthority.fieldconsents.application.production.annual;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.production.annual.AnnualProductionTestUtil.PRODUCTION_YEAR;

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
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionController;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionForm;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionFormValidator;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.validation.FormErrorSummaryService;

@ContextConfiguration(classes = AnnualProductionController.class)
class AnnualProductionControllerTest extends AbstractControllerTest {

  @MockBean
  private FormErrorSummaryService formErrorSummaryService;

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
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    annualProductionForm = AnnualProductionTestUtil.getEmptyAnnualProductionForm();

    when(applicationVersionService.getLatestApplicationVersionOrError(APPLICATION_ID)).thenReturn(applicationVersion);
  }

  @Test
  @WithMockUser
  void getAnnualProductionRequestForm() throws Exception {
    when(annualProductionService.getAnnualProductionForm(applicationVersion, PRODUCTION_YEAR)).thenReturn(annualProductionForm);

    mockMvc.perform(get(ReverseRouter.route(on(AnnualProductionController.class).getAnnualProductionRequestForm(APPLICATION_ID)))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/production/annualProductionForm"))
        .andExpect(model().attribute("requestYear", PRODUCTION_YEAR));
  }

  @Test
  void getAnnualProductionRequestForm_withUnauthorizedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AnnualProductionController.class).getAnnualProductionRequestForm(APPLICATION_ID))))
            .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveAnnualProductionDetails_withValidForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AnnualProductionController.class).saveAnnualProductionDetails(APPLICATION_ID, annualProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/startapplication/productionApplicationTaskList"));
  }

  @Test
  void saveAnnualProductionDetails() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AnnualProductionController.class)
            .saveAnnualProductionDetails(APPLICATION_ID, annualProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}