package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNoOperatorButLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorButEmptyLicences;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;

@WithMockUser
@ContextConfiguration(classes = FieldController.class)
public class FieldControllerTest extends AbstractControllerTest {

  @MockBean
  FieldService fieldService;

  @Test
  void manageField_fieldWithOperatorAndLicences() throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithOperatorAndLicences.getId()), anyString()))
        .thenReturn(field1JsonWithOperatorAndLicences);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithOperatorAndLicences.getId()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/fields"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithOperatorAndLicences);
  }

  @Test
  void manageField_fieldWithNoOperatorButLicencesExist() throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithNoOperatorButLicences.getId()), anyString()))
        .thenReturn(field1JsonWithNoOperatorButLicences);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithNoOperatorButLicences.getId()))))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/assets/fields"))
            .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithNoOperatorButLicences);
  }


  @Test
  void manageField_fieldWithOperatorButEmptyLicences() throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithOperatorButEmptyLicences.getId()), anyString()))
        .thenReturn(field1JsonWithOperatorButEmptyLicences);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithOperatorButEmptyLicences.getId()))))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/assets/fields"))
            .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithOperatorButEmptyLicences);
  }


  @Test
  void manageField_fieldWithNoOperatorOrLicences() throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithNullOperatorAndLicences.getId()), anyString()))
        .thenReturn(field1JsonWithNullOperatorAndLicences);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithNullOperatorAndLicences.getId()))))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/assets/fields"))
            .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithNullOperatorAndLicences);
  }

  private void checkModelAsserts(ModelAndView modelAndView,
                                 FieldWithOperatorAndLicencesJson fieldJson) {
    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry("fieldJson", fieldJson)
        .containsEntry("operatorExists", fieldJson.operatorExists())
        .containsEntry("licencesExist", fieldJson.licencesExist())
        .containsEntry("startApplicationEnabled", fieldJson.operatorExists() && fieldJson.licencesExist())
        .containsEntry("operatorName", fieldJson.getOperatorName())
        .containsEntry("licences", fieldJson.getLicencesAsString())
        .containsEntry("startApplicationUrl", ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationForm(fieldJson.getId())));
  }
}
