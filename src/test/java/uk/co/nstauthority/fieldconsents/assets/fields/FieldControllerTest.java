package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNoOperatorButLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorButEmptyLicences;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = FieldController.class)
public class FieldControllerTest extends AbstractControllerTest {

  @MockBean
  private FieldService fieldService;

  @MockBean
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @BeforeEach
  void setUp() {
    when(permissionService.hasPermission(user, Set.of(RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS)))
        .thenReturn(true);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageField_fieldWithOperatorAndLicences(boolean userHasCreatePermission) throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithOperatorAndLicences.getId()), anyString()))
        .thenReturn(field1JsonWithOperatorAndLicences);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any()))
        .thenReturn(userHasCreatePermission);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithOperatorAndLicences.getId(), null)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/fields"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithOperatorAndLicences, userHasCreatePermission);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageField_fieldWithNoOperatorButLicencesExist(boolean userHasCreatePermission) throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithNoOperatorButLicences.getId()), anyString()))
        .thenReturn(field1JsonWithNoOperatorButLicences);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any()))
        .thenReturn(userHasCreatePermission);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithNoOperatorButLicences.getId(), null)))
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/assets/fields"))
            .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithNoOperatorButLicences, userHasCreatePermission);
  }


  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageField_fieldWithOperatorButEmptyLicences(boolean userHasCreatePermission) throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithOperatorButEmptyLicences.getId()), anyString()))
        .thenReturn(field1JsonWithOperatorButEmptyLicences);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any()))
        .thenReturn(userHasCreatePermission);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithOperatorButEmptyLicences.getId(), null)))
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/assets/fields"))
            .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithOperatorButEmptyLicences, userHasCreatePermission);
  }


  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageField_fieldWithNoOperatorOrLicences(boolean userHasCreatePermission) throws Exception {

    when(fieldService.getFieldWithOperatorAndLicences(eq(field1JsonWithNullOperatorAndLicences.getId()), anyString()))
        .thenReturn(field1JsonWithNullOperatorAndLicences);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any()))
        .thenReturn(userHasCreatePermission);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FieldController.class)
                .manageField(field1JsonWithNullOperatorAndLicences.getId(), null)))
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/assets/fields"))
            .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, field1JsonWithNullOperatorAndLicences, userHasCreatePermission);
  }

  private void checkModelAsserts(ModelAndView modelAndView,
                                 FieldWithOperatorAndLicencesJson fieldJson,
                                 boolean userHasCreatePermission) {
    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry("fieldJson", fieldJson)
        .containsEntry("operatorExists", fieldJson.operatorExists())
        .containsEntry("licencesExist", fieldJson.licencesExist())
        .containsEntry("startApplicationEnabled",
            fieldJson.operatorExists() && fieldJson.licencesExist() && userHasCreatePermission)
        .containsEntry("operatorName", fieldJson.getOperatorName())
        .containsEntry("licences", fieldJson.getLicencesAsString())
        .containsEntry("startApplicationUrl", ReverseRouter.route(on(StartApplicationFromFieldController.class)
            .getStartApplicationForm(fieldJson.getId())));
  }
}
