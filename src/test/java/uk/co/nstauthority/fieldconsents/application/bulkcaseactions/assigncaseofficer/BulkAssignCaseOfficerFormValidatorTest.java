package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class BulkAssignCaseOfficerFormValidatorTest {

  private static final String REQUIRED = "required";

  private static final String CASE_OFFICER_WUA_ID = "caseOfficerWuaId";
  private static final String SELECTED_APPLICATION_IDS = "selectedApplicationIds";

  private static final String SELECT_A_CASE_OFFICER = "Select a case officer";
  private static final String SELECT_AT_LEAST_ONE_APPLICATION = "Select at least one application";

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Spy
  @InjectMocks
  private BulkAssignCaseOfficerFormValidator validator;

  @Test
  void validate() {
    var form = BulkAssignCaseOfficerForm.empty();
    var bindingResult = getBindingResult(form);

    validator.validate(form, bindingResult);

    verify(validator).validateCaseOfficerWuaId(form, bindingResult);
    verify(validator).validateSelectedApplicationIds(form, bindingResult);
  }

  @Test
  void validateCaseOfficerWuaId_nullCaseOfficerWuaId() {
    var form = new BulkAssignCaseOfficerForm(null, Set.of("1", "2", "3"));
    var bindingResult = getBindingResult(form);

    validator.validateCaseOfficerWuaId(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(tuple(CASE_OFFICER_WUA_ID, REQUIRED, SELECT_A_CASE_OFFICER));
  }

  @Test
  void validateCaseOfficerWuaId_invalidCaseOfficerWuaId() {
    var form = new BulkAssignCaseOfficerForm("invalid", Set.of("1", "2", "3"));
    var bindingResult = getBindingResult(form);

    validator.validateCaseOfficerWuaId(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(tuple(CASE_OFFICER_WUA_ID, REQUIRED, SELECT_A_CASE_OFFICER));
  }

  @Test
  void validateCaseOfficerWuaId_wuaIdIsNotCaseOfficer() {
    var form = new BulkAssignCaseOfficerForm("1", Set.of("1", "2", "3"));
    var bindingResult = getBindingResult(form);

    var webUserAccountId = WebUserAccountId.valueOf(form.caseOfficerWuaId());

    when(teamQueryService.userHasStaticRole(webUserAccountId, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(false);

    validator.validateCaseOfficerWuaId(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(tuple(CASE_OFFICER_WUA_ID, REQUIRED, SELECT_A_CASE_OFFICER));
  }

  @Test
  void validateCaseOfficerWuaId_energyPortalUserDoesNotExist() {
    var form = new BulkAssignCaseOfficerForm("1", Set.of("1", "2", "3"));
    var bindingResult = getBindingResult(form);

    var webUserAccountId = WebUserAccountId.valueOf(form.caseOfficerWuaId());

    when(teamQueryService.userHasStaticRole(webUserAccountId, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);
    when(energyPortalUserService.findByWuaId(WebUserAccountId.from(1L))).thenReturn(Optional.empty());

    validator.validateCaseOfficerWuaId(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(tuple(CASE_OFFICER_WUA_ID, REQUIRED, SELECT_A_CASE_OFFICER));
  }

  @Test
  void validateCaseOfficerWuaId() {
    var form = new BulkAssignCaseOfficerForm("1", Set.of("1", "2", "3"));
    var bindingResult = getBindingResult(form);

    var webUserAccountId = WebUserAccountId.valueOf(form.caseOfficerWuaId());

    when(teamQueryService.userHasStaticRole(webUserAccountId, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);
    when(energyPortalUserService.findByWuaId(WebUserAccountId.from(1L))).thenReturn(Optional.of(ENERGY_PORTAL_USER_1));

    validator.validateCaseOfficerWuaId(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validateSelectedApplicationIds_noSelectedApplicationIds_null() {
    var form = new BulkAssignCaseOfficerForm("1", null);
    var bindingResult = getBindingResult(form);

    validator.validateSelectedApplicationIds(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(tuple(SELECTED_APPLICATION_IDS, REQUIRED, SELECT_AT_LEAST_ONE_APPLICATION));
  }

  @Test
  void validateSelectedApplicationIds_noSelectedApplicationIds_emptySet() {
    var form = new BulkAssignCaseOfficerForm("1", Collections.emptySet());
    var bindingResult = getBindingResult(form);

    validator.validateSelectedApplicationIds(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(tuple(SELECTED_APPLICATION_IDS, REQUIRED, SELECT_AT_LEAST_ONE_APPLICATION));
  }

  @Test
  void validateSelectedApplicationIds_invalidSelectedApplicationIds() {
    var form = new BulkAssignCaseOfficerForm("1", Set.of("1", "2", "three"));
    var bindingResult = getBindingResult(form);

    validator.validateSelectedApplicationIds(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(tuple(SELECTED_APPLICATION_IDS, REQUIRED, SELECT_AT_LEAST_ONE_APPLICATION));
  }

  @Test
  void validateSelectedApplicationIds() {
    var form = new BulkAssignCaseOfficerForm("1", Set.of("1", "2", "3"));
    var bindingResult = getBindingResult(form);

    validator.validateSelectedApplicationIds(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  private BindingResult getBindingResult(BulkAssignCaseOfficerForm form) {
    return new BeanPropertyBindingResult(form, "form");
  }

}
