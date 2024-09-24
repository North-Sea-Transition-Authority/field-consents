package uk.co.nstauthority.fieldconsents.application.rationale.vent;

import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.HOST;
import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.LOCATION;

import io.micrometer.common.util.StringUtils;
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
public class ApplicationRationaleVentService {

  private final ApplicationRationaleRepository repository;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationRationaleService applicationRationaleService;

  ApplicationRationaleVentService(
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
      String comment,
      List<String> ventingLocationAssetKeys,
      String hostLocationAssetKey
  ) {
    var applicationRationale = repository.findByApplicationVersion(applicationVersion).orElseGet(ApplicationRationale::new);
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setRationaleType(rationaleType);
    applicationRationale.setComment(comment);
    repository.save(applicationRationale);

    applicationAssetService.deleteAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(LOCATION, HOST));

    for (var assetKey : ventingLocationAssetKeys) {
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
      summaryDataView.addKeyValue("Is this application for an increase or decrease?", rationaleType.getDisplayName());

      if (StringUtils.isNotBlank(applicationRationale.getComment())) {
        switch (rationaleType) {
          case INCREASE:
            summaryDataView.addKeyValue("Why are you asking for an increase?", applicationRationale.getComment());
            break;
          case DECREASE:
            summaryDataView.addKeyValue("Why are you asking for a decrease?", applicationRationale.getComment());
            break;
          default:
            break;
        }
      }
    }

    var flaringLocations = applicationRationaleService.getLocations(applicationVersion)
        .stream()
        .map(assetJson -> ApplicationAssetView.from(assetJson).getName())
        .collect(Collectors.joining(", "));
    if (!flaringLocations.isEmpty()) {
      summaryDataView.addKeyValue("Where does the venting take place?", flaringLocations);
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
