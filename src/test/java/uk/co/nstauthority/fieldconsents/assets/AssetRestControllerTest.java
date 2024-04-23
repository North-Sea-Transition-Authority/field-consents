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

  TerminalWithOperatorJson bactonAssetJson = new TerminalWithOperatorJson(1, "Bacton",
      TerminalStatus.ACTIVE, OrganisationUnitTestUtil.orgUnit2Json);

  FieldWithOperatorJson albaAssetJson = new FieldWithOperatorJson(1, "ALBA",
      FieldTestUtil.FIELD_1_STATUS, FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA, FieldTestUtil.FIELD_1_SHORE,
      OrganisationUnitTestUtil.orgUnit1Json);

  @MockBean
  private AssetSearchService assetSearchService;

  @SecurityTest
  void searchAssetsForUser_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchAssetsForUser("brent", null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchAssetsForUser_assertHttpOk() throws Exception {
    when(assetSearchService.searchAssetsForUser("brent", user)).thenReturn(List.of(brentAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser("brent", user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1FIELD","text":"BRENT"}]}
         """));

  }

  @Test
  void searchAssetsForUser_fieldsAndTerminals() throws Exception {
    when(assetSearchService.searchAssetsForUser("br", user)).thenReturn(List.of(brentAssetJson, braeAssetJson));

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
    when(assetSearchService.searchAssets("br"))
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
    when(assetSearchService.searchTerminals("br"))
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
            .searchFieldAssets("test"))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchFields_assertHttpOk() throws Exception {
    when(assetSearchService.searchFields("brent"))
        .thenReturn(List.of(brentAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchFieldAssets("brent")))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1FIELD","text":"BRENT"}]}
         """));
  }

  @SecurityTest
  void searchTerminalAssetsForUser_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchTerminalAssetsForUser("test", null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchTerminalAssetsForUser_assertHttpOk() throws Exception {
    when(assetSearchService.searchTerminalsForUser("bacton", user)).thenReturn(List.of(bactonAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchTerminalAssetsForUser("bacton", user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1TERMINAL","text":"Bacton"}]}
         """));
  }

  @SecurityTest
  void searchFieldAssetsForUser_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class)
            .searchFieldAssetsForUser("test", null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void searchFieldAssetsForUser_assertHttpOk() throws Exception {
    when(assetSearchService.searchFieldsForUser("alba", user)).thenReturn(List.of(albaAssetJson));

    mockMvc.perform(get(ReverseRouter.route(on(AssetRestController.class).searchFieldAssetsForUser("alba", user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1FIELD","text":"ALBA"}]}
         """));
  }
}
