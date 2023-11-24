package uk.co.nstauthority.fieldconsents.integrationtest.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService.CONSULTATION_TEAM_TYPE;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.ANNUAL_CONSENT_YEAR;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CASE_MANAGER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CASE_OFFICER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CASE_OFFICER_ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CONSULTATION_TEAM;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CONSULTEE_ALLOCATOR_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CONSULTEE_ALLOCATOR_ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CONSULTEE_RESPONDER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CONSULTEE_RESPONDER_ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.INDUSTRY_TEAM;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.PORTAL_USERS_DTO_MAP;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.REGULATOR_TEAM;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.TECHNICAL_REVIEWER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.USER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.getApplicationDataItemForFieldInProgressOfType;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.getApplicationDataItemForTerminalInProgressOfType;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.getApplicationDataItemInProgressOfTypeAndLength;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.getApplicationDataItemProductionAssignedOfConsentLength;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.getApplicationDataItemProductionSubmittedOfConsentLength;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.getApplicationDataItemProductionWithConsultationConsentLength;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.getApplicationDataItemProductionWithReviewOpenOfConsentLength;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD2_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL2_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.REGULATOR_PERMISSIONS;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.AdditionalAssetsService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthForm;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilterForm;

@Transactional
class WorkAreaIntegrationTest extends AbstractIntegrationTest {

  @MockBean
  private TeamService teamService;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private OrganisationUnitService organisationUnitService;

  @MockBean
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  @MockBean
  private FieldService fieldService;

  @MockBean
  private AssetService assetService;

  @MockBean
  private OrganisationGroupQueryService organisationGroupQueryService;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  @MockBean
  private OpredTeamService opredTeamService;

  @Autowired
  private CaseAssignmentService caseAssignmentService;

  @Autowired
  private TechnicalReviewService technicalReviewService;

  @Autowired
  private ConsultationService consultationService;

  @Autowired
  private WorkAreaController workAreaController;

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ConsentLengthService consentLengthService;

  @Autowired
  private AdditionalAssetsService additionalAssetsService;

  @Autowired
  private ApplicationUpdateService applicationUpdateService;

  @Autowired
  private Clock clock;

  private WorkAreaFilterForm workAreaFilterForm;
  private RestSearchItem assetFieldRestSearchItem;
  private RestSearchItem assetTerminalRestSearchItem;
  private RestSearchItem orgUnitRestSearchItem;
  private ZonedDateTime zonedDateTime;

  @BeforeEach
  void setUp() {
    truncateApplicationsCascade();
    workAreaFilterForm = new WorkAreaFilterForm();
    zonedDateTime = ZonedDateTime.now(clock.getZone());
    when(teamService.isIndustryUser(USER_DETAIL)).thenReturn(true);
    when(organisationUnitService.getOrganisationUnitsByIds(
        ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(Collections.singletonList(
        OrganisationUnitTestUtil.orgUnit1Json));

    assetFieldRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    assetTerminalRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    orgUnitRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(null)).thenReturn(assetFieldRestSearchItem);
    when(applicationDataFilterFormService.getPrefilledOrganisation(null)).thenReturn(orgUnitRestSearchItem);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(
        USER_DETAIL,
        TeamType.INDUSTRY,
        EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS))
    ).thenReturn(Collections.singletonList(INDUSTRY_TEAM));

