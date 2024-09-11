package uk.co.nstauthority.fieldconsents.application.summary.shared;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceApi;
import uk.co.fivium.energyportalapi.generated.client.LicencesProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Licence;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@Service
public class LicenceDetailsSummaryCardService {

  private static final LicencesProjectionRoot QUERY = new LicencesProjectionRoot().id().licenceRef().scheduleExpiryDate();

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose(
      "Looking up licence details for application summary");

  private final ApplicationAssetLicenceService applicationAssetLicenceService;
  private final LicenceApi licenceApi;

  LicenceDetailsSummaryCardService(
      ApplicationAssetLicenceService applicationAssetLicenceService,
      LicenceApi licenceApi
  ) {
    this.applicationAssetLicenceService = applicationAssetLicenceService;
    this.licenceApi = licenceApi;
  }

  public Optional<SummaryCard> getSummaryCard(ApplicationVersion applicationVersion) {
    var assetLicences = applicationAssetLicenceService.getAssetLicences(applicationVersion);
    if (assetLicences.isEmpty()) {
      return Optional.empty();
    }

    var primaryAsset = assetLicences.stream()
        .map(ApplicationAssetLicence::getApplicationAsset)
        .filter(ApplicationAsset::isPrimary)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("primary asset not found for application %s"
            .formatted(applicationVersion.getApplication().getId())));

    if (!primaryAsset.isField()) {
      return Optional.empty();
    }

    var tableSummary = SummaryTableView.newWithHeading(
        "Licence reference",
        "Field name(s)",
        "Scheduled end date"
    );

    var fieldNamesByLicenceIds = assetLicences.stream()
        .collect(Collectors.groupingBy(
            ApplicationAssetLicence::getLicenceId,
            Collectors.mapping(
                applicationAssetLicence -> applicationAssetLicence.getApplicationAsset().getCachedAssetName(),
                Collectors.toList()
            )
        ));

    var licenceIds = fieldNamesByLicenceIds.keySet().stream().toList();
    licenceApi.searchLicencesById(licenceIds, QUERY, REQUEST_PURPOSE)
        .stream()
        .sorted(
            // sort by the field name that the licence is for first
            Comparator.comparing((Licence licence) -> fieldNamesByLicenceIds.get(licence.getId()).getFirst(), String::compareTo)
            .thenComparing(Licence::getLicenceRef) // TODO: FCS-616 sort the licences properly
        )
        .forEach(licence -> tableSummary.addRow(
            licence.getLicenceRef(),
            StringUtil.formatStringList(fieldNamesByLicenceIds.get(licence.getId()).stream().distinct().toList()),
            Optional.ofNullable(licence.getScheduleExpiryDate())
                .map(date -> DateUtils.format(date, DateUtils.LONG_DATE))
                .orElse("None")
        ));

    return Optional.of(SummaryCard.tableSummaryCard(tableSummary));
  }

}
