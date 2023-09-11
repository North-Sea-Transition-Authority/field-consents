package uk.co.nstauthority.fieldconsents.assets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalStatus;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

@ContextConfiguration(classes = AssetRestController.class)
public class AssetRestControllerTest extends AbstractControllerTest {

  AssetWithOperatorJson brentAssetJson = new FieldWithOperatorJson(1, "BRENT",
      FieldTestUtil.FIELD_1_STATUS, FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA, FieldTestUtil.FIELD_1_SHORE,
      OrganisationUnitTestUtil.orgUnit1Json
  );
  AssetWithOperatorJson braeAssetJson = new TerminalWithOperatorJson(1, "BRAE",
      TerminalStatus.ACTIVE, OrganisationUnitTestUtil.orgUnit2Json);

  @MockBean
  AssetService assetService;

  @SecurityTest
  void searchAssetsForUser_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchAssetsForUser("brent", null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchAssetsForUser_assertHttpOk() throws Exception {
    when(assetService.searchAssetsForUser("brent", user)).thenReturn(List.of(brentAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser("brent", user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1FIELD","text":"BRENT"}]}
         """));

  }

  @Test
  void searchAssetsForUser_fieldsAndTerminals() throws Exception {
    when(assetService.searchAssetsForUser("br", user)).thenReturn(List.of(brentAssetJson, braeAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser("br", user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1TERMINAL","text":"BRAE"}, {"id":"1FIELD","text":"BRENT"}]}
         """));
  }

  @SecurityTest
  void searchAllAssets_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchAllAssets("test"))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchAllAssets_fieldsAndTerminals() throws Exception {
    when(assetService.searchAssets("br"))
        .thenReturn(List.of(brentAssetJson, braeAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchAllAssets("br")))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1TERMINAL","text":"BRAE"}, {"id":"1FIELD","text":"BRENT"}]}
         """));
  }

  @SecurityTest
  void searchTerminalAssets_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchTerminalAssets("test"))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchTerminalAssets_fieldsAndTerminals() throws Exception {
    when(assetService.searchTerminals("br"))
        .thenReturn(List.of(braeAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchTerminalAssets("br")))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1TERMINAL","text":"BRAE"}]}
         """));
  }

  @SecurityTest
  void searchFields_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchFields("test"))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchFields_assertHttpOk() throws Exception {
    when(assetService.searchFields("brent"))
        .thenReturn(List.of(brentAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchFields("brent")))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1FIELD","text":"BRENT"}]}
         """));
  }
}
