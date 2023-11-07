package uk.co.nstauthority.fieldconsents.integrationtest.search;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.ANNUAL_CONSENT_YEAR;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.REGULATOR_TEAM;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.USER_DETAIL;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.getSearchResultItemForFieldInProgressOfType;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.getSearchResultItemForTerminalInProgressOfType;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.getSearchResultItemInProgressOfTypeAndLength;
import static uk.co.nstauthority.fieldconsents.integrationtest.IntegrationTestUtil.getSearchResultItemProductionSubmittedOfConsentLength;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD2_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL2_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.search.SearchFilterService.TERMINAL_LOOKUP_PURPOSE;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.AdditionalAssetsService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalRepository;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthForm;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.search.SearchController;
import uk.co.nstauthority.fieldconsents.search.SearchFilterForm;
import uk.co.nstauthority.fieldconsents.search.SearchResultItem;
import uk.co.nstauthority.fieldconsents.search.SearchSession;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Transactional
@DirtiesContext(classMode = AFTER_CLASS)
public class SearchIntegrationTest extends AbstractIntegrationTest {

  @MockBean
  private TeamService teamService;

  @MockBean
  private OrganisationUnitService organisationUnitService;

  @MockBean
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  @MockBean
  private FieldService fieldService;

  @MockBean
  private TerminalService terminalService;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @Autowired
  private SearchController searchController;

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ConsentLengthService consentLengthService;

  @Autowired
  private ApplicationVersionService applicationVersionService;

  @Autowired
  private ApplicationWithdrawalService applicationWithdrawalService;

  @Autowired
  private ApplicationWithdrawalRepository applicationWithdrawalRepository;

  @Autowired
  private AdditionalAssetsService additionalAssetsService;

  @Autowired
  private Clock clock;

  private SearchFilterForm searchForm;
  private RestSearchItem assetFieldRestSearchItem;
  private RestSearchItem assetTerminalRestSearchItem;
  private RestSearchItem orgUnitRestSearchItem;
  private Map<WebUserAccountId, EnergyPortalUserDto> portalUsersDtoMap;
  private ZonedDateTime zonedDateTime;

  @BeforeEach
  void setUp() {
    searchForm = new SearchFilterForm();
    zonedDateTime = ZonedDateTime.now(clock.getZone());
    when(teamService.isRegulatorUser(USER_DETAIL)).thenReturn(true);
    when(organisationUnitService.getOrganisationUnitsByIds(
        ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(Collections.singletonList(
        OrganisationUnitTestUtil.orgUnit1Json));

    assetFieldRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    assetTerminalRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    orgUnitRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(null)).thenReturn(assetFieldRestSearchItem);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER_DETAIL, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.singletonList(REGULATOR_TEAM));

    when(fieldService.findFieldsByIds(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(List.of(
        field1JsonWithOperatorAndLicences));

    portalUsersDtoMap = Map.of(WebUserAccountId.from(USER_DETAIL), ENERGY_PORTAL_USER_DTO);
    when(energyPortalUserService.getEnergyPortalUserMap(ArgumentMatchers.anyList())).thenReturn(portalUsersDtoMap);
  }

  // TODO: What do we want to test:
  //      1.  Test that a blank search returns all applications                                                                                       DONE!
  //      2.  Test that if I use the REFERENCE NUMBER filter the search only returns the application with the searched case ref number                DONE!
  //      3.  Test that if I use the STATUS filter the search only returns the application that are in the searched status                            FAIL FOR CERTAIN STATUSES! - FCS-510
  //      4.  Test that if I use the APPLICATION TYPE filter the search only returns the application with the searched type                           DONE!
  //      5.  Test that if I use the DURATION filter the search only returns the application with the searched consent duration type                  DONE!
  //      6.  Test that if I use the PRIMARY OPERATOR filter the search only returns the application with the searched operator                       DONE!
  //      7.  Test that if I use the SUBMISSION YEAR filter the search only returns the application submitted in the searched year                    DONE!
  //      8.  Test that if I use the CONSENT START YEAR filter the search only returns the application for consent starting on the searched year      DONE!
  //      9.  Test that if I use the ACE STATUS filter the search only returns the application with the ACE status specified                          DONE!
  //     10.  Test that if I use the ASSET TYPE filter the search only returns returns the application with the searched asset type
  //     11.  Test that if I use the FIELD filter the search only returns the application with the searched field                                     DONE!
  //     12.  Test that if I use the FACILITY filter the search only returns the application with the searched facility                               DONE!
  //     13.  Test that if I use the LICENCE REFERENCE filter the search only returns the application with the fields having the searched licence
  //     14.  Test priority order with multiple applications

  /*********************************** EMPTY SEARCH ***********************************/
// TODO FCS-510: Find out why "matchingPortalUserDto" is null
  //  @Test
//  void searchAllApplications_whenFound() {
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
//    );
//  }

  // TODO FCS-510: Find out why "matchingPortalUserDto" is null
//  @Test
//  void searchAllApplications_whenNoneFound() {
//    var searchResults = getSearchResultItems(searchForm);
//
//    Assertions.assertThat(searchResults).isEmpty();
//  }

  /*********************************** REFERENCE NUMBER ***********************************/
  @Test
  void searchByReferenceNumber_foundWhenValidApplicationNumber() {
    searchForm.setReferenceNumber("10");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void searchByReferenceNumber_notFoundWhenNotANumber() {
    searchForm.setReferenceNumber("abc");
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).isEmpty();
  }

  /*********************************** APPLICATION STATUS ***********************************/
  @Test
  void searchByStatus_foundWhenInProgress() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationId = createNewApplicationVersionForField(ApplicationType.FLARE, consentLengthForm).getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults)
        .containsExactly(getSearchResultItemForFieldInProgressOfType(applicationId, ApplicationType.FLARE));
  }

