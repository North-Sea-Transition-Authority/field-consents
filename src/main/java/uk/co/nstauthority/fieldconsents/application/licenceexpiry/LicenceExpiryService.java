package uk.co.nstauthority.fieldconsents.application.licenceexpiry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.licences.LicenceJson;
import uk.co.nstauthority.fieldconsents.licences.LicenceJsonComparator;
import uk.co.nstauthority.fieldconsents.licences.LicenceView;

@Service
public class LicenceExpiryService {

  private static final String REQUEST_PURPOSE = "checking the asset licences for expiry date";
  private final ConsentLengthService consentLengthService;
  private final ApplicationAssetService applicationAssetService;
  private final FieldService fieldService;

  @Autowired
  public LicenceExpiryService(
      ConsentLengthService consentLengthService,
      ApplicationAssetService applicationAssetService,
      FieldService fieldService
  ) {
    this.consentLengthService = consentLengthService;
    this.applicationAssetService = applicationAssetService;
    this.fieldService = fieldService;
  }

  public List<LicenceView> getLicencesExpiringDuringConsentPeriod(ApplicationVersion applicationVersion) {
    var consentLengthDetails = consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetails.isEmpty()) {
      return Collections.emptyList();
    }

    var consentEnd = consentLengthService.getProposedConsentEndDate(consentLengthDetails.get());
    var fieldIds = applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(
            applicationVersion,
            List.of(AssetRole.PRIMARY, AssetRole.SECONDARY))
        .stream()
        .filter(ApplicationAsset::isField)
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();
    return fieldService.findFieldsWithOperatorAndLicences(fieldIds, REQUEST_PURPOSE)
        .stream()
        .flatMap(fieldWithOperatorAndLicencesJson -> fieldWithOperatorAndLicencesJson.getLicences().stream())
        .filter(licenceJson -> Objects.nonNull(licenceJson.scheduleExpiryDate())
            && licenceJson.scheduleExpiryDate().isBefore(consentEnd))
        .sorted(Comparator.comparing(LicenceJson::scheduleExpiryDate)
            .thenComparing(new LicenceJsonComparator()))
        .map(LicenceView::from)
        .distinct()
        .toList();
  }
}
