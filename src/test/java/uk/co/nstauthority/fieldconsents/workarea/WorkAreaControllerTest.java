package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.ManageAssetController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@WithMockUser
@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  static final String FIELD1_ASSET_KEY = "1FIELD";

  static final String WORK_AREA_VIEW_NAME = "fcs/workarea/workArea";

  BindingResult bindingResult;

  AssetSelectionForm form;

  @BeforeEach
  void setup() {
    form = new AssetSelectionForm();
    bindingResult = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void getWorkArea_assertHttpOk() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME));
  }

  @Test
  void manageAsset_whenValidForm_assertRedirection() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(WorkAreaController.class).manageAsset(form, bindingResult)))
            .with(csrf())
            .param("assetKey", FIELD1_ASSET_KEY)) // this sets the assetKey in the bound in stub form var
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageAssetController.class).manageAsset(FIELD1_ASSET_KEY))));
  }

  @Test
  void manageAsset_whenInValidForm_assertStatusOk() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(WorkAreaController.class).manageAsset(form, bindingResult)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(WORK_AREA_VIEW_NAME));
  }

}