  @Test
  void searchByStatus_notFoundWhenInProgress() {
    searchForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS));
    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createSubmittedApplicationVersion(ApplicationType.FLARE, consentLengthForm);

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).isEmpty();
  }

  // TODO FCS-510: Find out why "matchingPortalUserDto" is null
//  @Test
//  void searchByStatus_foundWhenSubmitted() {
//    searchForm.setStatuses(List.of(ApplicationVersionStatus.SUBMITTED));
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
//    );
//  }

  // TODO FCS-510: the following need further investigation as they are not working yet.
//  @Test
//  void searchByStatus_foundWhenWithdrawn() {
//    searchForm.setStatuses(List.of(ApplicationVersionStatus.WITHDRAWN));
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
//    applicationVersion.setStatus(ApplicationVersionStatus.WITHDRAWN);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
//    );
//  }

//  @Test
//  void searchByStatus_foundWhenAwaitingForPayment() {
//    searchForm.setStatuses(List.of(ApplicationVersionStatus.AWAITING_PAYMENT));
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    var applicationVersion = createAwaitingForPaymentApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
//    );
//  }
//
//    @Test
//  void searchByStatus_foundWhenCompleted() {
//    searchForm.setStatuses(List.of(ApplicationVersionStatus.COMPLETED));
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    var applicationVersion = createWithdrawnApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
//    );
//  }

  /*********************************** APPLICATION TYPE ***********************************/
  // TODO FCS-510: Find out why "matchingPortalUserDto" is null
//  @ParameterizedTest
//  @EnumSource(ApplicationType.class)
//  void searchByApplicationType_whenFound(ApplicationType applicationType) {
//    searchForm.setApplicationTypes(List.of(applicationType));
//
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemForFieldInProgressOfType(applicationId, applicationType)
//    );
//  }

  // TODO FCS-510: Find out why "matchingPortalUserDto" is null
