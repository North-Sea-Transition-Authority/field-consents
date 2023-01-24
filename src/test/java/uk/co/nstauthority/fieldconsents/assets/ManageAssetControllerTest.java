package uk.co.nstauthority.fieldconsents.assets;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.BAD_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldController;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ManageAssetController.class)
public class ManageAssetControllerTest extends AbstractControllerTest {

  @MockBean
  AssetService assetService;

  @Test
  @WithMockUser
  void manageAsset_fieldRedirect() throws Exception {
    when(assetService.getAssetFromKey(FIELD1_ASSET_KEY)).thenReturn(Optional.of(field1AssetJson));

    mockMvc
        .perform(
            get(ReverseRouter.route(on(ManageAssetController.class).manageAsset(FIELD1_ASSET_KEY)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(FieldController.class).manageField(field1AssetJson.getId()))));

  }

  @Test
  void manageAsset_fieldRedirect_unauthorized() throws Exception {
    when(assetService.getAssetFromKey(FIELD1_ASSET_KEY)).thenReturn(Optional.of(field1AssetJson));

    mockMvc
        .perform(
            get(ReverseRouter.route(on(ManageAssetController.class).manageAsset(FIELD1_ASSET_KEY)))
                .with(csrf())
        )
        .andExpect(status().isUnauthorized());

  }

  @Test
  @WithMockUser
  void manageAsset_terminalRedirect() throws Exception {
    when(assetService.getAssetFromKey(TERMINAL1_ASSET_KEY)).thenReturn(Optional.of(terminal1AssetJson));

    mockMvc
        .perform(
            get(ReverseRouter.route(on(ManageAssetController.class).manageAsset(TERMINAL1_ASSET_KEY)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TerminalController.class).manageTerminal(terminal1AssetJson.getId()))));
  }

  @Test
  @WithMockUser
  void manageAsset_noFieldOrTerminalRedirect() throws Exception {
    when(assetService.getAssetFromKey(BAD_ASSET_KEY)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get(ReverseRouter.route(on(ManageAssetController.class).manageAsset(BAD_ASSET_KEY)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())));
  }

}
