package uk.co.nstauthority.fieldconsents.application.assets;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@Service
public class AssetSummaryService {
  
  private final ApplicationAssetService applicationAssetService;

  private final ApplicationAssetLicenceService applicationAssetLicenceService;

  private final OrganisationUnitService organisationUnitService;

  private final ApplicationFlagService applicationFlagService;

  @Autowired
  public AssetSummaryService(ApplicationAssetService applicationAssetService,
                             ApplicationAssetLicenceService applicationAssetLicenceService,
                             OrganisationUnitService organisationUnitService,
                             ApplicationFlagService applicationFlagService) {
    this.applicationAssetService = applicationAssetService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
    this.organisationUnitService = organisationUnitService;
    this.applicationFlagService = applicationFlagService;
  }

  public List<AssetView> getSummaryViews(ApplicationVersion applicationVersion) {
    List<ApplicationAsset> assets = applicationAssetService.getSecondaryAssets(applicationVersion);
    Map<ApplicationAsset, List<ApplicationAssetLicence>> assetLicencesMap =
        applicationAssetLicenceService.getAssetLicencesMap(applicationVersion);
    return IntStream.range(0, assets.size())
        .mapToObj(index ->
            assetViewFrom(assets.get(index),
                index + 1,
                assetLicencesMap.get(assets.get(index))))
        .toList();
  }

  public AssetView getSummaryView(ApplicationAsset applicationAsset) {
    return assetViewFrom(applicationAsset, 1,
        applicationAssetLicenceService.getAssetLicences(applicationAsset));
  }

  private AssetView assetViewFrom(ApplicationAsset applicationAsset,
                                  Integer displayOrder,
                                  List<ApplicationAssetLicence> licences) {

    String deleteUrl = ReverseRouter.route(on(AdditionalAssetsController.class).deleteAssetConfirm(
        applicationAsset.getApplicationVersion().getApplication().getId(),
        applicationAsset.getAssetNo()
    ));

    // lookup the asset information (fallback to cached data)
    AssetJson assetJson = applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset);

    // lookup the asset operator information (fallback to cached data)
    OrganisationUnitJson assetOperator
        = organisationUnitService.getOrganisationUnitByIdOrFallback(
            applicationAsset.getAssetOperatorOuId(),
            "Organisation lookup for asset view information",
            applicationAsset.getCachedAssetOperatorName());

    return new AssetView(
        displayOrder,
        applicationAsset.getAssetNo(),
        assetJson.getName(),
        assetOperator.name(),
        // we are happy to always used cached information for the licence refs
        licences.stream().map(ApplicationAssetLicence::getCachedLicenceRef).collect(Collectors.joining(", ")),
        deleteUrl
    );
  }

  public List<SummaryCard> getAdditionalAssetsSummaryCards(ApplicationVersion applicationVersion) {
    var hasSecondaryAssetsOptional = applicationFlagService
        .findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS);

    if (hasSecondaryAssetsOptional.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    List<SummaryCard> summaryCards = new ArrayList<>();

    summaryCards.add(
        SummaryCard.simpleSummaryCard(
            List.of(SummaryKeyValue.fromBoolean(ApplicationFlagType.HAS_SECONDARY_ASSETS.getDisplayName(),
                hasSecondaryAssetsOptional.get()))
        )
    );

    getSummaryViews(applicationVersion)
        .stream()
        .map(assetView -> SummaryCard.simpleSummaryCardWithHeading(
            "Field " + assetView.displayOrder(),
            List.of(
                SummaryKeyValue.from("Field", assetView.assetName()),
                SummaryKeyValue.from("Field operator", assetView.assetOperatorName()),
                SummaryKeyValue.from("Licences", assetView.assetLicences())
            )
        ))
        .forEach(summaryCards::add);

    return summaryCards;
  }
}
