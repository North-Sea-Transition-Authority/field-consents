package uk.co.nstauthority.fieldconsents.production.longterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionController.END_YEAR;
import static uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionController.START_YEAR;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ContextConfiguration(classes = LongTermProductionController.class)
class LongTermProductionControllerTest extends AbstractControllerTest {

  @MockBean
  private LongTermProductionService longTermProductionService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private LongTermProductionFormValidator longTermProductionFormValidator;

  @MockBean
  private BindingResult bindingResult;

  private LongTermProductionForm longTermProductionForm;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    longTermProductionForm = ProductionTestUtils.getEmptyLongTermProductionForm(START_YEAR, END_YEAR);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  @WithMockUser
  public void getLongTermProductionRequestForm() throws Exception {
    when(longTermProductionService.getLongTermProductionForm(applicationVersion, START_YEAR, END_YEAR))
        .thenReturn(longTermProductionForm);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(LongTermProductionController.class)
            .getLongTermProductionRequestForm(APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/production/longTermProductionForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model.get("startYear")).isEqualTo(START_YEAR.toString());
    assertThat(model.get("endYear")).isEqualTo(END_YEAR.toString());
    assertThat(model.get("oilUnit")).isEqualTo(ProductionUnit.SCM_PER_DAY.getDisplayName());
    assertThat(model.get("gasUnit")).isEqualTo(ProductionUnit.KSCM_PER_DAY.getDisplayName());
    assertThat(model.get("submitUrl")).isEqualTo("/applications/" + APPLICATION_ID + "/long-term-production/");
    assertThat(model.get("form")).isEqualTo(longTermProductionForm);
  }

  @Test
  void getLongTermProductionRequestForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(LongTermProductionController.class).getLongTermProductionRequestForm(APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveLongTermProductionDetails_validForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(LongTermProductionController.class).saveLongTermProductionDetails(APPLICATION_ID, longTermProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/production/applicationSubmitted"));
  }

  @Test
  void saveLongTermProductionDetails_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(LongTermProductionController.class).saveLongTermProductionDetails(APPLICATION_ID, longTermProductionForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }

}

