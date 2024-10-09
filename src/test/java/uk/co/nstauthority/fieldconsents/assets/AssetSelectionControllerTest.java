package uk.co.nstauthority.fieldconsents.assets;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_ASSETS;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = AssetSelectionController.class)
class AssetSelectionControllerTest extends AbstractControllerTest {

  private static final String ASSET_SELECTION_VIEW_NAME = "fcs/assets/assetSelection";

  private ServiceUserDetail user;

  private BindingResult bindingResult;

  private AssetSelectionForm form;

  @BeforeEach
  void setup() {
    user = ServiceUserDetailTestUtil.Builder().build();
    form = AssetSelectionForm.empty();
    bindingResult = new BeanPropertyBindingResult(form, "form");
  }

  @SecurityTest
  void getAssetSelection_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAssetSelection_whenUserDoesNotHaveManageAssetsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS)))
        .thenReturn(false);

    mockMvc.perform(
        get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void manageAsset_whenUserDoesNotHaveManageAssetsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS)))
        .thenReturn(false);

    mockMvc.perform(
        post(ReverseRouter.route(on(AssetSelectionController.class).manageAsset(form, bindingResult)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAssetSelection_assertHttpOk() throws Exception {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(true);
    mockMvc.perform(get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(ASSET_SELECTION_VIEW_NAME));
  }

  @SecurityTest
  void manageAsset_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(AssetSelectionController.class).manageAsset(form, bindingResult)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void manageAsset_whenValidForm_assertRedirection() throws Exception {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(true);
    mockMvc
        .perform(post(ReverseRouter.route(on(AssetSelectionController.class).manageAsset(form, bindingResult)))
            .with(user(user))
            .with(csrf())
            .param("assetKey", field1AssetJson.getAssetKey().toString())) // this sets the assetKey in the bound in stub form var
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageAssetController.class).manageAsset(field1AssetJson.getAssetKey().toString()))));
  }

  @Test
  void manageAsset_whenInValidForm_assertStatusOk() throws Exception {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(true);
    mockMvc
        .perform(post(ReverseRouter.route(on(AssetSelectionController.class).manageAsset(form, bindingResult)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(ASSET_SELECTION_VIEW_NAME));
  }
}
