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
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

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
    mockMvc.perform(
        get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void manageAsset_whenUserDoesNotHaveManageAssetsPermission() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(AssetSelectionController.class).manageAsset(form, bindingResult)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @ParameterizedSecurityTest
  @EnumSource(
      value = Role.class,
      names = {"CASE_OFFICER", "CASE_MANAGER", "CONSENTS_AND_AUTHORISATIONS_MANAGER", "TECHNICAL_REVIEWER", "VIEWER"}
  )
  void getAssetSelection_regulator_validRoles(Role role) throws Exception {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(role)
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    ));

    mockMvc.perform(get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(ASSET_SELECTION_VIEW_NAME));
  }

  @ParameterizedSecurityTest
  @EnumSource(
      value = Role.class,
      names = {"ACCESS_MANAGER", "INDUSTRY_ACCESS_MANAGER", "DOCUMENT_TEMPLATE_MANAGER"}
  )
  void getAssetSelection_regulator_invalidRoles(Role role) throws Exception {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(role)
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    ));

    mockMvc.perform(get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedSecurityTest
  @EnumSource(
      value = Role.class,
      names = {"CREATOR", "EDITOR", "SUBMITTER", "FINANCE_ADMINISTRATOR", "VIEWER", "CONSENT_RECIPIENT"}
  )
  void getAssetSelection_industry_validRoles(Role role) throws Exception {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(role)
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.INDUSTRY)
                .build())
            .build()
    ));

    mockMvc.perform(get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(ASSET_SELECTION_VIEW_NAME));
  }

  @ParameterizedSecurityTest
  @EnumSource(
      value = Role.class,
      names = {"ACCESS_MANAGER"}
  )
  void getAssetSelection_industry_invalidRoles(Role role) throws Exception {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(role)
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.INDUSTRY)
                .build())
            .build()
    ));

    mockMvc.perform(get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getAssetSelection_consultee_assertForbidden() throws Exception {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.CONSULTEE)
                .build())
            .build()
    ));

    mockMvc.perform(get(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
            .with(user(user)))
        .andExpect(status().isForbidden());
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
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(Role.CASE_OFFICER)
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    ));

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
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withRole(Role.CASE_OFFICER)
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    ));

    mockMvc
        .perform(post(ReverseRouter.route(on(AssetSelectionController.class).manageAsset(form, bindingResult)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(ASSET_SELECTION_VIEW_NAME));
  }
}