    when(organisationGroupQueryService
        .getOrganisationUnitsByOrganisationGroupIds(List.of(INDUSTRY_TEAM.getOrganisationGroupId())))
        .thenReturn(List.of(orgUnit1Json));

    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        field1JsonWithOperatorAndLicences));

    when(energyPortalUserService.getEnergyPortalUserMap(ArgumentMatchers.anyList())).thenReturn(PORTAL_USERS_DTO_MAP);

    when(teamService.isRegulatorUser(CASE_MANAGER_DETAIL)).thenReturn(true);
    when(permissionService.hasPermission(CASE_MANAGER_DETAIL, EnumSet.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(CASE_MANAGER_DETAIL, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(Collections.singletonList(REGULATOR_TEAM));

    when(teamService.isRegulatorUser(CASE_OFFICER_DETAIL)).thenReturn(true);
    when(permissionService.hasPermission(CASE_OFFICER_DETAIL, EnumSet.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(CASE_OFFICER_DETAIL, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(Collections.singletonList(REGULATOR_TEAM));

    when(teamService.isRegulatorUser(TECHNICAL_REVIEWER_DETAIL)).thenReturn(true);
    when(permissionService.hasPermission(TECHNICAL_REVIEWER_DETAIL, EnumSet.of(RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS))).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(TECHNICAL_REVIEWER_DETAIL, TeamType.REGULATOR, REGULATOR_PERMISSIONS))
        .thenReturn(Collections.singletonList(REGULATOR_TEAM));

    when(teamService.isConsulteeUser(CONSULTEE_ALLOCATOR_DETAIL)).thenReturn(true);
    when(permissionService.hasPermission(CONSULTEE_ALLOCATOR_DETAIL, EnumSet.of(RolePermission.ALLOCATE_CONSULTATION))).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(CONSULTEE_ALLOCATOR_DETAIL, TeamType.OPRED, EnumSet.of(RolePermission.ALLOCATE_CONSULTATION, RolePermission.RESPOND_TO_CONSULTATION)))
        .thenReturn(Collections.singletonList(CONSULTATION_TEAM));

    when(teamService.isConsulteeUser(CONSULTEE_RESPONDER_DETAIL)).thenReturn(true);
    when(permissionService.hasPermission(CONSULTEE_RESPONDER_DETAIL, EnumSet.of(RolePermission.RESPOND_TO_CONSULTATION))).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(CONSULTEE_RESPONDER_DETAIL, TeamType.OPRED, EnumSet.of(RolePermission.ALLOCATE_CONSULTATION, RolePermission.RESPOND_TO_CONSULTATION)))
        .thenReturn(Collections.singletonList(CONSULTATION_TEAM));

    when(teamService.getWuaIdsOfTeamMembersWithRoles(
        TeamType.OPRED,
        Set.of(OpredTeamRole.ALLOCATOR)
    )).thenReturn(List.of(WebUserAccountId.from(CONSULTEE_ALLOCATOR_DETAIL)));
    when(energyPortalUserService.findByWuaIds(List.of(WebUserAccountId.from(CONSULTEE_ALLOCATOR_DETAIL))))
        .thenReturn(List.of(CONSULTEE_ALLOCATOR_ENERGY_PORTAL_USER_DTO));
    when(opredTeamService.isAccessManager(CONSULTATION_TEAM.toTeamId(), CONSULTEE_ALLOCATOR_DETAIL)).thenReturn(true);
  }

  /*********************************** REFERENCE NUMBER ***********************************/
  @Test
  void getWorkAreaItemsForIndustryByReferenceNumber_foundWhenValidApplicationNumber() {
    workAreaFilterForm.setReferenceNumber("10");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByReferenceNumber_notFoundWhenNotANumber() {
    workAreaFilterForm.setReferenceNumber("abc");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  /*********************************** APPLICATION STATUS ***********************************/
  @Test
  void getWorkAreaItemsForIndustryByStatus_foundWhenInProgress() {
    workAreaFilterForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationId = createNewApplicationVersionForField(ApplicationType.FLARE, consentLengthForm).getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems)
        .containsExactly(getApplicationDataItemForFieldInProgressOfType(applicationId, ApplicationType.FLARE, field1JsonWithOperatorAndLicences));
  }

  @Test
  void getWorkAreaItemsForIndustryByStatus_notFoundWhenInProgress() {
    workAreaFilterForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  @Test
  void getWorkAreaItemsForIndustryByStatus_foundWhenSubmitted() {
    workAreaFilterForm.setStatuses(List.of(ApplicationVersionStatus.SUBMITTED));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems)
        .containsExactly(getApplicationDataItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM));
  }

  /*********************************** APPLICATION TYPE ***********************************/
  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getWorkAreaItemsForIndustryByApplicationType_whenFound(ApplicationType applicationType) {
    workAreaFilterForm.setApplicationTypes(List.of(applicationType));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, applicationType, field1JsonWithOperatorAndLicences)
    );
  }
  
  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getWorkAreaItemsForIndustryByApplicationType_whenFoundWithMultipleTypes(ApplicationType applicationType) {
    workAreaFilterForm.setApplicationTypes(Arrays.asList(ApplicationType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, applicationType, field1JsonWithOperatorAndLicences)
    );
  }
  
  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "VENT", mode = EnumSource.Mode.EXCLUDE)
  void getWorkAreaItemsForIndustryByApplicationType_whenNotFound(ApplicationType applicationType) {
    workAreaFilterForm.setApplicationTypes(List.of(applicationType));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.VENT, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  /*********************************** CONSENT LENGTH TYPE ***********************************/
  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenFoundWithAnnualTerm() {
    workAreaFilterForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.ANNUAL)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenFoundWithShortTerm() {
    workAreaFilterForm.setDurationTypes(List.of(ConsentLengthType.SHORT_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenLongTermAndFoundWithMultipleDurationTypes() {
    workAreaFilterForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.LONG_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenAnnualAndFoundWithMultipleDurationTypes() {
    workAreaFilterForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.ANNUAL)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenShortTermAndFoundWithMultipleDurationTypes() {
    workAreaFilterForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenFoundWithLongTerm() {
    workAreaFilterForm.setDurationTypes(List.of(ConsentLengthType.LONG_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.LONG_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenNotFoundWithAnnualTerm() {
    workAreaFilterForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenNotFoundWithShortTerm() {
    workAreaFilterForm.setDurationTypes(List.of(ConsentLengthType.SHORT_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  @Test
  void getWorkAreaItemsForIndustryByConsentDuration_whenNotFoundWithLongTerm() {
    workAreaFilterForm.setDurationTypes(List.of(ConsentLengthType.LONG_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  /************************************** PRIMARY FIELDS **************************************/
  @Test
  void getWorkAreaItemsForIndustryByField_whenFoundWithPrimaryField() {
    workAreaFilterForm.setAssetKey(FIELD1_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD1_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD1_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    doReturn(Optional.of(field1JsonWithOperatorAndLicences)).when(assetService).getAsset(any(AssetKey.class), anyString());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION, field1JsonWithOperatorAndLicences)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByField_whenNotFoundWithPrimaryField() {
    workAreaFilterForm.setAssetKey(FIELD2_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD2_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    doReturn(Optional.of(field2JsonWithOperatorAndLicences)).when(assetService).getAsset(any(AssetKey.class), anyString());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  /****************************************** FACILITIES ******************************************/
  @Test
  void getWorkAreaItemsForIndustryByFacility_whenFound() {
    workAreaFilterForm.setAssetKey(TERMINAL1_ASSET_KEY);
    assetTerminalRestSearchItem = ApplicationDataFilterFormTestUtil.TERMINAL1_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(TERMINAL1_ASSET_KEY)).thenReturn(assetTerminalRestSearchItem);
    doReturn(Optional.of(terminal1JsonWithOperator)).when(assetService).getAsset(any(AssetKey.class), anyString());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForTerminal(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForTerminalInProgressOfType(applicationId, ApplicationType.PRODUCTION, terminal1JsonWithOperator)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByFacility_whenNotFound() {
    workAreaFilterForm.setAssetKey(TERMINAL2_ASSET_KEY);
    assetTerminalRestSearchItem = ApplicationDataFilterFormTestUtil.TERMINAL2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(TERMINAL2_ASSET_KEY)).thenReturn(assetTerminalRestSearchItem);
    doReturn(Optional.of(terminal2JsonWithOperator)).when(assetService).getAsset(any(AssetKey.class), anyString());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForTerminal(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  /*********************************** PRIMARY OPERATOR ***********************************/
  @Test
  void getWorkAreaItemsForIndustryByPrimaryOperator_whenFound() {
    orgUnitRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);

    workAreaFilterForm.setOperatorId(Integer.parseInt(orgUnitRestSearchItem.id()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION, field1JsonWithOperatorAndLicences)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryByPrimaryOperator_whenNotFound() {
    workAreaFilterForm.setOperatorId(2);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).isEmpty();
  }

  /*********************************** SUBMISSION YEAR ***********************************/
  @Test
  void getWorkAreaItemsForIndustryBySubmissionYear_whenFound() {
    workAreaFilterForm.setSubmittedYear(String.valueOf(zonedDateTime.getYear()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForIndustryBySubmissionYear_whenNotFound() {
    workAreaFilterForm.setSubmittedYear(String.valueOf(zonedDateTime.getYear() - 1));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).isEmpty();
  }

  /************************************** GEOGRAPHIC LOCATION **************************************/
  @ParameterizedTest
  @MethodSource("geographicAreasToFieldJsonsWhenFound")
  void getWorkAreaItemsForIndustryByGeographicLocation_whenFound(GeographicArea geographicArea,
                                               FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    workAreaFilterForm.setGeographicAreas(List.of(geographicArea));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION, fieldWithOperatorAndLicencesJson)
    );
  }

  @ParameterizedTest
  @MethodSource("geographicAreasToFieldJsonsWhenNotFound")
  void getWorkAreaItemsForIndustryByGeographicLocation_whenNotFound(GeographicArea geographicArea,
                                                         FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    workAreaFilterForm.setGeographicAreas(List.of(geographicArea));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  private static Stream<Arguments> geographicAreasToFieldJsonsWhenFound() {
    return Stream.of(
        arguments(GeographicArea.CNS, field1JsonWithOperatorAndLicences),
        arguments(GeographicArea.SNS, field2JsonWithOperatorAndLicences),
        arguments(GeographicArea.NNS, field3JsonWithOperatorAndLicences)
    );
  }

  private static Stream<Arguments> geographicAreasToFieldJsonsWhenNotFound() {
    return Stream.of(
        arguments(GeographicArea.CNS, field2JsonWithOperatorAndLicences),
        arguments(GeographicArea.SNS, field3JsonWithOperatorAndLicences),
        arguments(GeographicArea.NNS, field1JsonWithOperatorAndLicences)
    );
  }

  /************************************** ASSET TYPES **************************************/
  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToFieldJsonsWhenFound")
  void getWorkAreaItemsForIndustryByAssetType_whenFoundWithPrimaryFieldAssetType(AssetTypeWithShore assetTypeWithShore,
                                                            FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    workAreaFilterForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION, fieldWithOperatorAndLicencesJson)
    );
  }

  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToPrimaryAndSecondaryFieldJsonsWhenFound")
  void getWorkAreaItemsForIndustryByAssetType_whenFoundWithSecondaryFieldAssetType(AssetTypeWithShore assetTypeWithShore,
                                                                        FieldWithOperatorAndLicencesJson primaryFieldWithOperatorAndLicencesJson,
                                                                        FieldWithOperatorAndLicencesJson secondaryFieldWithOperatorAndLicencesJson) {
    workAreaFilterForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString()))
        .thenReturn(List.of(primaryFieldWithOperatorAndLicencesJson, secondaryFieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, primaryFieldWithOperatorAndLicencesJson);
    additionalAssetsService.saveAdditionalAsset(applicationVersion, secondaryFieldWithOperatorAndLicencesJson);

    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION, primaryFieldWithOperatorAndLicencesJson)
    );
  }

  @ParameterizedTest
  @MethodSource("fieldJsonsWithMultipleAssetTypesFound")
  void getWorkAreaItemsForIndustryByAssetType_whenFoundWithFieldAndMultipleAssetTypes(FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    workAreaFilterForm.setAssetTypesWithShore(Arrays.asList(AssetTypeWithShore.values()));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION, fieldWithOperatorAndLicencesJson)
    );
  }

  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToFieldJsonsWhenNotFound")
  void getWorkAreaItemsForIndustryByAssetType_whenNotFoundWithFieldAssetType(AssetTypeWithShore assetTypeWithShore,
                                                                  FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    workAreaFilterForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  @Test
  void getWorkAreaItemsForIndustryByAssetType_whenFoundWithFacilityAssetType() {
    workAreaFilterForm.setAssetTypesWithShore(List.of(AssetTypeWithShore.TERMINAL));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForTerminalJson(ApplicationType.PRODUCTION, consentLengthForm, terminal1JsonWithOperator);

    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForTerminalInProgressOfType(applicationId, ApplicationType.PRODUCTION, terminal1JsonWithOperator)
    );
  }

  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToFieldJsonsWhenFound")
  void getWorkAreaItemsForIndustryByAssetType_whenNotFoundWithFacilityAssetType(AssetTypeWithShore assetTypeWithShore,
                                                                     FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    workAreaFilterForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForTerminalJson(ApplicationType.PRODUCTION, consentLengthForm, terminal1JsonWithOperator);

    assertThat(getWorkAreaItems(workAreaFilterForm, USER_DETAIL)).isEmpty();
  }

  @Test
  void getWorkAreaItemsForIndustryByAssetType_whenFoundWithFacilityAndMultipleAssetTypes() {
    workAreaFilterForm.setAssetTypesWithShore(Arrays.asList(AssetTypeWithShore.values()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForTerminalJson(ApplicationType.PRODUCTION, consentLengthForm, terminal1JsonWithOperator);

    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemForTerminalInProgressOfType(applicationId, ApplicationType.PRODUCTION, terminal1JsonWithOperator)
    );
  }

  private static Stream<Arguments> assetTypesWithShoreToFieldJsonsWhenFound() {
    return Stream.of(
        arguments(AssetTypeWithShore.FIELD_OFFSHORE, field1JsonWithOperatorAndLicences),
        arguments(AssetTypeWithShore.FIELD_ONSHORE, field2JsonWithOperatorAndLicences),
        arguments(AssetTypeWithShore.FIELD_UNKNOWN, field3JsonWithOperatorAndLicences)
    );
  }

  private static Stream<Arguments> assetTypesWithShoreToPrimaryAndSecondaryFieldJsonsWhenFound() {
    return Stream.of(
        arguments(AssetTypeWithShore.FIELD_OFFSHORE, field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences),
        arguments(AssetTypeWithShore.FIELD_ONSHORE, field2JsonWithOperatorAndLicences, field3JsonWithOperatorAndLicences),
        arguments(AssetTypeWithShore.FIELD_UNKNOWN, field3JsonWithOperatorAndLicences, field1JsonWithOperatorAndLicences)
    );
  }

  private static Stream<Arguments> assetTypesWithShoreToFieldJsonsWhenNotFound() {
    return Stream.of(
        arguments(AssetTypeWithShore.FIELD_OFFSHORE, field2JsonWithOperatorAndLicences),
        arguments(AssetTypeWithShore.FIELD_ONSHORE, field3JsonWithOperatorAndLicences),
        arguments(AssetTypeWithShore.FIELD_UNKNOWN, field1JsonWithOperatorAndLicences)
    );
  }

  private static Stream<Arguments> fieldJsonsWithMultipleAssetTypesFound() {
    return Stream.of(
        arguments(field1JsonWithOperatorAndLicences),
        arguments(field2JsonWithOperatorAndLicences),
        arguments(field3JsonWithOperatorAndLicences)
    );
  }

  /********************************** CASE OFFICER ASSIGNED **********************************/
  @Test
  void getWorkAreaItemsForRegulatorByCaseOfficerAssigned_whenFound() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(
        TeamType.REGULATOR,
        Set.of(RegulatorTeamRole.CASE_OFFICER)
    )).thenReturn(List.of(WebUserAccountId.from(CASE_OFFICER_DETAIL)));
    when(energyPortalUserService.findByWuaIds(List.of(WebUserAccountId.from(CASE_OFFICER_DETAIL))))
        .thenReturn(List.of(CASE_OFFICER_ENERGY_PORTAL_USER_DTO));

    workAreaFilterForm.setCaseOfficerWuaId(CASE_OFFICER_DETAIL.wuaId());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createAssignedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, CASE_MANAGER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemProductionAssignedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForRegulatorByCaseOfficerAssigned_whenNotFound() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(
        TeamType.REGULATOR,
        Set.of(RegulatorTeamRole.CASE_OFFICER)
    )).thenReturn(List.of(WebUserAccountId.from(CASE_OFFICER_DETAIL), WebUserAccountId.from(USER_DETAIL)));
    when(energyPortalUserService.findByWuaIds(List.of(WebUserAccountId.from(CASE_OFFICER_DETAIL), WebUserAccountId.from(USER_DETAIL))))
        .thenReturn(List.of(CASE_OFFICER_ENERGY_PORTAL_USER_DTO, ENERGY_PORTAL_USER_DTO));

    workAreaFilterForm.setCaseOfficerWuaId(CASE_OFFICER_DETAIL.wuaId());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createAssignedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm, USER_DETAIL);

    assertThat(getWorkAreaItems(workAreaFilterForm, CASE_MANAGER_DETAIL)).isEmpty();
  }

  /********************************** TECHNICAL REVIEWER ASSIGNED **********************************/
  @Test
  void getWorkAreaItemsForRegulatorByTechnicalReviewerAssigned_whenFound() {
    when(caseAssignmentService.getCurrentCaseOfficers()).thenReturn(List.of(CASE_OFFICER_ENERGY_PORTAL_USER_DTO));

    workAreaFilterForm.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_DETAIL.wuaId());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createApplicationVersionWithAssignedReviewer(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL, TECHNICAL_REVIEWER_DETAIL, zonedDateTime.plusDays(7).toInstant());
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, CASE_OFFICER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemProductionWithReviewOpenOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void getWorkAreaItemsForRegulatorByTechnicalReviewerAssigned_whenNotFound() {
    when(caseAssignmentService.getCurrentCaseOfficers()).thenReturn(List.of(CASE_OFFICER_ENERGY_PORTAL_USER_DTO));

    workAreaFilterForm.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_DETAIL.wuaId());

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createApplicationVersionWithAssignedReviewer(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL, USER_DETAIL, zonedDateTime.plusDays(7).toInstant());

    assertThat(getWorkAreaItems(workAreaFilterForm, CASE_OFFICER_DETAIL)).isEmpty();
  }

  /********************************** COLSULTEE USER **********************************/
  @Test
  void getWorkAreaItemsForConsulteeAllocator_whenConsultationOpen() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createApplicationVersionWithOpenConsultation(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL, zonedDateTime.plusDays(7).toInstant());
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, CONSULTEE_ALLOCATOR_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemProductionWithConsultationConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM, zonedDateTime.plusDays(7).toInstant())
    );
  }

  @Test
  void getWorkAreaItemsForConsulteeAllocator_whenNoConsultationOpen() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createAssignedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL);

    assertThat(getWorkAreaItems(workAreaFilterForm, CONSULTEE_ALLOCATOR_DETAIL)).isEmpty();
  }

  @Test
  @Disabled(value = "application not found - FCS-520")
  void getWorkAreaItemsForConsulteeResponder_whenConsultationOpen() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(
        TeamType.OPRED,
        Set.of(OpredTeamRole.RESPONDER)
    )).thenReturn(List.of(WebUserAccountId.from(CONSULTEE_RESPONDER_DETAIL)));
    when(energyPortalUserService.findByWuaIds(List.of(WebUserAccountId.from(CONSULTEE_RESPONDER_DETAIL))))
        .thenReturn(List.of(CONSULTEE_RESPONDER_ENERGY_PORTAL_USER_DTO));
    when(opredTeamService.isResponder(CONSULTATION_TEAM.toTeamId(), CONSULTEE_RESPONDER_DETAIL)).thenReturn(true);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createApplicationVersionWithConsultationResponder(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL);
    var applicationId = applicationVersion.getApplication().getId();

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, CONSULTEE_RESPONDER_DETAIL);
    assertThat(workAreaItems).containsExactly(
        getApplicationDataItemProductionWithConsultationConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM, zonedDateTime.plusDays(7).toInstant())
    );
  }

  @Test
  void getWorkAreaItemsForConsulteeResponder_whenNoConsultationOpen() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(
        TeamType.OPRED,
        Set.of(OpredTeamRole.RESPONDER)
    )).thenReturn(List.of(WebUserAccountId.from(CONSULTEE_RESPONDER_DETAIL)));
    when(energyPortalUserService.findByWuaIds(List.of(WebUserAccountId.from(CONSULTEE_RESPONDER_DETAIL))))
        .thenReturn(List.of(CONSULTEE_ALLOCATOR_ENERGY_PORTAL_USER_DTO));
    when(opredTeamService.isResponder(CONSULTATION_TEAM.toTeamId(), CONSULTEE_RESPONDER_DETAIL)).thenReturn(true);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createAssignedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL);

    assertThat(getWorkAreaItems(workAreaFilterForm, CONSULTEE_RESPONDER_DETAIL)).isEmpty();
  }

  /*********************************** SORTING INDUSTRY ***********************************/
  @Test
  void getWorkAreaItemsForIndustry_noUpdatesSortDescending() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var productionAppVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var flareAppVersion = createNewApplicationVersionForField(ApplicationType.FLARE, consentLengthForm);

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    var applicationVersionIds = workAreaItems.stream().map(ApplicationDataItem::applicationId).toList();

    assertThat(applicationVersionIds).containsExactly(
        flareAppVersion.getId(),
        productionAppVersion.getId()
    );
  }

  @Test
  void getWorkAreaItemsForIndustry_oneUpdateSortDeadlineAscending() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var updateAppVersion = createApplicationVersionWithOpenUpdateRequest(ApplicationType.PRODUCTION, consentLengthForm, zonedDateTime.plusDays(3).toInstant());
    var flareAppVersion = createNewApplicationVersionForField(ApplicationType.FLARE, consentLengthForm);

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    var applicationVersionIds = workAreaItems.stream().map(ApplicationDataItem::applicationId).toList();

    assertThat(applicationVersionIds).containsExactly(
        updateAppVersion.getId(),
        flareAppVersion.getId()
    );
  }

  @Test
  void getWorkAreaItemsForIndustry_twoUpdatesSortDeadlineAscending() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var update1AppVersion = createApplicationVersionWithOpenUpdateRequest(ApplicationType.PRODUCTION, consentLengthForm, zonedDateTime.plusDays(7).toInstant());
    var update2AppVersion = createApplicationVersionWithOpenUpdateRequest(ApplicationType.FLARE, consentLengthForm, zonedDateTime.plusDays(3).toInstant());
    var ventAppVersion = createNewApplicationVersionForField(ApplicationType.VENT, consentLengthForm);

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, USER_DETAIL);
    var applicationVersionIds = workAreaItems.stream().map(ApplicationDataItem::applicationId).toList();

    assertThat(applicationVersionIds).containsExactly(
        update2AppVersion.getId(),
        update1AppVersion.getId(),
        ventAppVersion.getId()
    );
  }

  /*********************************** SORTING REGULATOR ***********************************/
  @Test
  void getWorkAreaItemsForRegulatorCaseOfficer_sortDescending() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var productionAppVersion = createAssignedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL);
    var flareAppVersion = createAssignedApplicationVersion(ApplicationType.FLARE, consentLengthForm, CASE_OFFICER_DETAIL);

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, CASE_OFFICER_DETAIL);
    var applicationVersionIds = workAreaItems.stream().map(ApplicationDataItem::applicationId).toList();

    assertThat(applicationVersionIds).containsExactly(
        flareAppVersion.getId(),
        productionAppVersion.getId()
    );
  }

  @Test
  void getWorkAreaItemsForRegulatorCaseManager_sortDescending() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var productionAppVersion = createAssignedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL);
    var flareAppVersion = createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, CASE_MANAGER_DETAIL);
    var applicationVersionIds = workAreaItems.stream().map(ApplicationDataItem::applicationId).toList();

    assertThat(applicationVersionIds).containsExactly(
        flareAppVersion.getId(),
        productionAppVersion.getId()
    );
  }

  @Test
  void getWorkAreaItemsForRegulatorTechnicalReviewer_sortDeadlineAscending() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var productionAppVersion = createApplicationVersionWithAssignedReviewer(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL, TECHNICAL_REVIEWER_DETAIL, zonedDateTime.plusDays(3).toInstant());
    var flareAppVersion = createApplicationVersionWithAssignedReviewer(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL, TECHNICAL_REVIEWER_DETAIL, zonedDateTime.plusDays(2).toInstant());

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, TECHNICAL_REVIEWER_DETAIL);
    var applicationVersionIds = workAreaItems.stream().map(ApplicationDataItem::applicationId).toList();

    assertThat(applicationVersionIds).containsExactly(
        flareAppVersion.getId(),
        productionAppVersion.getId()
    );
  }

  /*********************************** SORTING CONSULTEE ***********************************/
  @Test
  void getWorkAreaItemsForConsultee_sortDeadlineAscending() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var productionAppVersion = createApplicationVersionWithOpenConsultation(ApplicationType.PRODUCTION, consentLengthForm, CASE_OFFICER_DETAIL, zonedDateTime.plusDays(7).toInstant());
    var flareAppVersion = createApplicationVersionWithOpenConsultation(ApplicationType.FLARE, consentLengthForm, CASE_OFFICER_DETAIL, zonedDateTime.plusDays(2).toInstant());

    var workAreaItems = getWorkAreaItems(workAreaFilterForm, CONSULTEE_ALLOCATOR_DETAIL);
    var applicationVersionIds = workAreaItems.stream().map(ApplicationDataItem::applicationId).toList();

    assertThat(applicationVersionIds).containsExactly(
        flareAppVersion.getId(),
        productionAppVersion.getId()
    );
  }
  /*****************************************************************************************/

  private ApplicationVersion createNewApplicationVersionForField(ApplicationType applicationType, ConsentLengthForm consentLengthForm) {
    var applicationVersion = applicationService.createNewApplicationForField(
        applicationType,
        field1JsonWithOperatorAndLicences,
        OrganisationUnitTestUtil.orgUnit1Json,
        USER_DETAIL
    );

    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    return applicationVersion;
  }

  private ApplicationVersion createNewApplicationVersionForTerminal(ApplicationType applicationType, ConsentLengthForm consentLengthForm) {
    var applicationVersion = applicationService.createNewApplicationForTerminal(
        applicationType,
        terminal1JsonWithOperator,
        OrganisationUnitTestUtil.orgUnit1Json,
        USER_DETAIL
    );

    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    return applicationVersion;
  }

  private ApplicationVersion createSubmittedApplicationVersion(ApplicationType applicationType, ConsentLengthForm consentLengthForm) {
    var applicationVersion = createAwaitingForPaymentApplicationVersion(applicationType, consentLengthForm);
    applicationService.submitApplication(applicationVersion, USER_DETAIL);

    return applicationVersion;
  }

  private ApplicationVersion createAssignedApplicationVersion(ApplicationType applicationType, ConsentLengthForm consentLengthForm, ServiceUserDetail caseOfficer) {
    var applicationVersion = createSubmittedApplicationVersion(applicationType, consentLengthForm);

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(caseOfficer.wuaId()))).thenReturn(true);

    caseAssignmentService.assignCaseOfficer(applicationVersion, caseOfficer, CASE_MANAGER_DETAIL);
    applicationVersion.setCaseOfficerWuaId(caseOfficer.wuaId());

    return applicationVersion;
  }

  private ApplicationVersion createApplicationVersionWithAssignedReviewer(ApplicationType applicationType,
                                                                          ConsentLengthForm consentLengthForm,
                                                                          ServiceUserDetail caseOfficer,
                                                                          ServiceUserDetail technicalReviewer,
                                                                          Instant deadlineInstant) {
    var applicationVersion = createAssignedApplicationVersion(applicationType, consentLengthForm, caseOfficer);
    applicationVersion.setCaseOfficerWuaId(caseOfficer.wuaId());

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(technicalReviewer.wuaId()))).thenReturn(true);

    technicalReviewService.saveTechnicalReviewRequest(applicationVersion, deadlineInstant,
        "request text", technicalReviewer, caseOfficer);

    return applicationVersion;
  }

  private ApplicationVersion createApplicationVersionWithOpenConsultation(ApplicationType applicationType,
                                                                          ConsentLengthForm consentLengthForm,
                                                                          ServiceUserDetail caseOfficer,
                                                                          Instant deadlineInstant) {
    var applicationVersion = createAssignedApplicationVersion(applicationType, consentLengthForm, caseOfficer);
    applicationVersion.setCaseOfficerWuaId(caseOfficer.wuaId());

    when(teamService.getTeamsByType(CONSULTATION_TEAM_TYPE)).thenReturn(Collections.singletonList(CONSULTATION_TEAM));

    consultationService.requestConsultation(applicationVersion, deadlineInstant, CONSULTEE_ALLOCATOR_DETAIL);

    return applicationVersion;
  }

  private ApplicationVersion createApplicationVersionWithConsultationResponder(ApplicationType applicationType,
                                                                               ConsentLengthForm consentLengthForm,
                                                                               ServiceUserDetail caseOfficer) {
    var applicationVersion = createAssignedApplicationVersion(applicationType, consentLengthForm, caseOfficer);
    applicationVersion.setCaseOfficerWuaId(caseOfficer.wuaId());

    when(teamService.getTeamsByType(CONSULTATION_TEAM_TYPE)).thenReturn(Collections.singletonList(CONSULTATION_TEAM));

    consultationService.requestConsultation(applicationVersion, zonedDateTime.plusDays(7).toInstant(), CONSULTEE_ALLOCATOR_DETAIL);

    var consultation = consultationService.getLatestOpenConsultation(applicationVersion.getApplication());
    consultationService.assignResponderToConsultation(consultation, CONSULTEE_ALLOCATOR_DETAIL, CONSULTEE_RESPONDER_DETAIL);

    return applicationVersion;
  }

  private ApplicationVersion createAwaitingForPaymentApplicationVersion(ApplicationType applicationType, ConsentLengthForm consentLengthForm) {
    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);

    applicationService.prepareApplicationForPayment(applicationVersion);
    return applicationVersion;
  }

  private ApplicationVersion createNewApplicationVersionForFieldJson(ApplicationType applicationType,
                                                                     ConsentLengthForm consentLengthForm,
                                                                     FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    var applicationVersion = applicationService.createNewApplicationForField(
        applicationType,
        fieldWithOperatorAndLicencesJson,
        OrganisationUnitTestUtil.orgUnit1Json,
        USER_DETAIL
    );

    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    return applicationVersion;
  }

  private ApplicationVersion createNewApplicationVersionForTerminalJson(ApplicationType applicationType,
                                                                        ConsentLengthForm consentLengthForm,
                                                                        TerminalWithOperatorJson terminalWithOperatorJson) {
    var applicationVersion = applicationService.createNewApplicationForTerminal(
        applicationType,
        terminalWithOperatorJson,
        OrganisationUnitTestUtil.orgUnit1Json,
        USER_DETAIL
    );

    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    return applicationVersion;
  }

  private ApplicationVersion createApplicationVersionWithOpenUpdateRequest(ApplicationType applicationType, ConsentLengthForm consentLengthForm, Instant deadlineInstant) {
    var applicationVersion = createAwaitingForPaymentApplicationVersion(applicationType, consentLengthForm);
    applicationService.submitApplication(applicationVersion, USER_DETAIL);

    applicationUpdateService.saveApplicationUpdateRequest(
        applicationVersion,
        deadlineInstant,
        "update request text",
        CASE_OFFICER_DETAIL
    );

    return applicationVersion;
  }
  
  @SuppressWarnings("unchecked")
  private List<ApplicationDataItem> getWorkAreaItems(WorkAreaFilterForm workAreaForm, ServiceUserDetail userDetail) {
    var workAreaFilter = new WorkAreaFilter();
    workAreaFilter.update(workAreaForm);
    var modelAndView = workAreaController.getWorkArea(workAreaFilter, userDetail);
    assertThat(modelAndView.getModel()).containsKey(WorkAreaController.WORK_AREA_ITEMS);

    return (List<ApplicationDataItem>) modelAndView.getModel().get(WorkAreaController.WORK_AREA_ITEMS);
  }
}
