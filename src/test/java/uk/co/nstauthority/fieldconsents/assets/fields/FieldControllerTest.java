package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.ManageAssetService;
import uk.co.nstauthority.fieldconsents.assets.StartApplicationDecision;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ContextConfiguration(classes = FieldController.class)
public class FieldControllerTest extends AbstractControllerTest {

  private static final Set<Role> INDUSTRY_ROLES = EnumSet.of(
      Role.CREATOR,
      Role.EDITOR,
      Role.SUBMITTER,
      Role.FINANCE_ADMINISTRATOR,
      Role.VIEWER,
      Role.CONSENT_RECIPIENT
  );

  @MockitoBean
  private ManageAssetService manageAssetService;

  @MockitoBean
  private AssetService assetService;

  @Captor
  private ArgumentCaptor<Supplier<FieldWithOperatorAndLicencesJson>> fieldJsonSupplierCaptor;

  @SecurityTest
  void manageField_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
            .manageField(field1JsonWithOperatorAndLicences.getId(), null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void manageField_whenFieldNotFound() throws Exception {
    var fieldId = field1JsonWithOperator.getId();

    when(fieldService.findFieldWithOperator(eq(fieldId), anyString()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
            .manageField(field1JsonWithOperator.getId(), null)))
            .with(user(user))
        )
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void manageField_whenUserDoesNotHaveAnyRequiredRoles_thenIsForbidden() throws Exception {
    var fieldId = field1JsonWithOperator.getId();

    when(fieldService.findFieldWithOperator(eq(fieldId), anyString()))
        .thenReturn(Optional.of(field1JsonWithOperator));

    mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
            .manageField(field1JsonWithOperator.getId(), null)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void manageField() throws Exception {
    var fieldId = field1JsonWithOperator.getId();

    // Required for AssetRoleInterceptor
    when(fieldService.findFieldWithOperator(eq(fieldId), anyString()))
        .thenReturn(Optional.of(field1JsonWithOperator));
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, field1JsonWithOperator, INDUSTRY_ROLES))
        .thenReturn(true);

    when(fieldService.getFieldWithOperatorAndLicences(field1JsonWithOperatorAndLicences.getId(), "Get field details for management screen"))
        .thenReturn(field1JsonWithOperatorAndLicences);

    var startApplicationDecision = StartApplicationDecision.allowed();
    when(assetService.getStartApplicationDecisionForField(eq(user), any())).thenReturn(startApplicationDecision);

    var applicationDataItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());
    when(manageAssetService.getApplicationDataItemViews(field1JsonWithOperatorAndLicences.getAssetKey(), user))
        .thenReturn(applicationDataItemViews);

    var backLinkUrl = ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection());
    var startApplicationUrl = ReverseRouter.route(on(StartApplicationFromFieldController.class).getStartApplicationForm(field1JsonWithOperatorAndLicences.getId(), null));

    mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
            .manageField(field1JsonWithOperatorAndLicences.getId(), null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/fields"))
        .andExpect(model().attribute("fieldJson", field1JsonWithOperatorAndLicences))
        .andExpect(model().attribute("startApplicationDecision", startApplicationDecision))
        .andExpect(model().attribute("operatorName", field1JsonWithOperatorAndLicences.getOperatorName()))
        .andExpect(model().attribute("licences", field1JsonWithOperatorAndLicences.getLicencesAsString()))
        .andExpect(model().attribute("backLinkUrl", backLinkUrl))
        .andExpect(model().attribute("startApplicationUrl", startApplicationUrl))
        .andExpect(model().attribute("applicationDataItemViews", applicationDataItemViews));

    verify(assetService).getStartApplicationDecisionForField(eq(user), fieldJsonSupplierCaptor.capture());
    assertThat(fieldJsonSupplierCaptor.getValue().get()).isEqualTo(field1JsonWithOperatorAndLicences);
  }
}
