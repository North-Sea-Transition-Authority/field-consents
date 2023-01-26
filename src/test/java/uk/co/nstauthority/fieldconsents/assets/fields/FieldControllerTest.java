package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;

@WithMockUser
@ContextConfiguration(classes = FieldController.class)
public class FieldControllerTest extends AbstractControllerTest {

  @MockBean
  FieldService fieldService;

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void manageField_field1() throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithOperatorAndLicences.getId()), anyString()))
        .thenReturn(field1JsonWithOperatorAndLicences);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithOperatorAndLicences.getId()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/fields"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView,
        field1JsonWithOperatorAndLicences.getId(),
        field1JsonWithOperatorAndLicences.getName(),
        Boolean.TRUE);
  }

  @Test
  void manageField_fieldWithNoLicences() throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithNullOperatorAndLicences.getId()), anyString()))
        .thenReturn(field1JsonWithNullOperatorAndLicences);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithNullOperatorAndLicences.getId()))))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/assets/fields"))
            .andReturn().getModelAndView();

    checkModelAsserts(modelAndView,
        field1JsonWithNullOperatorAndLicences.getId(),
        field1JsonWithNullOperatorAndLicences.getName(),
        Boolean.FALSE);
  }

  private void checkModelAsserts(ModelAndView modelAndView,
                                 Integer fieldId,
                                 String fieldName,
                                 Boolean licencesExist) {
    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry("fieldId", fieldId)
        .containsEntry("fieldName", fieldName)
        .containsEntry("licencesExist", licencesExist)
        .containsEntry("warningHeading", "Associated licences missing")
        .containsEntry("warningContent", FieldController.LICENCE_WARNING.formatted(
            applicationContext.getBean(CustomerConfigurationProperties.class).mnemonic()))
        .containsEntry("startApplicationUrl", ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationForm(fieldId)));
  }
}
