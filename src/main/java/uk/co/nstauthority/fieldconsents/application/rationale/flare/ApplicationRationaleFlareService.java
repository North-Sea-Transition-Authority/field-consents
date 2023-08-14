package uk.co.nstauthority.fieldconsents.application.rationale.flare;

import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.HOST;
import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.LOCATION;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleRepository;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@Service
public class ApplicationRationaleFlareService {

  private final ApplicationRationaleRepository repository;
  private final ApplicationAssetService applicationAssetService;

  ApplicationRationaleFlareService(
      ApplicationRationaleRepository repository,
      ApplicationAssetService applicationAssetService
  ) {
    this.repository = repository;
    this.applicationAssetService = applicationAssetService;
  }

  public boolean doesApplicationRationaleExistFor(ApplicationVersion applicationVersion) {
    return repository.existsApplicationRationaleByApplicationVersion(applicationVersion);
  }

  public Optional<ApplicationRationale> findByApplicationVersion(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();
    if (!ApplicationType.FLARE.equals(applicationType)) {
      throw new IllegalArgumentException("Expected ApplicationVersion.Application type to be [%s] but was [%s]"
          .formatted(ApplicationType.FLARE, applicationType)
      );
    }

    return repository.findByApplicationVersion(applicationVersion);
  }

  @Transactional
  public void saveApplicationRationale(
      ApplicationVersion applicationVersion,
      ApplicationRationaleType rationaleType,
      String comment,
      List<String> flaringLocationAssetKeys,
      String hostLocationAssetKey
  ) {
    var isIncrease = ApplicationRationaleType.INCREASE.equals(rationaleType);
    if (!isIncrease && Objects.nonNull(comment)) {
      throw new IllegalArgumentException(
          "Comment is not applicable for %s.%s".formatted(
              ApplicationRationaleType.class.getSimpleName(),
              rationaleType
          ));
    }

    var applicationRationale = repository.findByApplicationVersion(applicationVersion).orElseGet(ApplicationRationale::new);
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setRationaleType(rationaleType);
    applicationRationale.setComment(comment);
    repository.save(applicationRationale);

    applicationAssetService.deleteAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(LOCATION, HOST));

    for (var assetKey : flaringLocationAssetKeys) {
      applicationAssetService.createAssetForApplicationVersion(applicationVersion, assetKey, LOCATION);
    }
    applicationAssetService.createAssetForApplicationVersion(applicationVersion, hostLocationAssetKey, HOST);
  }

  public SummaryCard getApplicationRationaleFlareSummaryCard(ApplicationVersion applicationVersion) {
    var applicationRationaleOptional = findByApplicationVersion(applicationVersion);
    if (applicationRationaleOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var summaryDataView = new SummaryDataView(new ArrayList<>());
    var applicationRationale = applicationRationaleOptional.get();

    var increaseOrDecrease = applicationRationale.getRationaleType();
    summaryDataView.addKeyValue(
        "Is this application for an increase or decrease?",
        increaseOrDecrease.getDisplayName()
    );

    if (ApplicationRationaleType.INCREASE.equals(increaseOrDecrease)) {
      summaryDataView.addKeyValue(
          "Why are you asking for an increase?",
          applicationRationale.getComment()
      );
    }

    var flaringLocations = getFlaringLocations(applicationVersion)
        .stream()
        .map(ApplicationAssetView::getName)
        .collect(Collectors.joining(", "));
    summaryDataView.addKeyValue("Where does the flaring take place?", flaringLocations);

    var hostLocation = applicationAssetService.findAssetJsonListFor(applicationVersion, AssetRole.HOST)
        .stream()
        .findFirst()
        .map(AssetJson::getSelectionText)
        .orElse("");
    summaryDataView.addKeyValue("What is the host?", hostLocation);

    return SummaryCard.simpleSummaryCard(summaryDataView);
  }

  List<ApplicationAssetView> getFlaringLocations(ApplicationVersion applicationVersion) {
    return applicationAssetService
        .findAssetJsonListFor(applicationVersion, AssetRole.LOCATION)
        .stream()
        .map(ApplicationAssetView::from)
        .toList();
  }

  RestSearchItem getHostLocation(ApplicationVersion applicationVersion) {
    return applicationAssetService
        .findAssetJsonListFor(applicationVersion, AssetRole.HOST)
        .stream()
        .findFirst()
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

}
