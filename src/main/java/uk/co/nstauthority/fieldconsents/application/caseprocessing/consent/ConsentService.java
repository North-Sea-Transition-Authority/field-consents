package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class ConsentService {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationAssetService applicationAssetService;
  private final ConsentRepository consentRepository;
  private final ConsentDataService consentDataService;
  private final ConsentLengthService consentLengthService;
  private final Clock clock;

  ConsentService(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationAssetService applicationAssetService,
      ConsentRepository consentRepository,
      ConsentDataService consentDataService,
      ConsentLengthService consentLengthService,
      Clock clock
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationAssetService = applicationAssetService;
    this.consentRepository = consentRepository;
    this.consentDataService = consentDataService;
    this.consentLengthService = consentLengthService;
    this.clock = clock;
  }

  @Transactional
  public Consent createConsent(Application application, ServiceUserDetail user) {
    var consent = new Consent();

    consent.setApplication(application);
    consent.setIssuedByWuaId(user.wuaId());
    consent.setIssuedInstant(clock.instant());

    consentRepository.save(consent);

    return consent;
  }

  @Transactional
  public void setConsentSupersededByConsent(Consent consent, Consent supersededByConsent) {
    if (consent.getSupersededByConsent() != null) {
      throw new IllegalStateException("Consent %d already superseded by consent %d"
          .formatted(consent.getId(), consent.getSupersededByConsent().getId()));
    }

    consent.setSupersededByConsent(supersededByConsent);

    consentRepository.save(consent);
  }

  public boolean shouldCheckProductionConsentExists(ApplicationVersion applicationVersion) {
    if (ApplicationType.PRODUCTION == applicationVersion.getApplication().getType()) {
      return false;
    }

    return switch (applicationVersion.getStatus()) {
      case IN_PROGRESS, AWAITING_PAYMENT, SUBMITTED -> applicationAssetService.getPrimaryAsset(applicationVersion).isField();
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

  public Optional<Consent> findConsent(Application application) {
    return consentRepository.findByApplicationId(application.getId());
  }

  public Consent getConsent(Application application) {
    return findConsent(application)
        .orElseThrow(() -> new IllegalStateException("Unable to find consent for application %d".formatted(application.getId())));
  }

  public Consent getPreviousConsent(Application application) {
    var applicationId = application.getId();

    if (!application.isRevision()) {
      throw new IllegalStateException("Application %d is not a revision".formatted(applicationId));
    }

    return consentRepository.findPreviousConsentByApplication(application)
        .orElseThrow(() -> new IllegalStateException("Unable to find previous consent for application %d"
            .formatted(applicationId)));
  }

  public String generateConsentApplicationReference(Consent consent) {
    var latestApplicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(consent.getApplication().getId());

    return applicationService.generateApplicationReference(latestApplicationVersion);
  }
}
