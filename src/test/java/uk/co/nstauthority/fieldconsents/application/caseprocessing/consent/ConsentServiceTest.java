package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.core.FileUploadRequest;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentEmailService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.PdfRenderingOptions;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationDocumentInstanceService applicationDocumentInstanceService;

  @Mock
  private ConsentRepository consentRepository;

  @Mock
  private ConsentDataService consentDataService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private FileService fileService;

  @Mock
  private ConsentEmailService consentEmailService;

  @Mock
  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ConsentService consentService;

  private Application application;

  private ApplicationVersion applicationVersion;

  private ApplicationAsset primaryApplicationAsset;

  @BeforeEach
  void beforeEach() {
    consentService = spy(new ConsentService(
        applicationService,
        applicationAssetService,
        applicationDocumentInstanceService,
        consentRepository,
        consentDataService,
        consentLengthService,
        fieldConsentsFileService,
        fileService,
        clock,
        consentEmailService,
        consentFieldEquityPartnerService
    ));

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();
    application = applicationVersion.getApplication();
  }

  @Test
  void issueConsent() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentCaptor = ArgumentCaptor.forClass(Consent.class);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentService).generateDocumentInstancesAndSaveToConsent(any(), any());
    doNothing().when(consentService).copySupportingDocumentsToConsent(any(), any());

    consentService.issueConsent(applicationVersion, user);

    verify(consentRepository).save(consentCaptor.capture());

    var consent = consentCaptor.getValue();

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

    verify(consentService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent);
    verify(consentService).copySupportingDocumentsToConsent(application, consent);
    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToOperator(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToCaseOfficer(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
  }

  @Test
  void issueConsent_whenApplicationIsForTerminal_thenFieldEquityPartnersAreNotSaved() {
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.TERMINAL).build();
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentService).generateDocumentInstancesAndSaveToConsent(any(), any());
    doNothing().when(consentService).copySupportingDocumentsToConsent(any(), any());

    consentService.issueConsent(applicationVersion, user);

    verify(consentFieldEquityPartnerService, never()).saveFieldEquityPartners(any(), any());
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToOperatorFails_thenConsentIsStillIssued() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentCaptor = ArgumentCaptor.forClass(Consent.class);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentService).generateDocumentInstancesAndSaveToConsent(any(), any());
    doNothing().when(consentService).copySupportingDocumentsToConsent(any(), any());

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consentEmailService)
        .sendConsentIssuedEmailToOperator(applicationVersion);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consentService.issueConsent(applicationVersion, user)
    );

    verify(consentRepository).save(consentCaptor.capture());

    var consent = consentCaptor.getValue();

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

    verify(consentService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent);
    verify(consentService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToOperator(applicationVersion);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToCaseOfficerFails_thenConsentIsStillIssued() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentCaptor = ArgumentCaptor.forClass(Consent.class);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentService).generateDocumentInstancesAndSaveToConsent(any(), any());
    doNothing().when(consentService).copySupportingDocumentsToConsent(any(), any());

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consentEmailService)
        .sendConsentIssuedEmailToCaseOfficer(applicationVersion);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consentService.issueConsent(applicationVersion, user)
    );

    verify(consentRepository).save(consentCaptor.capture());

    var consent = consentCaptor.getValue();

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

    verify(consentService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent);
    verify(consentService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToCaseOfficer(applicationVersion);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToFieldEquityPartners_thenConsentIsStillIssued() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentCaptor = ArgumentCaptor.forClass(Consent.class);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentService).generateDocumentInstancesAndSaveToConsent(any(), any());
    doNothing().when(consentService).copySupportingDocumentsToConsent(any(), any());

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consentEmailService)
        .sendConsentIssuedEmailToFieldEquityPartners(any(), any());

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consentService.issueConsent(applicationVersion, user)
    );

    verify(consentRepository).save(consentCaptor.capture());

    var consent = consentCaptor.getValue();

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

    verify(consentService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent);
    verify(consentService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
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

  @Test
  void generateDocumentInstancesAndSaveToConsent() {
    var consent = ConsentTestUtil.newBuilder().build();

    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder().build();
    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder().build();

    var byteArrayResource1 = mock(ByteArrayResource.class);
    var byteArrayResource2 = mock(ByteArrayResource.class);

    when(applicationDocumentInstanceService.getDocumentInstanceDtos(applicationVersion.getApplication()))
        .thenReturn(List.of(documentInstanceDto1, documentInstanceDto2));
    when(applicationDocumentInstanceService.renderPdf(applicationVersion, documentInstanceDto1, PdfRenderingOptions.newBuilder().build()))
        .thenReturn(byteArrayResource1);
    when(applicationDocumentInstanceService.renderPdf(applicationVersion, documentInstanceDto2, PdfRenderingOptions.newBuilder().build()))
        .thenReturn(byteArrayResource2);

    ArgumentCaptor<Function<FileUploadRequest.Builder, FileUploadRequest>> fileUploadRequestBuilderFunctionCaptor =
        ArgumentCaptor.forClass(Function.class);

    when(fileService.upload(fileUploadRequestBuilderFunctionCaptor.capture()))
        .thenReturn(FileUploadResponse.success(UUID.randomUUID(), FileSource.fromInputStreamSource(null, "test", "test/test", 1)));

    consentService.generateDocumentInstancesAndSaveToConsent(applicationVersion, consent);

    var consentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    assertThat(fileUploadRequestBuilderFunctionCaptor.getAllValues())
        .extracting(function -> function.apply(FileUploadRequest.newBuilder().withBucket("bucket")))
        .extracting(
            FileUploadRequest::fileSource,
            FileUploadRequest::usageId,
            FileUploadRequest::usageType,
            FileUploadRequest::documentType,
            FileUploadRequest::description,
            FileUploadRequest::validate
        )
        .containsExactly(
            tuple(
                FileSource.fromInputStreamSource(
                    byteArrayResource1,
                    "%s.%s".formatted(documentInstanceDto1.title(), MediaType.APPLICATION_PDF.getSubtype()),
                    MediaType.APPLICATION_PDF_VALUE,
                    byteArrayResource1.contentLength()
                ),
                consentFileUsage.usageId(),
                consentFileUsage.usageType(),
                consentFileUsage.documentType(),
                documentInstanceDto1.description(),
                false
            ),
            tuple(
                FileSource.fromInputStreamSource(
                    byteArrayResource2,
                    "%s.%s".formatted(documentInstanceDto2.title(), MediaType.APPLICATION_PDF.getSubtype()),
                    MediaType.APPLICATION_PDF_VALUE,
                    byteArrayResource2.contentLength()
                ),
                consentFileUsage.usageId(),
                consentFileUsage.usageType(),
                consentFileUsage.documentType(),
                documentInstanceDto2.description(),
                false
            )
        );
  }

  @Test
  void copySupportingDocumentsToConsent() {
    var consent = ConsentTestUtil.newBuilder().build();

    consentService.copySupportingDocumentsToConsent(application, consent);

    var supportingConsentDocumentApplicationFileUsage = ApplicationFileUsage.supportingConsentDocumentFrom(application);
    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    verify(fieldConsentsFileService).copyUploadedFiles(
        supportingConsentDocumentApplicationFileUsage,
        supportingConsentDocumentConsentFileUsage
    );
  }

  @Test
  void findConsent_consentDoesNotExist() {
    when(consentRepository.findByApplication_Id(application.getId())).thenReturn(Optional.empty());

    assertThat(consentService.findConsent(application)).isEmpty();
  }

  @Test
  void findConsent_consentExists() {
    var consent = ConsentTestUtil.newBuilder().build();

    when(consentRepository.findByApplication_Id(application.getId())).thenReturn(Optional.of(consent));

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
}
