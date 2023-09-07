package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService.ALL_ORG_UNITS_DATA_ITEM_PURPOSE;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUserAction.RESUME_APPLICATION;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUserAction.VIEW_APPLICATION;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.caseOfficer;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForField;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentAssignedForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentVersion2InProgressForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.portalUserDtosMap;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.submitter;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.technicalReviewer;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.viewer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class ApplicationDataItemDtoServiceTest {

  @Mock
  private FieldService fieldService;
  @Mock
  private EnergyPortalUserService energyPortalUserService;
  @Mock
  private OrganisationUnitService organisationUnitService;
  @Mock
  private ApplicationService applicationService;
  @Mock
  private ApplicationVersionService applicationVersionService;
  @Mock
  private PermissionService permissionService;
  @InjectMocks
  private ApplicationDataItemDtoService applicationDataItemDtoService;


  @Test
  void getOrganisationUnitJsonsFromApplicationDataItemDtos_emptyList() {
    var applicationDataItemDtoTerminal = ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal();
    when(organisationUnitService.getOrganisationUnitsByIds(List.of(PRIMARY_OPERATOR_OU_ID_1), ALL_ORG_UNITS_DATA_ITEM_PURPOSE))
        .thenReturn(Collections.emptyList());

    assertThat(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(
        applicationDataItemDtoTerminal)))
        .isEmpty();
  }

  @Test
  void getOrganisationUnitJsonsFromApplicationDataItemDtos_nonEmptyList() {
    var applicationDataItemDtoTerminal = ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal();
    when(organisationUnitService.getOrganisationUnitsByIds(List.of(PRIMARY_OPERATOR_OU_ID_1), ALL_ORG_UNITS_DATA_ITEM_PURPOSE))
        .thenReturn(List.of(OrganisationUnitTestUtil.orgUnit1Json));

    assertThat(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(
        applicationDataItemDtoTerminal)))
        .containsExactly(OrganisationUnitTestUtil.orgUnit1Json);
  }

  @Test
  void getFieldJsonMapFromApplicationDataItemDtos_emptyMap() {
    var applicationDataItemDtoField = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(Collections.emptyList());

    assertThat(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDtoField)))
        .isEmpty();
  }

  @Test
  void getFieldJsonMapFromApplicationDataItemDtos_nonEmptyMap() {
    var applicationDataItemDtoField = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(List.of(field1JsonWithOperator));

    assertThat(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDtoField)))
        .isEqualTo(Map.of(FIELD_ID_1, field1JsonWithOperator));
  }

  @Test
  void getEnergyPortalUserDtoMapFromApplicationDataItemDtos() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentAssignedForTerminal();
    var portalUserWuaIdList = List.of(
        WebUserAccountId.from(applicationDataItemDto.getSubmittedByWuaId()),
        WebUserAccountId.from(applicationDataItemDto.getCaseOfficerWuaId()),
        WebUserAccountId.from(applicationDataItemDto.getTechnicalReviewerWuaId())
    );

    when(energyPortalUserService.findByWuaIds(portalUserWuaIdList))
        .thenReturn(List.of(submitter, caseOfficer, technicalReviewer));

    assertThat(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(List.of(applicationDataItemDto)))
        .isEqualTo(portalUserDtosMap);
  }

  @Test
  void getDisplayReference_whenApplicationInProgressAndUserCanResume() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, RESUME_APPLICATION))
        .isEqualTo("Resume application");
  }

  @Test
  void getDisplayReference_whenApplicationVersion2InProgressAndUserCanResume() {
    var ventAppVersion = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.VENT, 1, 2);
    when(applicationService.generateApplicationReference(ventAppVersion)).thenReturn("VCON/500/0 (Version 2)");
    when(applicationVersionService.getApplicationVersionById(ventAppVersion.getId())).thenReturn(ventAppVersion);

    var applicationDataItemDto = getApplicationDataItemDtoForShortVentVersion2InProgressForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, RESUME_APPLICATION))
        .isEqualTo("Resume VCON/500/0 (Version 2)");
  }

  @Test
  void getDisplayReference_whenApplicationSubmitted() {
    var ventAppVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    when(applicationService.generateApplicationReference(ventAppVersion)).thenReturn("VCON/500/0 (Version 1)");
    when(applicationVersionService.getApplicationVersionById(ventAppVersion.getId())).thenReturn(ventAppVersion);

    var applicationDataItemDto = getApplicationDataItemDtoForShortVentSubmittedForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, RESUME_APPLICATION))
        .isEqualTo("VCON/500/0 (Version 1)");
  }

  @Test
  void getDisplayReference_whenApplicationInProgressAndUserCanView() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, VIEW_APPLICATION))
        .isEqualTo("View application");
  }

  @Test
  void getDisplayReference_whenApplicationVersion2InProgressAndUserCanView() {
    var ventAppVersion = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.VENT, 1, 2);
    when(applicationService.generateApplicationReference(ventAppVersion)).thenReturn("VCON/500/0 (Version 2)");
    when(applicationVersionService.getApplicationVersionById(ventAppVersion.getId())).thenReturn(ventAppVersion);

    var applicationDataItemDto = getApplicationDataItemDtoForShortVentVersion2InProgressForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, VIEW_APPLICATION))
        .isEqualTo("View VCON/500/0 (Version 2)");
  }

  @Test
  void getApplicationDataItemUserActionFromUser_whenUserHasViewPermissions() {
    var viewerUser = ServiceUserDetail.from(viewer);
    when(permissionService.hasPermission(viewerUser, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS)))
        .thenReturn(false);

    assertThat(applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(viewerUser))
        .isEqualTo(VIEW_APPLICATION);
  }

  @Test
  void getApplicationDataItemUserActionFromUser_whenUserHasEditPermissions() {
    var editUser = ServiceUserDetail.from(submitter);
    when(permissionService.hasPermission(editUser, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS)))
        .thenReturn(true);

    assertThat(applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(editUser))
        .isEqualTo(RESUME_APPLICATION);
  }

  @Test
  void getDisplayConsentDuration_whenNotYetSpecified() {
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForProductionInProgressForFieldNoDuration();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEmpty();
  }

  @Test
  void getDisplayConsentDuration_shortTerm() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentAssignedForTerminal();
    var consentDurationString = applicationDataItemDto.getDuration().getShortDisplayName();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEqualTo(
            "%s %s - %s".formatted(consentDurationString,
                DateUtils.format(applicationDataItemDto.getShortTermStartDate(), DateUtils.SHORT_DATE),
                DateUtils.format(applicationDataItemDto.getShortTermEndDate(), DateUtils.SHORT_DATE)
            )
        );
  }

  @Test
  void getDisplayConsentDuration_Annual() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();
    var consentDurationString = applicationDataItemDto.getDuration().getShortDisplayName();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEqualTo("%s %s".formatted(consentDurationString, applicationDataItemDto.getConsentYear()));
  }

  @Test
  void getDisplayConsentDuration_LongTerm() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForTerminal();
    var consentDurationString = applicationDataItemDto.getDuration().getShortDisplayName();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEqualTo(
            "%s %d - %d".formatted(consentDurationString, applicationDataItemDto.getLongTermStartYear(), applicationDataItemDto.getLongTermEndYear())
        );
  }

  @Test
  void getDisplayAssetLocation_Field() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayAssetLocation(applicationDataItemDto, Map.of(FIELD_ID_1, field1JsonWithOperator)))
        .isEqualTo(field1JsonWithOperator.getGeographicArea().getDisplayName());
  }


  @Test
  void getDisplayAssetLocation_Terminal() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayAssetLocation(applicationDataItemDto, Map.of(FIELD_ID_1, field1JsonWithOperator)))
        .isEmpty();
  }

  @Test
  void getDisplayAssetLocation_unknownArea() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayAssetLocation(applicationDataItemDto, Map.of(FIELD_ID_2, field2JsonWithOperator)))
        .isEqualTo("Unknown area");
  }

  @Test
  void getDisplaySubmitter_Field() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForField();

    assertThat(applicationDataItemDtoService.getDisplaySubmitter(applicationDataItemDto, portalUserDtosMap))
        .isEqualTo("Submitter: %s".formatted(submitter.displayName()));
  }

  @Test
  void getDisplaySubmitter_Terminal() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForTerminal();

    assertThat(applicationDataItemDtoService.getDisplaySubmitter(applicationDataItemDto, portalUserDtosMap))
        .isEqualTo("Submitter: %s".formatted(submitter.displayName()));
  }

  @Test
  void getDisplayAceFlag_whenTrue() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForField();

    assertThat(applicationDataItemDtoService.getDisplayAceFlag(applicationDataItemDto))
        .isEqualTo("ACE");
  }

  @Test
  void getDisplayAceFlag_whenFalse() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayAceFlag(applicationDataItemDto))
        .isEmpty();
  }

  @Test
  void getDisplayCaseOfficer_withNoCaseOfficer() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayCaseOfficer(applicationDataItemDto, portalUserDtosMap))
        .isEmpty();
  }

  @Test
  void getDisplayCaseOfficer_withCaseOfficer() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentAssignedForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayCaseOfficer(applicationDataItemDto, portalUserDtosMap))
        .isEqualTo("Case officer: %s".formatted(caseOfficer.displayName()));
  }

  @Test
  void getDisplayTechnicalReviewer_withIndustryType() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayTechnicalReviewer(applicationDataItemDto, portalUserDtosMap, TeamType.INDUSTRY))
        .isEmpty();
  }

  @Test
  void getDisplayTechnicalReviewer_withNoTechnicalReviewer() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayTechnicalReviewer(applicationDataItemDto, portalUserDtosMap, TeamType.REGULATOR))
        .isEmpty();
  }

  @Test
  void getDisplayTechnicalReviewer_withTechnicalReviewer() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForField();

    assertThat(applicationDataItemDtoService.getDisplayTechnicalReviewer(applicationDataItemDto, portalUserDtosMap, TeamType.REGULATOR))
        .isEqualTo("Technical reviewer: %s".formatted(technicalReviewer.displayName()));
  }
}
