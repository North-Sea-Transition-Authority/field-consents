package uk.co.nstauthority.fieldconsents.assets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@WithMockUser
@ContextConfiguration(classes = AssetRestController.class)
public class AssetRestControllerTest extends AbstractControllerTest {

  AssetJson brent = new AssetJson(1, "BRENT", AssetType.FIELD);
  AssetJson brenda = new AssetJson(2, "TEST", AssetType.FIELD);
  AssetJson brae = new AssetJson(1, "BRAE", AssetType.TERMINAL);

  @MockBean
  AssetService assetService;

  @BeforeEach
  void beforeEach() {
    when(assetService.getAllAssets()).thenReturn(List.of(brent, brenda, brae));
  }

  @Test
  void searchAssets_assertHttpOk() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchAssets("brent"))))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1FIELD","text":"BRENT"}]}
         """));

  }

  @Test
  void searchAssets_fieldsAndTerminals() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchAssets("br"))))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1TERMINAL","text":"BRAE"}, {"id":"1FIELD","text":"BRENT"}]}
         """));

  }

}