//  @ParameterizedTest
//  @EnumSource(ApplicationType.class)
//  void searchByApplicationType_whenFoundWithMultipleTypes(ApplicationType applicationType) {
//    searchForm.setApplicationTypes(Arrays.asList(ApplicationType.values()));
//
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    var applicationVersion = createNewApplicationVersionForField(applicationType, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemForFieldInProgressOfType(applicationId, applicationType)
//    );
//  }

  // TODO FCS-510: Find out why "matchingPortalUserDto" is null
//  @ParameterizedTest
//  @EnumSource(value = ApplicationType.class, names = "VENT", mode = EnumSource.Mode.EXCLUDE)
//  void searchByApplicationType_whenNotFound(ApplicationType applicationType) {
//    searchForm.setApplicationTypes(List.of(applicationType));
//
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    createNewApplicationVersionForField(ApplicationType.VENT, consentLengthForm);
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).isEmpty();
//  }

  /*********************************** CONSENT LENGTH TYPE ***********************************/
  @Test
  void searchByConsentDuration_whenFoundWithAnnualTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.ANNUAL)
    );
  }

  @Test
  void searchByConsentDuration_whenFoundWithShortTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.SHORT_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void searchByConsentDuration_whenLongTermAndFoundWithMultipleDurationTypes() {
    searchForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.LONG_TERM)
    );
  }

  @Test
  void searchByConsentDuration_whenAnnualAndFoundWithMultipleDurationTypes() {
    searchForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.ANNUAL)
    );
  }

  @Test
  void searchByConsentDuration_whenShortTermAndFoundWithMultipleDurationTypes() {
    searchForm.setDurationTypes(Arrays.asList(ConsentLengthType.values()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void searchByConsentDuration_whenFoundWithLongTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.LONG_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.LONG_TERM)
    );
  }

  @Test
  void searchByConsentDuration_whenNotFoundWithAnnualTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByConsentDuration_whenNotFoundWithShortTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.SHORT_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByConsentDuration_whenNotFoundWithLongTerm() {
    searchForm.setDurationTypes(List.of(ConsentLengthType.LONG_TERM));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  /*********************************** PRIMARY OPERATOR ***********************************/
  // TODO FCS-510: Find out why "matchingPortalUserDto" is null
//  @Test
//  void searchByPrimaryOperator_whenFound() {
//    orgUnitRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_REST_SEARCH_ITEM;
//    when(applicationDataFilterFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);
//
//    searchForm.setOperatorId(Integer.parseInt(orgUnitRestSearchItem.id()));
//
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//
//    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION)
//    );
//  }

  @Test
  void searchByPrimaryOperator_whenNotFound() {
    searchForm.setOperatorId(2);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).isEmpty();
  }

  /*********************************** SUBMISSION YEAR ***********************************/
  @Test
  void searchBySubmissionYear_whenFound() {
    searchForm.setSubmittedYear(String.valueOf(zonedDateTime.getYear()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    var applicationVersion = createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemProductionSubmittedOfConsentLength(applicationId, clock.instant(), ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void searchBySubmissionYear_whenNotFound() {
    searchForm.setSubmittedYear(String.valueOf(zonedDateTime.getYear() - 1));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);

    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).isEmpty();
  }

  /*********************************** CONSENT START YEAR ***********************************/
  @Test
  void searchByConsentStartYear_whenFoundWithAnnual() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear()));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.ANNUAL)
    );
  }

  @Test
  void searchByConsentStartYear_whenFoundWithShortTerm() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear()));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.SHORT_TERM)
    );
  }

  @Test
  void searchByConsentStartYear_whenFoundWithLongTerm() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear()));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemInProgressOfTypeAndLength(applicationId, ApplicationType.PRODUCTION, ConsentLengthType.LONG_TERM)
    );
  }

  @Test
  void searchByConsentStartYear_whenNotFoundWithAnnual() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear() - 1));

    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(ANNUAL_CONSENT_YEAR);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByConsentStartYear_whenNotFoundWithShortTerm() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear() - 1));

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByConsentStartYear_whenNotFoundWithLongTerm() {
    searchForm.setConsentStartYear(String.valueOf(zonedDateTime.getYear() - 1));

    var consentLengthForm = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
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

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).isEmpty();
  }

  /************************************** ACE STATUS **************************************/
  @ParameterizedTest
  @MethodSource("aceStatusFlagsConsentYearsWhenFound")
  void searchByAceStatus_whenFound(AceFlagStatus aceFlagStatus, int annualConsentYear) {
    searchForm.setAceFlagStatuses(List.of(aceFlagStatus));
    var consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(annualConsentYear);

    createSubmittedApplicationVersion(ApplicationType.PRODUCTION, consentLengthForm);

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).isNotEmpty();

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

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).isEmpty();

  }

  private static Stream<Arguments> aceStatusFlagsConsentYearsWhenNotFound() {
    return Stream.of(
        arguments(AceFlagStatus.ACE, ANNUAL_CONSENT_YEAR),
        arguments(AceFlagStatus.NON_ACE, ANNUAL_CONSENT_YEAR + 1)
    );
  }

  /************************************** ASSET FIELDS **************************************/
  // TODO FCS-510: Find out why "matchingPortalUserDto" is null
