package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@WithMockUser
@ContextConfiguration(classes = FieldController.class)
public class FieldControllerTest extends AbstractControllerTest {

  @MockBean
  FieldService fieldService;

  @Test
  void manageField_field1() throws Exception {

    when(fieldService.getField(field1Json.getId(), "Manage field")).thenReturn(field1Json);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class).manageField(field1Json.getId()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/fields"))
        .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertEquals(field1Json.getId(), model.get("fieldId"));
    assertEquals(field1Json.getName(), model.get("fieldName"));
  }
}
