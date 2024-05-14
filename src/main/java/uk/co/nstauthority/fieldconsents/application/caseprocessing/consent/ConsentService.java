package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document.ConsentDocumentGenerationDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentEmailService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.PdfRenderingOptions;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class ConsentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentService.class);

  private final ApplicationService applicationService;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationDocumentInstanceService applicationDocumentInstanceService;
  private final ConsentRepository consentRepository;
  private final ConsentDataService consentDataService;
  private final ConsentLengthService consentLengthService;
  private final FieldConsentsFileService fieldConsentsFileService;
  private final FileService fileService;
  private final Clock clock;
  private final ConsentEmailService consentEmailService;
  private final ConsentFieldEquityPartnerService consentFieldEquityPartnerService;
  private final ConsentDocumentGenerationDataService consentDocumentGenerationDataService;

  ConsentService(
      ApplicationService applicationService,
      ApplicationAssetService applicationAssetService,
      ApplicationDocumentInstanceService applicationDocumentInstanceService,
      ConsentRepository consentRepository,
      ConsentDataService consentDataService,
      ConsentLengthService consentLengthService,
      FieldConsentsFileService fieldConsentsFileService,
      FileService fileService,
      Clock clock,
      ConsentEmailService consentEmailService,
      ConsentFieldEquityPartnerService consentFieldEquityPartnerService,
      ConsentDocumentGenerationDataService consentDocumentGenerationDataService
  ) {
    this.applicationService = applicationService;
    this.applicationAssetService = applicationAssetService;
    this.applicationDocumentInstanceService = applicationDocumentInstanceService;
    this.consentRepository = consentRepository;
    this.consentDataService = consentDataService;
    this.consentLengthService = consentLengthService;
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.fileService = fileService;
    this.clock = clock;
    this.consentEmailService = consentEmailService;
    this.consentFieldEquityPartnerService = consentFieldEquityPartnerService;
    this.consentDocumentGenerationDataService = consentDocumentGenerationDataService;
  }

  @Transactional
  public void issueConsent(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var application = applicationVersion.getApplication();

    var consent = new Consent();

    consent.setApplication(application);
    consent.setIssuedByWuaId(user.wuaId());
    consent.setIssuedInstant(clock.instant());

    consentRepository.save(consent);

    if (applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
      consentFieldEquityPartnerService.saveFieldEquityPartners(consent, applicationVersion);
    }

    generateDocumentInstancesAndSaveToConsent(applicationVersion, consent);
    copySupportingDocumentsToConsent(application, consent);

    applicationService.consentApplication(applicationVersion);

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

  public boolean shouldCheckProductionConsentExists(ApplicationVersion applicationVersion) {
    if (ApplicationType.PRODUCTION == applicationVersion.getApplication().getType()) {
      return false;
    }

    return switch (applicationVersion.getStatus()) {
      case IN_PROGRESS, AWAITING_PAYMENT, SUBMITTED -> true;
      default -> false;
    };
  }

  public ProductionConsentCheckResult checkProductionConsentExistsForInProgressApplication(
      ApplicationVersion applicationVersion
  ) {
    var consentLengthDetailsOptional = consentLengthService.findConsentLengthDetails(applicationVersion);
    if (consentLengthDetailsOptional.isEmpty()) {
      return ProductionConsentCheckResult.CONSENT_DETAILS_DO_NOT_EXIST;
    }

    // find consent data for the primary and secondary fields on this application
    var fieldIds = applicationAssetService
        .findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
            applicationVersion,
            AssetType.FIELD,
            Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
        )
        .stream()
        .map(ApplicationAsset::getAssetId)
        .collect(Collectors.toSet());

    var consentLengthDetails = consentLengthDetailsOptional.get();
    var proposedConsentStartDate = consentLengthService.getProposedConsentStartDate(consentLengthDetails);
    var proposedConsentEndDate = consentLengthService.getProposedConsentEndDate(consentLengthDetails);

    var consentDataListByFieldId = consentDataService.getConsentDataListInRangeForConsentedProductionApplicationsByFieldId(
        proposedConsentStartDate,
        proposedConsentEndDate,
        fieldIds
    );

    // if there is no production consent data, this application cannot be within a production period
    if (consentDataListByFieldId.isEmpty()) {
      return ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;
    }

    for (var fieldId : fieldIds) {
      var consentDataList = consentDataListByFieldId.get(fieldId);

      // if there is no consent data for this field, the application is not within a production period
      if (consentDataList == null || consentDataList.isEmpty()) {
        return ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;
      }

      if (!allDaysCoveredByProductionConsents(consentDataList, proposedConsentStartDate, proposedConsentEndDate)) {
        return ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;
      }
    }

    return ProductionConsentCheckResult.WITHIN_ACTIVE_CONSENT;
  }

  boolean allDaysCoveredByProductionConsents(List<ConsentData> consentDataList, LocalDate start, LocalDate end) {
    var requestedDays = generateRange(start, end);
    var consentedDays = consentDataList
        .stream()
        .flatMap(consentData -> generateRange(consentData.getConsentStartDate(), consentData.getConsentEndDate()).stream())
        .filter(consentDate -> DateUtils.isAfterOrEqualTo(consentDate, start))
        .filter(consentDate -> DateUtils.isBeforeOrEqualTo(consentDate, end))
        .collect(Collectors.toSet());

    return CollectionUtils.disjunction(requestedDays, consentedDays).isEmpty();
  }

  List<LocalDate> generateRange(LocalDate start, LocalDate end) {
    return start.datesUntil(end.plusDays(1)).toList(); // add 1 day to make the range inclusive of the end date
  }

  public boolean nonExpiredConsentExists(ApplicationVersion applicationVersion) {
    if (applicationVersion.getStatus() != ApplicationVersionStatus.CONSENTED) {
      return false;
    }

    var consentEndDate = consentDataService.getConsentData(applicationVersion.getApplication()).getConsentEndDate();

    return DateUtils.isBeforeOrEqualTo(LocalDate.now(clock), consentEndDate);
  }

  void generateDocumentInstancesAndSaveToConsent(ApplicationVersion applicationVersion, Consent consent) {
    var documentInstanceDtos = applicationDocumentInstanceService.getDocumentInstanceDtos(applicationVersion.getApplication());

    for (var documentInstanceDto : documentInstanceDtos) {
      generateDocumentInstanceAndSaveToConsent(applicationVersion, documentInstanceDto, consent);
    }
  }

  private void generateDocumentInstanceAndSaveToConsent(
      ApplicationVersion applicationVersion,
      DocumentInstanceDto documentInstanceDto,
      Consent consent
  ) {
    var renderResultWithGenerationData = applicationDocumentInstanceService.renderPdf(
        applicationVersion,
        documentInstanceDto,
        PdfRenderingOptions.newBuilder().build()
    );

    var pdfContent = renderResultWithGenerationData.pdfRenderResult().pdfContent();

    var fileSource = FileSource.fromInputStreamSource(
        pdfContent,
        "%s.%s".formatted(documentInstanceDto.title(), MediaType.APPLICATION_PDF.getSubtype()),
        MediaType.APPLICATION_PDF_VALUE,
        pdfContent.contentLength()
    );

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
        renderResultWithGenerationData
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

  Optional<Consent> findConsent(Application application) {
    return consentRepository.findByApplication_Id(application.getId());
  }

  Consent getConsent(Application application) {
    return findConsent(application)
        .orElseThrow(() -> new IllegalStateException("Unable to find consent for application %d".formatted(application.getId())));
  }
}
