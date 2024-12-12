package uk.co.nstauthority.fieldconsents.integrationtest.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.ANNUAL_CONSENT_YEAR;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.CASE_MANAGER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.CASE_OFFICER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.PORTAL_USERS_DTO_MAP;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.REGULATOR_TEAM;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.USER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.getCompleteApplicationDataItemForSearchBuilder;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.getConsentDurationString;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD2_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL2_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.TERMINAL_LOOKUP_PURPOSE;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.AdditionalAssetsService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataRepository;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthForm;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.search.SearchController;
import uk.co.nstauthority.fieldconsents.search.SearchFilterForm;
import uk.co.nstauthority.fieldconsents.search.SearchFilterFormService;
import uk.co.nstauthority.fieldconsents.search.SearchSession;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

class SearchIntegrationTest extends AbstractIntegrationTest {

  @MockBean
  private OrganisationUnitService organisationUnitService;

  @MockBean
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  @MockBean
  private FieldService fieldService;

  @MockBean
  private TerminalService terminalService;

  @MockBean
  private TeamQueryService teamQueryService;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @MockBean
  private SearchFilterFormService searchFilterFormService;

  @MockBean
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Autowired
  private SearchController searchController;

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ApplicationSubmissionService applicationSubmissionService;

  @Autowired
  private ConsentLengthService consentLengthService;

  @Autowired
  private ApplicationVersionService applicationVersionService;

  @Autowired
  private ApplicationWithdrawalService applicationWithdrawalService;

  @Autowired
  private CaseAssignmentService caseAssignmentService;

  @Autowired
  private AdditionalAssetsService additionalAssetsService;

  @Autowired
  private ConsentDataRepository consentDataRepository;

  @Autowired
  private Clock clock;

  private SearchFilterForm searchForm;
  private RestSearchItem assetFieldRestSearchItem;
  private RestSearchItem assetTerminalRestSearchItem;
  private RestSearchItem orgUnitRestSearchItem;
  private RestSearchItem orgUnitGroupRestSearchItem;
  private ZonedDateTime zonedDateTime;

  @BeforeEach
  void setUp() {
    truncateApplicationsCascade();

    searchForm = new SearchFilterForm();
    zonedDateTime = ZonedDateTime.now(clock.getZone());

    when(teamQueryService.userIsMemberOfTeamType(USER_DETAIL, TeamType.REGULATOR))
        .thenReturn(true);
    when(teamQueryService.getTeamRoles(USER_DETAIL)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build()
    ));
    when(teamQueryService.userHasAtLeastOneStaticRole(USER_DETAIL, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);

    when(organisationUnitService.getOrganisationUnitsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString()))
        .thenReturn(Collections.singletonList(OrganisationUnitTestUtil.orgUnit1Json));

    assetFieldRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    assetTerminalRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    orgUnitRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    orgUnitGroupRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(null))
        .thenReturn(assetFieldRestSearchItem);

    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString()))
        .thenReturn(List.of(field1JsonWithOperatorAndLicences));

    when(energyPortalUserService.getEnergyPortalUserMap(ArgumentMatchers.anyList()))
        .thenReturn(PORTAL_USERS_DTO_MAP);
  }

  /*********************************** EMPTY SEARCH ***********************************/
  @Test
  void searchAllApplications_whenFound() {
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);

    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .build()
    );
  }

  @Test
  void searchAllApplications_whenNoneFound() {
    var searchResults = getSearchResultItems(searchForm);

    assertThat(searchResults).isEmpty();
  }

  /*********************************** REFERENCE NUMBER ***********************************/
  @Test
  void searchByReferenceNumber_foundWhenValidApplicationNumber() {
    searchForm.setReferenceNumber("8000");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .build()
    );
  }

  @Test
  void searchByReferenceNumber_notFoundWhenNotANumber() {
    searchForm.setReferenceNumber("abc");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /*********************************** APPLICATION STATUS ***********************************/
  @Test
  void searchByStatus_foundWhenInProgress() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationId = createNewApplicationVersionForField(ApplicationType.FLARE, consentLengthForm).getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults)
        .containsExactly(
            getCompleteApplicationDataItemForSearchBuilder()
                .withApplicationId(applicationId)
                .withType(ApplicationType.FLARE.getDisplayName())

                .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
                .withAsset(field1JsonWithOperatorAndLicences.getName())
                .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
                .build()
        );
  }

  @Test
  void searchByStatus_notFoundWhenInProgress() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByStatus_foundWhenSubmitted() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.SUBMITTED));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .build()
    );
  }

  @Test
  void searchByStatus_foundWhenWithdrawn() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.WITHDRAWN));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createWithdrawnApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    applicationVersion.setStatus(ApplicationVersionStatus.WITHDRAWN);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withStatus(ApplicationVersionStatus.WITHDRAWN.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .build()
    );
  }

  @Test
  void searchByStatus_foundWhenAwaitingForPayment() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.AWAITING_PAYMENT));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    when(fieldService.getField(AssetKey.from(FIELD1_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field1JsonWithOperatorAndLicences);

    var applicationVersion = createAwaitingForPaymentApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.AWAITING_PAYMENT.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByStatus_foundWhenConsented() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.CONSENTED));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    when(teamQueryService.userHasStaticRole(CASE_OFFICER_DETAIL, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);
    var consentData = new ConsentData(1);
    consentData.setConsentStartDate(zonedDateTime.toLocalDate());
    consentData.setConsentEndDate(zonedDateTime.toLocalDate().plusYears(1));

    var applicationVersion = createConsentedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm, consentData);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.CONSENTED.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .withCaseOfficer("%s %s".formatted(CASE_OFFICER_DETAIL.forename(), CASE_OFFICER_DETAIL.surname()))
            .build()
    );
  }

  /*********************************** APPLICATION TYPE ***********************************/
  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void searchByApplicationType_whenFound(ApplicationType applicationType) {
    searchForm.setApplicationTypes(List.of(applicationType));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withType(applicationType.getDisplayName())
            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void searchByApplicationType_whenFoundWithMultipleTypes(ApplicationType applicationType) {
    searchForm.setApplicationTypes(Arrays.asList(ApplicationType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withType(applicationType.getDisplayName())
            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "VENT", mode = EnumSource.Mode.EXCLUDE)
  void searchByApplicationType_whenNotFound(ApplicationType applicationType) {
    searchForm.setApplicationTypes(List.of(applicationType));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(ApplicationType.VENT, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();

    applicationVersionService.deleteApplicationVersion(applicationVersion);
  }

  /*********************************** CONSENT LENGTH TYPE ***********************************/
  @Test
  void searchByConsentDuration_whenFoundWithAnnualTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withDuration(getConsentDurationString(ConsentLengthType.ANNUAL))
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByConsentDuration_whenFoundWithShortTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.SHORT_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withDuration(getConsentDurationString(ConsentLengthType.SHORT_TERM))
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByConsentDuration_whenLongTermAndFoundWithMultipleDurationTypes() {
    searchForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withDuration(getConsentDurationString(ConsentLengthType.LONG_TERM))
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByConsentDuration_whenAnnualAndFoundWithMultipleDurationTypes() {
    searchForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withDuration(getConsentDurationString(ConsentLengthType.ANNUAL))
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByConsentDuration_whenShortTermAndFoundWithMultipleDurationTypes() {
    searchForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withDuration(getConsentDurationString(ConsentLengthType.SHORT_TERM))
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByConsentDuration_whenFoundWithLongTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.LONG_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withDuration(getConsentDurationString(ConsentLengthType.LONG_TERM))
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByConsentDuration_whenNotFoundWithAnnualTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByConsentDuration_whenNotFoundWithShortTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.SHORT_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByConsentDuration_whenNotFoundWithLongTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.LONG_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /*********************************** PRIMARY OPERATOR ***********************************/
  @Test
  void searchByPrimaryOperator_whenFound() {
    orgUnitRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);

    searchForm.setOperatorId(Integer.parseInt(orgUnitRestSearchItem.id()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)

            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByPrimaryOperator_whenNotFound() {
    searchForm.setOperatorId(2);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /******************************** PRIMARY OPERATOR GROUP ********************************/
  @Test
  void searchByPrimaryOperatorGroup_whenFound() {
    orgUnitGroupRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_GROUP_REST_SEARCH_ITEM;
    when(searchFilterFormService.getPrefilledOrganisationGroup(any())).thenReturn(orgUnitGroupRestSearchItem);
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(anyList()))
        .thenReturn(List.of(orgUnit1Json));
    searchForm.setOperatorGroupId(Integer.parseInt(orgUnitGroupRestSearchItem.id()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withOperator(CACHED_PRIMARY_OPERATOR_NAME_1)
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByPrimaryOperatorGroup_whenNotFound() {
    searchForm.setOperatorGroupId(2);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /*********************************** SUBMISSION YEAR ***********************************/
  @Test
  void searchBySubmissionYear_whenFound() {
    searchForm.setSubmittedYear(String.valueOf(zonedDateTime.getYear()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .build()
    );
  }

  @Test
  void searchBySubmissionYear_whenNotFound() {
    searchForm.setSubmittedYear(String.valueOf(zonedDateTime.getYear() - 1));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }


  /*********************************** CONSENT START/END YEAR ***********************************/
  @Test
  void searchByConsentStartYear_whenFound() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear()));
    when(teamQueryService.userHasStaticRole(CASE_OFFICER_DETAIL, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var consentData = new ConsentData(1);
    consentData.setConsentStartDate(zonedDateTime.toLocalDate());
    consentData.setConsentEndDate(zonedDateTime.toLocalDate().plusYears(1).minusDays(30));

    var applicationVersion = createSubmittedApplicationVersionWithConsentData(ApplicationType.PRODUCTION, consentLengthForm, consentData);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .withCaseOfficer("%s %s".formatted(CASE_OFFICER_DETAIL.forename(), CASE_OFFICER_DETAIL.surname()))
            .build()
    );
  }

  @Test
  void searchByConsentStartYear_whenNotFound() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear() - 1));
    when(teamQueryService.userHasStaticRole(CASE_OFFICER_DETAIL, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var consentData = new ConsentData(1);
    consentData.setConsentStartDate(zonedDateTime.toLocalDate());
    consentData.setConsentEndDate(zonedDateTime.toLocalDate().plusYears(1).minusDays(30));

    createSubmittedApplicationVersionWithConsentData(ApplicationType.PRODUCTION, consentLengthForm, consentData);

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).isEmpty();
  }

  @Test
  void searchByConsentEndYear_whenFound() {
    searchForm.setConsentEndYear(String.valueOf(zonedDateTime.getYear()));
    when(teamQueryService.userHasStaticRole(CASE_OFFICER_DETAIL, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var consentData = new ConsentData(1);
    consentData.setConsentStartDate(zonedDateTime.toLocalDate());
    consentData.setConsentEndDate(zonedDateTime.toLocalDate().minusDays(30));

    var applicationVersion = createSubmittedApplicationVersionWithConsentData(ApplicationType.PRODUCTION, consentLengthForm, consentData);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withReference(APPLICATION_REFERENCE)
            .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
            .withSubmittedDateTime(DateUtils.format(clock.instant(), DateUtils.DATE_TIME))
            .withSubmittedBy("%s %s".formatted(USER_DETAIL.forename(), USER_DETAIL.surname()))
            .withAceFlag(false)
            .withCaseOfficer("%s %s".formatted(CASE_OFFICER_DETAIL.forename(), CASE_OFFICER_DETAIL.surname()))
            .build()
    );
  }

  @Test
  void searchByConsentEndYear_whenNotFound() {
    searchForm.setConsentEndYear(String.valueOf(zonedDateTime.getYear()));
    when(teamQueryService.userHasStaticRole(CASE_OFFICER_DETAIL, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var consentData = new ConsentData(1);
    consentData.setConsentStartDate(zonedDateTime.toLocalDate());
    consentData.setConsentEndDate(zonedDateTime.toLocalDate().plusYears(1).minusDays(30));

    createSubmittedApplicationVersionWithConsentData(ApplicationType.PRODUCTION, consentLengthForm, consentData);

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(ConsentLengthType.class)
  void searchByConsentStartYear_whenNotFound(ConsentLengthType consentLengthType) {
    searchForm.setSubmittedYear(String.valueOf(zonedDateTime.getYear() - 1));

    var consentLengthForm =
        switch (consentLengthType) {
          case ANNUAL -> ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
          case LONG_TERM -> ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
          case SHORT_TERM -> ConsentLengthTestUtil.getLongTermConsentLengthForm();
        };

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /************************************** ACE STATUS **************************************/
  @ParameterizedTest
  @MethodSource("aceStatusFlagsConsentYearsWhenFound")
  void searchByAceStatus_whenFound(AceFlagStatus aceFlagStatus, int annualConsentYear) {
    searchForm.setAceFlagStatuses(List.of(aceFlagStatus));
    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(annualConsentYear);

    createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).isNotEmpty();

  }

  private static Stream<Arguments> aceStatusFlagsConsentYearsWhenFound() {
    return Stream.of(
        arguments(AceFlagStatus.ACE, ANNUAL_CONSENT_YEAR + 1),
        arguments(AceFlagStatus.NON_ACE, ANNUAL_CONSENT_YEAR)
    );
  }

  @ParameterizedTest()
  @MethodSource("aceStatusFlagsConsentYearsWhenNotFound")
  void searchByAceStatus_whenNotFound(AceFlagStatus aceFlagStatus, int annualConsentYear) {
    searchForm.setAceFlagStatuses(List.of(aceFlagStatus));
    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(annualConsentYear);

    createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  private static Stream<Arguments> aceStatusFlagsConsentYearsWhenNotFound() {
    return Stream.of(
        arguments(AceFlagStatus.ACE, ANNUAL_CONSENT_YEAR),
        arguments(AceFlagStatus.NON_ACE, ANNUAL_CONSENT_YEAR + 1)
    );
  }

  /************************************** ASSET FIELDS **************************************/
  @Test
  void searchByField_whenFoundWithPrimaryField() {
    searchForm.setFieldAssetKey(FIELD1_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD1_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD1_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    when(fieldService.getField(AssetKey.from(FIELD1_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field1JsonWithOperatorAndLicences);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)

            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByField_whenFoundWithSecondaryField() {
    searchForm.setFieldAssetKey(FIELD2_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD2_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    when(fieldService.getField(AssetKey.from(FIELD2_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field2JsonWithOperatorAndLicences);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    additionalAssetsService.saveAdditionalAsset(applicationVersion, field2JsonWithOperatorAndLicences);

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)

            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByField_whenNotFoundWithPrimaryField() {
    searchForm.setFieldAssetKey(FIELD2_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD2_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    when(fieldService.getField(AssetKey.from(FIELD2_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field2JsonWithOperatorAndLicences);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByField_whenNotFoundWithSecondaryField() {
    searchForm.setFieldAssetKey(FIELD2_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD2_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    when(fieldService.getField(AssetKey.from(FIELD2_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field2JsonWithOperatorAndLicences);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    additionalAssetsService.saveAdditionalAsset(applicationVersion, field3JsonWithOperatorAndLicences);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /************************************** ASSET FACILITIES **************************************/
  @Test
  void searchByFacility_whenFound() {
    searchForm.setTerminalAssetKey(TERMINAL1_ASSET_KEY);
    assetTerminalRestSearchItem = ApplicationDataFilterFormTestUtil.TERMINAL1_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(TERMINAL1_ASSET_KEY)).thenReturn(assetTerminalRestSearchItem);
    when(terminalService.getTerminal(AssetKey.from(TERMINAL1_ASSET_KEY).assetId(), TERMINAL_LOOKUP_PURPOSE)).thenReturn(terminal1JsonWithOperator);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForTerminal(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(terminal1JsonWithOperator.getName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .withGeographicArea("")
            .withLicences("")
            .build()
    );
  }

  @Test
  void searchByFacility_whenNotFound() {
    searchForm.setTerminalAssetKey(TERMINAL2_ASSET_KEY);
    assetTerminalRestSearchItem = ApplicationDataFilterFormTestUtil.TERMINAL2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(TERMINAL2_ASSET_KEY)).thenReturn(assetTerminalRestSearchItem);
    when(terminalService.getTerminal(AssetKey.from(TERMINAL2_ASSET_KEY).assetId(), TERMINAL_LOOKUP_PURPOSE)).thenReturn(terminal2JsonWithOperator);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForTerminal(ApplicationType.PRODUCTION, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /************************************** ASSET TYPES **************************************/
  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToFieldJsonsWhenFound")
  void searchByAssetType_whenFoundWithPrimaryFieldAssetType(AssetTypeWithShore assetTypeWithShore,
                                                            FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    searchForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(fieldWithOperatorAndLicencesJson.getName())
            .withGeographicArea(fieldWithOperatorAndLicencesJson.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .withLicences(fieldWithOperatorAndLicencesJson.getLicencesAsString())
            .build()
    );
  }

  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToPrimaryAndSecondaryFieldJsonsWhenFound")
  void searchByAssetType_whenFoundWithSecondaryFieldAssetType(AssetTypeWithShore assetTypeWithShore,
                                                              FieldWithOperatorAndLicencesJson primaryFieldWithOperatorAndLicencesJson,
                                                              FieldWithOperatorAndLicencesJson secondaryFieldWithOperatorAndLicencesJson) {
    searchForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString()))
        .thenReturn(List.of(primaryFieldWithOperatorAndLicencesJson, secondaryFieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, primaryFieldWithOperatorAndLicencesJson);
    additionalAssetsService.saveAdditionalAsset(applicationVersion, secondaryFieldWithOperatorAndLicencesJson);

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(primaryFieldWithOperatorAndLicencesJson.getName())
            .withGeographicArea(primaryFieldWithOperatorAndLicencesJson.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .withLicences(primaryFieldWithOperatorAndLicencesJson.getLicencesAsString())
            .build()
    );
  }

  @ParameterizedTest
  @MethodSource("fieldJsonsWithMultipleAssetTypesFound")
  void searchByAssetType_whenFoundWithFieldAndMultipleAssetTypes(FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    searchForm.setAssetTypesWithShore(Arrays.asList(AssetTypeWithShore.values()));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(fieldWithOperatorAndLicencesJson.getName())
            .withGeographicArea(fieldWithOperatorAndLicencesJson.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .withLicences(fieldWithOperatorAndLicencesJson.getLicencesAsString())
            .build()
    );
  }

  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToFieldJsonsWhenNotFound")
  void searchByAssetType_whenNotFoundWithFieldAssetType(AssetTypeWithShore assetTypeWithShore,
                                                        FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    searchForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, fieldWithOperatorAndLicencesJson);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByAssetType_whenFoundWithFacilityAssetType() {
    searchForm.setAssetTypesWithShore(List.of(AssetTypeWithShore.TERMINAL));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForTerminalJson(ApplicationType.PRODUCTION, consentLengthForm, terminal1JsonWithOperator);

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(terminal1JsonWithOperator.getName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .withGeographicArea("")
            .withLicences("")
            .build()
    );
  }

  @ParameterizedTest
  @MethodSource("assetTypesWithShoreToFieldJsonsWhenFound")
  void searchByAssetType_whenNotFoundWithFacilityAssetType(AssetTypeWithShore assetTypeWithShore,
                                                           FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson) {
    searchForm.setAssetTypesWithShore(List.of(assetTypeWithShore));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        fieldWithOperatorAndLicencesJson));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForTerminalJson(ApplicationType.PRODUCTION, consentLengthForm, terminal1JsonWithOperator);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByAssetType_whenFoundWithFacilityAndMultipleAssetTypes() {
    searchForm.setAssetTypesWithShore(Arrays.asList(AssetTypeWithShore.values()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForTerminalJson(ApplicationType.PRODUCTION, consentLengthForm, terminal1JsonWithOperator);

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(terminal1JsonWithOperator.getName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .withGeographicArea("")
            .withLicences("")
            .build()
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

  /************************************** LICENCES **************************************/
  @Test
  void searchByLicenceReference_whenFoundOnPrimaryField() {
    searchForm.setLicenceReference("P1");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, field1JsonWithOperatorAndLicences);
    when(fieldService.findFieldsWithOperatorAndLicences(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        field1JsonWithOperatorAndLicences));

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(field1JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field1JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .build()
    );
  }

  @Test
  void searchByLicenceReference_whenFoundOnSecondaryField() {
    searchForm.setLicenceReference("P3");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, field2JsonWithOperatorAndLicences);
    additionalAssetsService.saveAdditionalAsset(applicationVersion, field1JsonWithOperatorAndLicences);
    when(fieldService.findFieldsWithOperatorAndLicences(ArgumentMatchers.anyList(), ArgumentMatchers.anyString()))
        .thenReturn(List.of(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences));
    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        field2JsonWithOperatorAndLicences));

    var applicationId = applicationVersion.getApplication().getId();
    var searchResults = getSearchResultItems(searchForm);
    assertThat(searchResults).containsExactly(
        getCompleteApplicationDataItemForSearchBuilder()
            .withApplicationId(applicationId)
            .withAsset(field2JsonWithOperatorAndLicences.getName())
            .withGeographicArea(field2JsonWithOperatorAndLicences.getGeographicArea().getDisplayName())
            .withStatus(ApplicationVersionStatus.IN_PROGRESS.getDisplayName())
            .withLicences(field2JsonWithOperatorAndLicences.getLicencesAsString())
            .build()
    );
  }

  @Test
  void searchByLicenceReference_whenNotFound() {
    searchForm.setLicenceReference("P3");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForFieldJson(ApplicationType.PRODUCTION, consentLengthForm, field2JsonWithOperatorAndLicences);
    when(fieldService.findFieldsWithOperatorAndLicences(ArgumentMatchers.anyList(), ArgumentMatchers.anyString()))
        .thenReturn(List.of(field2JsonWithOperatorAndLicences));

    assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByLicenceReference_whenNotFoundWithInvalidReference() {
    searchForm.setLicenceReference("abc");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    assertThat(getSearchResultItems(searchForm)).isEmpty();
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
    applicationSubmissionService.submitApplication(applicationVersion, USER_DETAIL);

    return applicationVersion;
  }

  private ApplicationVersion createSubmittedApplicationVersionWithConsentData(ApplicationType applicationType,
                                                                              ConsentLengthForm consentLengthForm,
                                                                              ConsentData consentData) {
    var applicationVersion = createSubmittedApplicationVersion(applicationType, consentLengthForm);
    caseAssignmentService.assignCaseOfficer(applicationVersion, CASE_OFFICER_DETAIL, CASE_MANAGER_DETAIL);

    consentData.setApplication(applicationVersion.getApplication());
    consentDataRepository.save(consentData);

    return applicationVersion;
  }

  private ApplicationVersion createConsentedApplicationVersion(ApplicationType applicationType,
                                                               ConsentLengthForm consentLengthForm,
                                                               ConsentData consentData) {
    var applicationVersion = createSubmittedApplicationVersionWithConsentData(applicationType, consentLengthForm, consentData);

    applicationService.consentApplication(applicationVersion);

    return applicationVersion;
  }

  private ApplicationVersion createAwaitingForPaymentApplicationVersion(ApplicationType applicationType, ConsentLengthForm consentLengthForm) {
    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);

    applicationService.prepareApplicationForPayment(applicationVersion);
    return applicationVersion;
  }

  private ApplicationVersion createWithdrawnApplicationVersion(ApplicationType applicationType, ConsentLengthForm consentLengthForm) {
    var applicationVersion = createSubmittedApplicationVersion(applicationType, consentLengthForm);

    applicationWithdrawalService.saveWithdrawalRequest(
        applicationVersion,
        "Test request",
        USER_DETAIL
    );
    applicationWithdrawalService.saveWithdrawalResponse(
        applicationVersion,
        WithdrawalStatus.ACCEPTED,
        "Test response",
        USER_DETAIL
    );
    return applicationVersion;
  }

  @SuppressWarnings("unchecked")
  private List<ApplicationDataItemView> getSearchResultItems(SearchFilterForm searchForm) {
    var searchSession = new SearchSession(searchForm);
    searchSession.update(searchForm);
    var modelAndView = searchController.getSearch(searchSession, USER_DETAIL);
    assertThat(modelAndView.getModel()).containsKey(SearchController.SEARCH_RESULT_ITEMS);

    return (List<ApplicationDataItemView>) modelAndView.getModel().get(SearchController.SEARCH_RESULT_ITEMS);
  }
}
