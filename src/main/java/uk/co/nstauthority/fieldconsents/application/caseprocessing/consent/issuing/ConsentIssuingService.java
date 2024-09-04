package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document.ConsentDocumentComparators;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document.ConsentDocumentGenerationDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@Service
public class ConsentIssuingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentIssuingService.class);

  private final ApplicationService applicationService;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationDocumentInstanceService applicationDocumentInstanceService;
  private final FieldConsentsFileService fieldConsentsFileService;
  private final FileService fileService;
  private final ConsentService consentService;
  private final ConsentEmailService consentEmailService;
  private final ConsentFieldEquityPartnerService consentFieldEquityPartnerService;
  private final ConsentDocumentGenerationDataService consentDocumentGenerationDataService;

  ConsentIssuingService(
      ApplicationService applicationService,
      ApplicationAssetService applicationAssetService,
      ApplicationDocumentInstanceService applicationDocumentInstanceService,
      FieldConsentsFileService fieldConsentsFileService,
      FileService fileService,
      ConsentService consentService,
      ConsentEmailService consentEmailService,
      ConsentFieldEquityPartnerService consentFieldEquityPartnerService,
      ConsentDocumentGenerationDataService consentDocumentGenerationDataService
  ) {
    this.applicationService = applicationService;
    this.applicationAssetService = applicationAssetService;
    this.applicationDocumentInstanceService = applicationDocumentInstanceService;
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.fileService = fileService;
    this.consentService = consentService;
    this.consentEmailService = consentEmailService;
    this.consentFieldEquityPartnerService = consentFieldEquityPartnerService;
    this.consentDocumentGenerationDataService = consentDocumentGenerationDataService;
  }

  @Transactional
  @Observed(name = "fcs.consent.issued", contextualName = "consent issued")
  public Consent issueConsent(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var application = applicationVersion.getApplication();

    var consent = consentService.createConsent(application, user);

    if (application.isRevision()) {
      var previousConsent = consentService.getPreviousConsent(application);

      consentService.setConsentSupersededByConsent(previousConsent, consent);
    }

    if (applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
      consentFieldEquityPartnerService.saveFieldEquityPartners(consent, applicationVersion);
    }

    generateDocumentInstancesAndSaveToConsent(applicationVersion, consent, user);
    copySupportingDocumentsToConsent(application, consent);

    applicationService.consentApplication(applicationVersion);

    return consent;
  }

  public void sendConsentIssuedEmails(
      ApplicationVersion applicationVersion,
      ServiceUserDetail user,
      Consent consent
  ) {
    try {
      consentEmailService.sendConsentIssuedEmailToOperator(applicationVersion);
    } catch (Exception exception) {
      LOGGER.error("""
            An attempt to send a consent issued notification to the operator \
            by user with wuaId [{}] for application version with id [{}] failed. \
            Note: this hasn't prevented the consent being issued.
            """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
    try {
      consentEmailService.sendConsentIssuedEmailToCaseOfficer(applicationVersion);
    } catch (Exception exception) {
      LOGGER.error("""
            An attempt to send a consent issued notification to case officer \
            by user with wuaId [{}] for application version with id [{}] failed. \
            Note: this hasn't prevented the consent being issued.
            """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
    try {
      consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);
    } catch (Exception exception) {
      LOGGER.error("""
            An attempt to send a consent issued notification to field equity partners \
            by user with wuaId [{}] for application version with id [{}] failed. \
            Note: this hasn't prevented the consent being issued.
            """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
  }

  void generateDocumentInstancesAndSaveToConsent(
      ApplicationVersion applicationVersion,
      Consent consent,
      ServiceUserDetail user
  ) {
    var documentInstanceDtos = applicationDocumentInstanceService
        .getDocumentInstanceDtos(applicationVersion.getApplication())
        .stream()
        .sorted(ConsentDocumentComparators.documentInstanceDto())
        .toList();

    for (var documentInstanceDto : documentInstanceDtos) {
      generateDocumentInstanceAndSaveToConsent(
          applicationVersion,
          documentInstanceDto,
          consent,
          user
      );
    }
  }

  private void generateDocumentInstanceAndSaveToConsent(
      ApplicationVersion applicationVersion,
      DocumentInstanceDto documentInstanceDto,
      Consent consent,
      ServiceUserDetail serviceUserDetail
  ) {
    var pdfRenderResult = applicationDocumentInstanceService.renderAndSignPdf(
        applicationVersion,
        documentInstanceDto,
        serviceUserDetail,
        false
    );
    ByteArrayResource pdfContent = pdfRenderResult.pdfContent();

    FileSource fileSource = FileSource.fromInputStreamSource(
        pdfContent,
        "%s.%s".formatted(documentInstanceDto.title(), MediaType.APPLICATION_PDF.getSubtype()),
        MediaType.APPLICATION_PDF_VALUE,
        pdfContent.contentLength());

    var consentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    var fileUploadResponse = fileService.upload(builder -> builder
        .withFileSource(fileSource)
        .withUsage(consentFileUsage.usageId(), consentFileUsage.usageType(), consentFileUsage.documentType())
        .withDescription(documentInstanceDto.description())
        .withValidate(false)
        .build());
    var error = fileUploadResponse.getError();
    if (error != null) {
      throw new IllegalStateException("Failed to upload file: %s".formatted(error));
    }

    consentDocumentGenerationDataService.createDocumentGenerationData(
        consent,
        documentInstanceDto,
        pdfRenderResult
    );
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
