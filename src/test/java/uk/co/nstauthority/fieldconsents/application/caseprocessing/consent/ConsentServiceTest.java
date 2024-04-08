package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentEmailService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.PdfRenderingOptions;
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

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ConsentService consentService;

  @BeforeEach
  void beforeEach() {
    consentService = spy(new ConsentService(
        applicationService,
        applicationDocumentInstanceService,
        consentRepository,
        fieldConsentsFileService,
        fileService,
        clock,
        consentEmailService
    ));
  }

  @Test
  void issueConsent() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();

    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentCaptor = ArgumentCaptor.forClass(Consent.class);

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

    verify(consentService).generateDocumentInstancesAndSaveToConsent(application, consent);
    verify(consentService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).completeApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToOperator(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToCaseOfficer(applicationVersion);
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToOperatorFails_thenConsentIsStillIssued() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();

    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentCaptor = ArgumentCaptor.forClass(Consent.class);

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

    verify(consentService).generateDocumentInstancesAndSaveToConsent(application, consent);
    verify(consentService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).completeApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToOperator(applicationVersion);
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToCaseOfficerFails_thenConsentIsStillIssued() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();

    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentCaptor = ArgumentCaptor.forClass(Consent.class);

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

    verify(consentService).generateDocumentInstancesAndSaveToConsent(application, consent);
    verify(consentService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).completeApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToCaseOfficer(applicationVersion);
  }

  @Test
  void generateDocumentInstancesAndSaveToConsent() {
    var application = ApplicationTestUtil.getSubmittedApplicationWithType(ApplicationType.PRODUCTION);
    var consent = ConsentTestUtil.newBuilder().build();

    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder().build();
    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder().build();

    var byteArrayResource1 = mock(ByteArrayResource.class);
    var byteArrayResource2 = mock(ByteArrayResource.class);

    when(applicationDocumentInstanceService.getDocumentInstanceDtos(application))
        .thenReturn(List.of(documentInstanceDto1, documentInstanceDto2));
    when(applicationDocumentInstanceService.renderPdf(application, documentInstanceDto1, PdfRenderingOptions.newBuilder().build()))
        .thenReturn(byteArrayResource1);
    when(applicationDocumentInstanceService.renderPdf(application, documentInstanceDto2, PdfRenderingOptions.newBuilder().build()))
        .thenReturn(byteArrayResource2);

    ArgumentCaptor<Function<FileUploadRequest.Builder, FileUploadRequest>> fileUploadRequestBuilderFunctionCaptor =
        ArgumentCaptor.forClass(Function.class);

    when(fileService.upload(fileUploadRequestBuilderFunctionCaptor.capture()))
        .thenReturn(FileUploadResponse.success(UUID.randomUUID(), FileSource.fromInputStreamSource(null, "test", "test/test", 1)));

    consentService.generateDocumentInstancesAndSaveToConsent(application, consent);

    var consentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    assertThat(fileUploadRequestBuilderFunctionCaptor.getAllValues())
        .extracting(function -> function.apply(FileUploadRequest.newBuilder().withBucket("bucket")))
        .extracting(
            FileUploadRequest::fileSource,
            FileUploadRequest::usageId,
            FileUploadRequest::usageType,
            FileUploadRequest::documentType,
            FileUploadRequest::validate
        )
        .containsExactly(
            tuple(
                FileSource.fromInputStreamSource(
                    byteArrayResource1,
                    documentInstanceDto1.title(),
                    MediaType.APPLICATION_PDF_VALUE,
                    byteArrayResource1.contentLength()
                ),
                consentFileUsage.usageId(),
                consentFileUsage.usageType(),
                consentFileUsage.documentType(),
                false
            ),
            tuple(
                FileSource.fromInputStreamSource(
                    byteArrayResource2,
                    documentInstanceDto2.title(),
                    MediaType.APPLICATION_PDF_VALUE,
                    byteArrayResource2.contentLength()
                ),
                consentFileUsage.usageId(),
                consentFileUsage.usageType(),
                consentFileUsage.documentType(),
                false
            )
        );
  }

  @Test
  void copySupportingDocumentsToConsent() {
    var application = ApplicationTestUtil.getSubmittedApplicationWithType(ApplicationType.PRODUCTION);
    var consent = ConsentTestUtil.newBuilder().build();

    consentService.copySupportingDocumentsToConsent(application, consent);

    var supportingConsentDocumentApplicationFileUsage = ApplicationFileUsage.supportingConsentDocumentFrom(application);
    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    verify(fieldConsentsFileService).copyUploadedFiles(
        supportingConsentDocumentApplicationFileUsage,
        supportingConsentDocumentConsentFileUsage
    );
  }
}
