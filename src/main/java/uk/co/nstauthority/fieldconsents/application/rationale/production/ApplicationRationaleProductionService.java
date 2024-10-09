package uk.co.nstauthority.fieldconsents.application.rationale.production;

import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.HOST;
import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.LOCATION;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleRepository;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@Service
public class ApplicationRationaleProductionService {

  private final ApplicationRationaleRepository repository;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationRationaleService applicationRationaleService;
  private final ApplicationVersionService applicationVersionService;
  private final Clock clock;
  private final ConsentDataService consentDataService;
  private final ConsentLengthService consentLengthService;
  private final ConsentFigureUnitService consentFigureUnitService;
  private final ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService;

  ApplicationRationaleProductionService(
      ApplicationRationaleRepository repository,
      ApplicationAssetService applicationAssetService,
      ApplicationRationaleService applicationRationaleService,
      ApplicationVersionService applicationVersionService,
      Clock clock,
      ConsentDataService consentDataService,
      ConsentLengthService consentLengthService,
      ConsentFigureUnitService consentFigureUnitService,
      ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService
  ) {
    this.repository = repository;
    this.applicationAssetService = applicationAssetService;
    this.applicationRationaleService = applicationRationaleService;
    this.applicationVersionService = applicationVersionService;
    this.clock = clock;
    this.consentDataService = consentDataService;
    this.consentLengthService = consentLengthService;
    this.consentFigureUnitService = consentFigureUnitService;
    this.consentDataLongTermProductionFiguresService = consentDataLongTermProductionFiguresService;
  }

  @Transactional
  public void saveApplicationRationale(
      ApplicationVersion applicationVersion,
      ApplicationRationaleType rationaleType,
      String comment,
      List<AssetKey> productionLocationAssetKeys,
      AssetKey hostLocationAssetKey
  ) {
    var applicationRationale = repository.findByApplicationVersion(applicationVersion).orElseGet(ApplicationRationale::new);
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setRationaleType(rationaleType);
    applicationRationale.setComment(comment);

    repository.save(applicationRationale);

    applicationAssetService.deleteAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(LOCATION, HOST));

    for (var assetKey : productionLocationAssetKeys) {
      applicationAssetService.createAssetForApplicationVersion(applicationVersion, assetKey, LOCATION);
    }
    applicationAssetService.createAssetForApplicationVersion(applicationVersion, hostLocationAssetKey, HOST);
  }

  public SummaryCard getSummaryCard(ApplicationVersion applicationVersion) {
    var applicationRationaleOptional = applicationRationaleService.findByApplicationVersion(applicationVersion);

    var summaryDataView = new SummaryDataView(new ArrayList<>());

    if (applicationRationaleOptional.isPresent()) {
      var applicationRationale = applicationRationaleOptional.get();

      var rationaleType = applicationRationale.getRationaleType();
      summaryDataView.addKeyValue(
          "Is this application for an increase, decrease, extension or other?",
          rationaleType.getDisplayName()
      );
      if (StringUtils.isNotBlank(applicationRationale.getComment())) {
        switch (rationaleType) {
          case INCREASE:
            summaryDataView.addKeyValue("Why are you asking for an increase?", applicationRationale.getComment());
            break;
          case DECREASE:
            summaryDataView.addKeyValue("Why are you asking for a decrease?", applicationRationale.getComment());
            break;
          case EXTENSION:
            summaryDataView.addKeyValue("Why are you asking for an extension?", applicationRationale.getComment());
            break;
          case OTHER:
            summaryDataView.addKeyValue("Why have you selected 'other'?", applicationRationale.getComment());
            break;
          case NO_CHANGE:
          default:
            break;
        }
      }
    }

    var productionLocations = applicationRationaleService.getLocations(applicationVersion)
        .stream()
        .map(assetJson -> ApplicationAssetView.from(assetJson).getName())
        .collect(Collectors.joining(", "));
    if (!productionLocations.isEmpty()) {
      summaryDataView.addKeyValue("At which location are the production activities?", productionLocations);
    }

    var hostLocation = applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST)
        .stream()
        .findFirst()
        .map(AssetJson::getSelectionText)
        .orElse("");
    if (!hostLocation.isEmpty()) {
      summaryDataView.addKeyValue("What is the host?", hostLocation);
    }

    if (summaryDataView.keyValues().isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    return SummaryCard.simpleSummaryCard(summaryDataView);
  }

  public Optional<OilAndGasMaximums> findOilAndGasMaximums(ApplicationVersion applicationVersion) {
    var currentYear = LocalDate.now(clock).getYear();
    return consentDataService
        .getConsentDataForYearAndApplicationVersionPrimaryAssetAndApplicationType(currentYear, applicationVersion)
        .stream()
        .max(Comparator.comparing(ConsentData::getConsentStartDate))
        .map(consentData -> getOilAndGasMaximumsForCurrentYear(currentYear, consentData));
  }

  OilAndGasMaximums getOilAndGasMaximumsForCurrentYear(Integer currentYear, ConsentData consentData) {
    var application = consentData.getApplication();
    var applicationType = application.getType();

    if (ApplicationType.PRODUCTION != applicationType) {
      throw new IllegalArgumentException(
          "Consent data [%s] is not for a production application. The application [%s] is of type %s"
              .formatted(consentData.getId(), application.getId(), applicationType));
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId());
    var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();
    var consentFigureUnitView = consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType);

    return switch (consentLengthType) {
      case SHORT_TERM, ANNUAL -> OilAndGasMaximums.from(currentYear, consentData, consentFigureUnitView);
      case LONG_TERM -> consentDataLongTermProductionFiguresService.getConsentDataLongTermProductionFiguresList(application)
          .stream()
          .filter(figures -> figures.getYear().equals(currentYear))
          .findFirst()
          .map(figures -> OilAndGasMaximums.from(figures, consentFigureUnitView))
          .orElse(null);
    };
  }

}