//  @Test
//  void searchByField_whenPrimaryAndFound() {
//    searchForm.setFieldAssetKey(FIELD1_ASSET_KEY);
//    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD1_REST_SEARCH_ITEM;
//    when(applicationDataFilterFormService.getPrefilledAsset(FIELD1_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
//    when(fieldService.getField(AssetKey.from(FIELD1_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field1JsonWithOperatorAndLicences);
//
//    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
//    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
//    var applicationId = applicationVersion.getApplication().getId();
//
//    var searchResults = getSearchResultItems(searchForm);
//    Assertions.assertThat(searchResults).containsExactly(
//        getSearchResultItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION)
//    );
//  }

  @Test
  void searchByField_whenSecondaryAndFound() {
    searchForm.setFieldAssetKey(FIELD2_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD2_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    when(fieldService.getField(AssetKey.from(FIELD2_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field2JsonWithOperatorAndLicences);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    additionalAssetsService.saveAdditionalAsset(applicationVersion, field2JsonWithOperatorAndLicences);

    var applicationId = applicationVersion.getApplication().getId();

    var searchResults = getSearchResultItems(searchForm);
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemForFieldInProgressOfType(applicationId, ApplicationType.PRODUCTION)
    );
  }

  @Test
  void searchByField_whenPrimaryAndNotFound() {
    searchForm.setFieldAssetKey(FIELD2_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD2_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    when(fieldService.getField(AssetKey.from(FIELD2_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field2JsonWithOperatorAndLicences);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
  }

  @Test
  void searchByField_whenSecondaryAndNotFound() {
    searchForm.setFieldAssetKey(FIELD2_ASSET_KEY);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD2_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD2_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    when(fieldService.getField(AssetKey.from(FIELD2_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field2JsonWithOperatorAndLicences);

    var consentLengthForm = ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var applicationVersion = createNewApplicationVersionForField(ApplicationType.PRODUCTION, consentLengthForm);
    additionalAssetsService.saveAdditionalAsset(applicationVersion, field3JsonWithOperatorAndLicences);

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
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
    Assertions.assertThat(searchResults).containsExactly(
        getSearchResultItemForTerminalInProgressOfType(applicationId, ApplicationType.PRODUCTION)
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

    Assertions.assertThat(getSearchResultItems(searchForm)).isEmpty();
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
    applicationService.submitApplication(applicationVersion, USER_DETAIL);

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
//    applicationWithdrawalService.saveWithdrawalResponse(
//        applicationVersion,
//        WithdrawalStatus.ACCEPTED,
//        "Test response",
//        USER_DETAIL
//    );
    applicationVersionService.withdrawApplicationVersion(applicationVersion);
    return applicationVersion;
  }

  @SuppressWarnings("unchecked")
  private List<SearchResultItem> getSearchResultItems(SearchFilterForm searchForm) {
    var searchSession = new SearchSession(searchForm);
    searchSession.update(searchForm);
    var modelAndView = searchController.getSearch(searchSession, USER_DETAIL);
    Assertions.assertThat(modelAndView.getModel()).containsKey(SearchController.SEARCH_RESULT_ITEMS);

    return (List<SearchResultItem>) modelAndView.getModel().get(SearchController.SEARCH_RESULT_ITEMS);
  }
}
