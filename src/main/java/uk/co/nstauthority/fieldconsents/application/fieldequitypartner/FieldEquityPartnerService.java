package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@Service
public class FieldEquityPartnerService {

  private static final RequestPurpose FIELD_EQUITY_PARTNER_LOOKUP_REQUEST_PURPOSE =
      new RequestPurpose("Consent data field equity partner names lookup");

  private final ApplicationAssetService applicationAssetService;
  private final FieldApi fieldApi;

  FieldEquityPartnerService(
      ApplicationAssetService applicationAssetService,
      FieldApi fieldApi
  ) {
    this.applicationAssetService = applicationAssetService;
    this.fieldApi = fieldApi;
  }

  public FieldEquityPartnersView getFieldEquityPartnersView(ApplicationVersion applicationVersion) {
    var fieldEquityPartnerNames = getFieldEquityPartnerNames(applicationVersion);

    return new FieldEquityPartnersView(fieldEquityPartnerNames);
  }

  public List<String> getFieldEquityPartnerNames(ApplicationVersion applicationVersion) {
    var fieldApplicationAssets = applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
        applicationVersion,
        AssetType.FIELD,
        EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
    );

    if (fieldApplicationAssets.stream().noneMatch(ApplicationAsset::isPrimary)) {
      throw new IllegalStateException("Unable to find a primary field application asset for application version [%s]"
          .formatted(applicationVersion.getId()));
    }

    var fieldIds = fieldApplicationAssets.stream()
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    var query = new FieldsProjectionRoot()
        .fieldEquityPartners()
          .organisationUnit()
            .name()
        .root();

    return fieldApi.getFieldsByIds(fieldIds, query, FIELD_EQUITY_PARTNER_LOOKUP_REQUEST_PURPOSE)
        .stream()
        .flatMap(field -> field.getFieldEquityPartners().stream())
        .map(fieldEquityPartner -> fieldEquityPartner.getOrganisationUnit().getName())
        .distinct()
        .sorted()
        .toList();
  }

}
