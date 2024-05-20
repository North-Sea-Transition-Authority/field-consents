package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ConsentRepository consentRepository;

  @Mock
  private ConsentDataService consentDataService;

  @Mock
  private ConsentLengthService consentLengthService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ConsentService consentService;

  private Application application;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void beforeEach() {
    consentService = spy(new ConsentService(
        applicationAssetService,
        consentRepository,
        consentDataService,
        consentLengthService,
        clock
    ));

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();
  }

  @Test
  void createConsent() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consent = consentService.createConsent(application, user);

    assertThat(consent)
        .isNotNull()
        .extracting(
            Consent::getApplication,
            Consent::getIssuedByWuaId,
            Consent::getIssuedInstant
        ).containsExactly(
            application,
            user.wuaId(),
            clock.instant()
        );

    verify(consentRepository).save(consent);
  }

  @Test
  void shouldCheckProductionConsentExists_production() {
    var applicationVersion = new ApplicationVersion();

    var application = new Application();
    application.setType(ApplicationType.PRODUCTION);

    applicationVersion.setApplication(application);

    assertThat(consentService.shouldCheckProductionConsentExists(applicationVersion)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class)
  void shouldCheckProductionConsentExists_flare(ApplicationVersionStatus applicationVersionStatus) {
    var applicationVersion = new ApplicationVersion();

    var application = new Application();
    application.setType(ApplicationType.FLARE);

    applicationVersion.setApplication(application);
    applicationVersion.setStatus(applicationVersionStatus);

    var shouldCheckProductionConsentExists = switch (applicationVersionStatus) {
      case IN_PROGRESS, AWAITING_PAYMENT, SUBMITTED -> true;
      default -> false;
    };

    assertThat(consentService.shouldCheckProductionConsentExists(applicationVersion)).isEqualTo(shouldCheckProductionConsentExists);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class)
  void shouldCheckProductionConsentExists_vent(ApplicationVersionStatus applicationVersionStatus) {
    var applicationVersion = new ApplicationVersion();

    var application = new Application();
    application.setType(ApplicationType.VENT);

    applicationVersion.setApplication(application);
    applicationVersion.setStatus(applicationVersionStatus);

    var shouldCheckProductionConsentExists = switch (applicationVersionStatus) {
      case IN_PROGRESS, AWAITING_PAYMENT, SUBMITTED -> true;
      default -> false;
    };

    assertThat(consentService.shouldCheckProductionConsentExists(applicationVersion)).isEqualTo(shouldCheckProductionConsentExists);
  }

  @Test
  void checkProductionConsentExistsForInProgressApplication_noConsentLengthDetailsExist() {
    var applicationVersion = new ApplicationVersion();
    var application = new Application();
    application.setType(ApplicationType.FLARE);
    applicationVersion.setApplication(application);

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.empty());

    assertThat(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion))
        .isEqualTo(ProductionConsentCheckResult.CONSENT_DETAILS_DO_NOT_EXIST);
  }

  @ParameterizedTest
  @MethodSource("checkProductionConsentExistsForInProgressApplication_arguments")
  void checkProductionConsentExistsForInProgressApplication(
      LocalDate proposedStartDate,
      LocalDate proposedEndDate,
      Set<Integer> fieldIds,
      Map<Integer, List<ConsentData>> consentDataListByFieldId,
      ProductionConsentCheckResult expectedResult
  ) {
    var applicationVersion = new ApplicationVersion();
    var application = new Application();
    application.setType(ApplicationType.FLARE);
    applicationVersion.setApplication(application);

    var fieldApplicationAssets = fieldIds.stream().map(fieldId -> ApplicationAssetTestUtil.newBuilder().withAssetId(fieldId).build()).toList();
    var consentLengthDetails = new ConsentLengthDetails();

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(applicationVersion, AssetType.FIELD, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY))).thenReturn(fieldApplicationAssets);
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedEndDate);
    when(consentDataService.getConsentDataListInRangeForConsentedProductionApplicationsByFieldId(proposedStartDate, proposedEndDate, fieldIds)).thenReturn(consentDataListByFieldId);

    assertThat(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion))
        .isEqualTo(expectedResult);
  }

  private static Stream<Arguments> checkProductionConsentExistsForInProgressApplication_arguments() {
    var proposedStartDate = LocalDate.now();
    var proposedEndDate = proposedStartDate.plusDays(10);

    return Stream.of(
        arguments(
            proposedStartDate,
            proposedEndDate,
            Set.of(1, 2, 3),
            Map.of(), // no consent data found for any of the fields
            ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT
        ),
        arguments(
            proposedStartDate,
            proposedEndDate,
            Set.of(1, 2, 3),
            Map.of(1, List.of()), // no consent data found for any of the fields
            ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT
        ),
        arguments(
            proposedStartDate,
            proposedEndDate,
            Set.of(1, 2, 3),
            Map.of(
                1, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusMonths(1))
                    .withConsentEndDate(proposedEndDate.plusMonths(1))
                    .build()),
                2, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusDays(1))
                    .withConsentEndDate(proposedEndDate.plusDays(1))
                    .build()),
                3, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusDays(1))
                    .withConsentEndDate(proposedEndDate.minusDays(1)) // this consent is a day short for field 3
                    .build())
            ),
            ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT
        ),
        arguments(
            proposedStartDate,
            proposedEndDate,
            Set.of(1, 2, 3),
            Map.of(
                1, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusMonths(1))
                    .withConsentEndDate(proposedEndDate.plusMonths(1))
                    .build()),
                2, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusDays(1))
                    .withConsentEndDate(proposedEndDate.plusDays(1))
                    .build())
                // field 3 is missing consent data
            ),
            ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT
        ),
        arguments(
            proposedStartDate,
            proposedEndDate,
            Set.of(1, 2, 3),
            Map.of(
                1, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusMonths(1))
                    .withConsentEndDate(proposedEndDate.plusMonths(1))
                    .build()),
                2, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusDays(1))
                    .withConsentEndDate(proposedEndDate.plusDays(1))
                    .build()),
                3, List.of(ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(proposedStartDate.minusDays(1))
                    .withConsentEndDate(proposedEndDate.plusDays(1))
                    .build())
            ),
            ProductionConsentCheckResult.WITHIN_ACTIVE_CONSENT
        )
    );
  }

  @ParameterizedTest
  @MethodSource("allDaysCoveredByProductionConsents_arguments")
  void allDaysCoveredByProductionConsents(
      LocalDate from,
      LocalDate to,
      List<ConsentData> consentDataList,
      boolean allDaysCoveredByProductionConsents
  ) {
    assertThat(consentService.allDaysCoveredByProductionConsents(consentDataList, from, to))
        .isEqualTo(allDaysCoveredByProductionConsents);
  }

  private static Stream<Arguments> allDaysCoveredByProductionConsents_arguments() {
    return Stream.of(
        arguments(
            // no consents exist
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(),
            false
        ),
        arguments(
            // active consents fall either side of the proposed consent
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2019, 1, 1))
                    .withConsentEndDate(LocalDate.of(2019, 12, 31))
                    .build(),
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2021, 1, 1))
                    .withConsentEndDate(LocalDate.of(2021, 12, 31))
                    .build()
            ),
            false
        ),
        arguments(
            // the second half of the proposed consent has no active consent
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2019, 6, 1))
                    .withConsentEndDate(LocalDate.of(2020, 6, 1))
                    .build()
            ),
            false
        ),
        arguments(
            // the first half of the proposed consent has no active consent
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2020, 6, 1))
                    .withConsentEndDate(LocalDate.of(2021, 6, 30))
                    .build()
            ),
            false
        ),
        arguments(
            // the first and last proposed months have no active consent
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2020, 2, 1))
                    .withConsentEndDate(LocalDate.of(2020, 11, 30))
                    .build()
            ),
            false
        ),
        arguments(
            // the proposed consent is fully within an active consent
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2019, 1, 1))
                    .withConsentEndDate(LocalDate.of(2021, 12, 31))
                    .build()
            ),
            true
        ),
        arguments(
            // two overlapping consents exist and cover the proposed consent
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2019, 6, 1))
                    .withConsentEndDate(LocalDate.of(2020, 6, 1))
                    .build(),
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2020, 6, 1))
                    .withConsentEndDate(LocalDate.of(2021, 6, 1))
                    .build()
            ),
            true
        ),
        arguments(
            // two overlapping active consents exist, but the second on ends before the first, still covers the proposed consent
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2019, 6, 1))
                    .withConsentEndDate(LocalDate.of(2020, 12, 31))
                    .build(),
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2020, 6, 1))
                    .withConsentEndDate(LocalDate.of(2020, 6, 30))
                    .build()
            ),
            true
        ),
        arguments(
            // active consents cover only the proposed start and end days, no in-between
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31),
            List.of(
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2019, 6, 1))
                    .withConsentEndDate(LocalDate.of(2020, 1, 1))
                    .build(),
                ConsentDataTestUtil.newBuilder()
                    .withConsentStartDate(LocalDate.of(2020, 12, 31))
                    .withConsentEndDate(LocalDate.of(2021, 2, 1))
                    .build()
            ),
            false
        )
    );
  }

  @Test
  void generateRange() {
    var today = LocalDate.now();
    var yesterday = today.minusDays(1);
    var tomorrow = today.plusDays(1);

    assertThat(consentService.generateRange(yesterday, tomorrow)).containsExactlyInAnyOrder(yesterday, today, tomorrow);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "CONSENTED", mode = EnumSource.Mode.EXCLUDE)
  void nonExpiredConsentExists_applicationVersionStatusIsNotConsented(ApplicationVersionStatus status) {
    applicationVersion.setStatus(status);

    assertThat(consentService.nonExpiredConsentExists(applicationVersion)).isFalse();
  }

  @Test
  void nonExpiredConsentExists_applicationVersionStatusIsConsentedAndConsentEndDateIsInPast() {
    applicationVersion.setStatus(ApplicationVersionStatus.CONSENTED);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withConsentEndDate(LocalDate.now(clock).minusDays(1))
        .build();

    when(consentDataService.getConsentData(application)).thenReturn(consentData);

    assertThat(consentService.nonExpiredConsentExists(applicationVersion)).isFalse();
  }

  @Test
  void nonExpiredConsentExists_applicationVersionStatusIsConsentedAndConsentEndDateIsToday() {
    applicationVersion.setStatus(ApplicationVersionStatus.CONSENTED);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withConsentEndDate(LocalDate.now(clock))
        .build();

    when(consentDataService.getConsentData(application)).thenReturn(consentData);

    assertThat(consentService.nonExpiredConsentExists(applicationVersion)).isTrue();
  }

  @Test
  void nonExpiredConsentExists_applicationVersionStatusIsConsentedAndConsentEndDateIsInFuture() {
    applicationVersion.setStatus(ApplicationVersionStatus.CONSENTED);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withConsentEndDate(LocalDate.now(clock).plusDays(1))
        .build();

    when(consentDataService.getConsentData(application)).thenReturn(consentData);

    assertThat(consentService.nonExpiredConsentExists(applicationVersion)).isTrue();
  }

  @Test
  void findConsent_consentDoesNotExist() {
    when(consentRepository.findByApplicationId(application.getId())).thenReturn(Optional.empty());

    assertThat(consentService.findConsent(application)).isEmpty();
  }

  @Test
  void findConsent_consentExists() {
    var consent = ConsentTestUtil.newBuilder().build();

    when(consentRepository.findByApplicationId(application.getId())).thenReturn(Optional.of(consent));

    assertThat(consentService.findConsent(application)).contains(consent);
  }

  @Test
  void getConsent_consentDoesNotExist() {
    doReturn(Optional.empty()).when(consentService).findConsent(application);

    assertThatThrownBy(() -> consentService.getConsent(application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to find consent for application %d".formatted(application.getId()));
  }

  @Test
  void getConsent_consentExists() {
    var consent = ConsentTestUtil.newBuilder().build();

    doReturn(Optional.of(consent)).when(consentService).findConsent(application);

    assertThat(consentService.getConsent(application)).isEqualTo(consent);
  }

  @Test
  void findPreviousConsentByApplicationId_previousConsentDoesNotExist() {
    when(consentRepository.findPreviousConsentByApplicationId(application.getId())).thenReturn(Optional.empty());

    assertThat(consentService.findPreviousConsentByApplicationId(application.getId())).isEmpty();
  }

  @Test
  void findPreviousConsentByApplicationId_previousConsentExists() {
    var previousConsent = ConsentTestUtil.newBuilder().build();

    when(consentRepository.findPreviousConsentByApplicationId(application.getId())).thenReturn(Optional.of(previousConsent));

    assertThat(consentService.findPreviousConsentByApplicationId(application.getId())).contains(previousConsent);
  }
}
