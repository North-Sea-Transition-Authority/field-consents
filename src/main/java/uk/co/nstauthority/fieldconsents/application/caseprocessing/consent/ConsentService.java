package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.time.Clock;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.PdfRenderingOptions;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@Service
public class ConsentService {

  private final ApplicationService applicationService;
  private final ApplicationDocumentInstanceService applicationDocumentInstanceService;
  private final ConsentRepository consentRepository;
  private final FieldConsentsFileService fieldConsentsFileService;
  private final FileService fileService;
  private final Clock clock;

  ConsentService(
      ApplicationService applicationService,
      ApplicationDocumentInstanceService applicationDocumentInstanceService,
      ConsentRepository consentRepository,
      FieldConsentsFileService fieldConsentsFileService,
      FileService fileService,
      Clock clock
  ) {
    this.applicationService = applicationService;
    this.applicationDocumentInstanceService = applicationDocumentInstanceService;
    this.consentRepository = consentRepository;
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.fileService = fileService;
    this.clock = clock;
  }

  @Transactional
  public void issueConsent(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var application = applicationVersion.getApplication();

    var consent = new Consent();

    consent.setApplication(application);
    consent.setIssuedByWuaId(user.wuaId());
    consent.setIssuedInstant(clock.instant());

    consentRepository.save(consent);

    generateDocumentInstancesAndSaveToConsent(application, consent);
    copySupportingDocumentsToConsent(application, consent);

    applicationService.completeApplication(applicationVersion);
  }

  void generateDocumentInstancesAndSaveToConsent(Application application, Consent consent) {
    applicationDocumentInstanceService.getDocumentInstanceDtos(application).forEach(documentInstanceDto -> {
      var byteArrayResource = applicationDocumentInstanceService.renderPdf(
          application,
          documentInstanceDto,
          PdfRenderingOptions.newBuilder().build()
      );

      var fileSource = FileSource.fromInputStreamSource(
          byteArrayResource,
          documentInstanceDto.title(),
          MediaType.APPLICATION_PDF_VALUE,
          byteArrayResource.contentLength()
      );

      var consentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

      var fileUploadResponse = fileService.upload(builder -> builder
          .withFileSource(fileSource)
          .withUsage(consentFileUsage.usageId(), consentFileUsage.usageType(), consentFileUsage.documentType())
          .withValidate(false)
          .build());
      var error = fileUploadResponse.getError();
      if (error != null) {
        throw new IllegalStateException("Failed to upload file: %s".formatted(error));
      }
    });
  }

  void copySupportingDocumentsToConsent(Application application, Consent consent) {
    var supportingConsentDocumentApplicationFileUsage = ApplicationFileUsage.supportingConsentDocumentFrom(application);
    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    fieldConsentsFileService.copyUploadedFiles(
        supportingConsentDocumentApplicationFileUsage,
        supportingConsentDocumentConsentFileUsage
    );
  }
}
