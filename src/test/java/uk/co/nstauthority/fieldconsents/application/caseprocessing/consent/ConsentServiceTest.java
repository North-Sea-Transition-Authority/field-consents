package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentEmailService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.PdfRenderingOptions;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ConsentServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationDocumentInstanceService applicationDocumentInstanceService;

  @Mock
  private ConsentRepository consentRepository;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private FileService fileService;

  @Mock
  private ConsentEmailService consentEmailService;

  @Mock
  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ConsentService consentService;

  private Application application;

  private ApplicationVersion applicationVersion;

  private ApplicationAsset primaryApplicationAsset;

  @BeforeEach
  void beforeEach() {
    consentService = spy(new ConsentService(
        applicationService,
        applicationDocumentInstanceService,
        consentRepository,
        fieldConsentsFileService,
        fileService,
        clock,
        consentEmailService,
        consentFieldEquityPartnerService,
        applicationAssetService
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
    verify(applicationService).completeApplication(applicationVersion);
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

    verify(applicationService).completeApplication(applicationVersion);
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

    verify(applicationService).completeApplication(applicationVersion);
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

    verify(applicationService).completeApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
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
