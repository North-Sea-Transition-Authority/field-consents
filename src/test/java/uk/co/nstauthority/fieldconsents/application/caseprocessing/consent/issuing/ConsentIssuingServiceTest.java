package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document.ConsentDocumentGenerationDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.FieldConsentsPdfRenderResult;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@ExtendWith(MockitoExtension.class)
class ConsentIssuingServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationDocumentInstanceService applicationDocumentInstanceService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private FileService fileService;

  @Mock
  private ConsentService consentService;

  @Mock
  private ConsentEmailService consentEmailService;

  @Mock
  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  @Mock
  private ConsentDocumentGenerationDataService consentDocumentGenerationDataService;

  @InjectMocks
  @Spy
  private ConsentIssuingService consentIssuingService;

  private Application application;

  private ApplicationVersion applicationVersion;

  private ApplicationAsset primaryApplicationAsset;

  @BeforeEach
  void beforeEach() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();
    application = applicationVersion.getApplication();
  }

  @Test
  void issueConsent_applicationIsNotRevision() {
    application.setVariationNo(0);

    var user = ServiceUserDetailTestUtil.Builder().build();

    var consent = ConsentTestUtil.newBuilder().build();

    when(consentService.createConsent(application, user)).thenReturn(consent);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentIssuingService).generateDocumentInstancesAndSaveToConsent(any(), any(), any());
    doNothing().when(consentIssuingService).copySupportingDocumentsToConsent(any(), any());

    consentIssuingService.issueConsent(applicationVersion, user);

    verify(consentService, never()).setConsentSupersededByConsent(any(), any());
    verify(consentIssuingService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent, user);
    verify(consentIssuingService).copySupportingDocumentsToConsent(application, consent);
    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToOperator(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToCaseOfficer(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
  }

  @Test
  void issueConsent_applicationIsRevision() {
    application.setVariationNo(1);

    var user = ServiceUserDetailTestUtil.Builder().build();

    var consent = ConsentTestUtil.newBuilder().build();

    var previousConsent = ConsentTestUtil.newBuilder().withId(consent.getId() - 1).build();

    when(consentService.createConsent(application, user)).thenReturn(consent);
    when(consentService.getPreviousConsent(application)).thenReturn(previousConsent);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentIssuingService).generateDocumentInstancesAndSaveToConsent(any(), any(), any());
    doNothing().when(consentIssuingService).copySupportingDocumentsToConsent(any(), any());

    consentIssuingService.issueConsent(applicationVersion, user);

    verify(consentService).setConsentSupersededByConsent(previousConsent, consent);
    verify(consentIssuingService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent, user);
    verify(consentIssuingService).copySupportingDocumentsToConsent(application, consent);
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
    doNothing().when(consentIssuingService).generateDocumentInstancesAndSaveToConsent(any(), any(), any());
    doNothing().when(consentIssuingService).copySupportingDocumentsToConsent(any(), any());

    consentIssuingService.issueConsent(applicationVersion, user);

    verify(consentFieldEquityPartnerService, never()).saveFieldEquityPartners(any(), any());
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToOperatorFails_thenConsentIsStillIssued() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consent = ConsentTestUtil.newBuilder().build();

    when(consentService.createConsent(application, user)).thenReturn(consent);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentIssuingService).generateDocumentInstancesAndSaveToConsent(any(), any(), any());
    doNothing().when(consentIssuingService).copySupportingDocumentsToConsent(any(), any());

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consentEmailService)
        .sendConsentIssuedEmailToOperator(applicationVersion);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consentIssuingService.issueConsent(applicationVersion, user)
    );

    verify(consentIssuingService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent, user);
    verify(consentIssuingService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToOperator(applicationVersion);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToCaseOfficerFails_thenConsentIsStillIssued() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consent = ConsentTestUtil.newBuilder().build();

    when(consentService.createConsent(application, user)).thenReturn(consent);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentIssuingService).generateDocumentInstancesAndSaveToConsent(any(), any(), any());
    doNothing().when(consentIssuingService).copySupportingDocumentsToConsent(any(), any());

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consentEmailService)
        .sendConsentIssuedEmailToCaseOfficer(applicationVersion);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consentIssuingService.issueConsent(applicationVersion, user)
    );

    verify(consentIssuingService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent, user);
    verify(consentIssuingService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToCaseOfficer(applicationVersion);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
  }

  @Test
  void issueConsent_whenSendConsentIssuedEmailToFieldEquityPartners_thenConsentIsStillIssued() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consent = ConsentTestUtil.newBuilder().build();

    when(consentService.createConsent(application, user)).thenReturn(consent);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    doNothing().when(consentIssuingService).generateDocumentInstancesAndSaveToConsent(any(), any(), any());
    doNothing().when(consentIssuingService).copySupportingDocumentsToConsent(any(), any());

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consentEmailService)
        .sendConsentIssuedEmailToFieldEquityPartners(any(), any());

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consentIssuingService.issueConsent(applicationVersion, user)
    );

    verify(consentIssuingService).generateDocumentInstancesAndSaveToConsent(applicationVersion, consent, user);
    verify(consentIssuingService).copySupportingDocumentsToConsent(application, consent);

    verify(applicationService).consentApplication(applicationVersion);
    verify(consentEmailService).sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);
    verify(consentFieldEquityPartnerService).saveFieldEquityPartners(consent, applicationVersion);
  }

  @Test
  void generateDocumentInstancesAndSaveToConsent() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var consent = ConsentTestUtil.newBuilder().build();

    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(DocumentTemplateDtoTestUtil.builder().withDisplayOrder(1).build())
        .build();

    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(DocumentTemplateDtoTestUtil.builder().withDisplayOrder(2).build())
        .build();

    var documentInstanceDto3 = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(DocumentTemplateDtoTestUtil.builder().withDisplayOrder(3).build())
        .build();

    var renderResultWithGenerationData1 = new FieldConsentsPdfRenderResult(
      new ByteArrayResource(new byte[]{1}), "html1", Map.of("FOO1", "BAR1")
    );

    var renderResultWithGenerationData2 = new FieldConsentsPdfRenderResult(
      new ByteArrayResource(new byte[]{1, 2}), "html2", Map.of("FOO2", "BAR2")
    );

    var renderResultWithGenerationData3 = new FieldConsentsPdfRenderResult(
      new ByteArrayResource(new byte[]{1, 2, 3}), "html3", Map.of("FOO3", "BAR3")
    );

    when(applicationDocumentInstanceService.getDocumentInstanceDtos(applicationVersion.getApplication()))
        .thenReturn(List.of(documentInstanceDto2, documentInstanceDto1, documentInstanceDto3));
    when(applicationDocumentInstanceService.renderAndSignPdf(applicationVersion, documentInstanceDto1, user, false))
        .thenReturn(renderResultWithGenerationData1);
    when(applicationDocumentInstanceService.renderAndSignPdf(applicationVersion, documentInstanceDto2, user, false))
        .thenReturn(renderResultWithGenerationData2);
    when(applicationDocumentInstanceService.renderAndSignPdf(applicationVersion, documentInstanceDto3, user, false))
        .thenReturn(renderResultWithGenerationData3);

    ArgumentCaptor<Function<FileUploadRequest.Builder, FileUploadRequest>> fileUploadRequestBuilderFunctionCaptor =
        ArgumentCaptor.forClass(Function.class);

    when(fileService.upload(fileUploadRequestBuilderFunctionCaptor.capture()))
        .thenReturn(FileUploadResponse.success(UUID.randomUUID(), FileSource.fromInputStreamSource(null, "test", "test/test", 1)));

    consentIssuingService.generateDocumentInstancesAndSaveToConsent(applicationVersion, consent, user);

    var consentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    assertThat(fileUploadRequestBuilderFunctionCaptor.getAllValues())
        .extracting(function -> function.apply(FileUploadRequest.newBuilder().withBucket("bucket")))
        .extracting(
            request -> {
              try {
                return request.fileSource().getInputStream().readAllBytes();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            },
            request -> request.fileSource().getFileName(),
            request -> request.fileSource().getContentType(),
            request -> request.fileSource().getSize(),
            FileUploadRequest::usageId,
            FileUploadRequest::usageType,
            FileUploadRequest::documentType,
            FileUploadRequest::description,
            FileUploadRequest::validate
        )
        .containsExactly(
            tuple(
                renderResultWithGenerationData1.pdfContent().getByteArray(),
                "%s.%s".formatted(documentInstanceDto1.title(), MediaType.APPLICATION_PDF.getSubtype()),
                MediaType.APPLICATION_PDF_VALUE,
                renderResultWithGenerationData1.pdfContent().contentLength(),
                consentFileUsage.usageId(),
                consentFileUsage.usageType(),
                consentFileUsage.documentType(),
                documentInstanceDto1.description(),
                false
            ),
            tuple(
                renderResultWithGenerationData2.pdfContent().getByteArray(),
                "%s.%s".formatted(documentInstanceDto2.title(), MediaType.APPLICATION_PDF.getSubtype()),
                MediaType.APPLICATION_PDF_VALUE,
                renderResultWithGenerationData2.pdfContent().contentLength(),
                consentFileUsage.usageId(),
                consentFileUsage.usageType(),
                consentFileUsage.documentType(),
                documentInstanceDto2.description(),
                false
            ),
            tuple(
                renderResultWithGenerationData3.pdfContent().getByteArray(),
                "%s.%s".formatted(documentInstanceDto3.title(), MediaType.APPLICATION_PDF.getSubtype()),
                MediaType.APPLICATION_PDF_VALUE,
                renderResultWithGenerationData3.pdfContent().contentLength(),
                consentFileUsage.usageId(),
                consentFileUsage.usageType(),
                consentFileUsage.documentType(),
                documentInstanceDto2.description(),
                false
            )
        );

    var inOrder = inOrder(consentDocumentGenerationDataService);
    inOrder.verify(consentDocumentGenerationDataService).createDocumentGenerationData(consent, documentInstanceDto1, renderResultWithGenerationData1);
    inOrder.verify(consentDocumentGenerationDataService).createDocumentGenerationData(consent, documentInstanceDto2, renderResultWithGenerationData2);
    inOrder.verify(consentDocumentGenerationDataService).createDocumentGenerationData(consent, documentInstanceDto3, renderResultWithGenerationData3);
  }

  @Test
  void copySupportingDocumentsToConsent() {
    var consent = ConsentTestUtil.newBuilder().build();

    consentIssuingService.copySupportingDocumentsToConsent(application, consent);

    var supportingConsentDocumentApplicationFileUsage = ApplicationFileUsage.supportingConsentDocumentFrom(application);
    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    verify(fieldConsentsFileService).copyUploadedFiles(
        supportingConsentDocumentApplicationFileUsage,
        supportingConsentDocumentConsentFileUsage
    );
  }
}
