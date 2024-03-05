package uk.co.nstauthority.fieldconsents.application.rationale.production;

import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.HOST;
import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.LOCATION;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleRepository;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@Service
public class ApplicationRationaleProductionService {

  private final ApplicationRationaleRepository repository;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationRationaleService applicationRationaleService;

  ApplicationRationaleProductionService(
      ApplicationRationaleRepository repository,
      ApplicationAssetService applicationAssetService,
      ApplicationRationaleService applicationRationaleService
  ) {
    this.repository = repository;
    this.applicationAssetService = applicationAssetService;
    this.applicationRationaleService = applicationRationaleService;
  }

  @Transactional
  public void saveApplicationRationale(
      ApplicationVersion applicationVersion,
      ApplicationRationaleType rationaleType,
      String extensionReason,
      String otherReason,
      List<String> productionLocationAssetKeys,
      String hostLocationAssetKey
  ) {
    var applicationRationale = repository.findByApplicationVersion(applicationVersion).orElseGet(ApplicationRationale::new);
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setRationaleType(rationaleType);

    if (ApplicationRationaleType.EXTENSION.equals(rationaleType)) {
      applicationRationale.setComment(extensionReason);
    }
    if (ApplicationRationaleType.OTHER.equals(rationaleType)) {
      applicationRationale.setComment(otherReason);
    }

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

      if (ApplicationRationaleType.EXTENSION.equals(rationaleType)) {
        summaryDataView.addKeyValue("Explain why you are requesting an extension", applicationRationale.getComment());
      }
      if (ApplicationRationaleType.OTHER.equals(rationaleType)) {
        summaryDataView.addKeyValue("Explain why you have selected 'other'", applicationRationale.getComment());
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

}
