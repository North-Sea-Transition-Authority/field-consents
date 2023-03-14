package uk.co.nstauthority.fieldconsents.application.summary.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.generated.types.FieldShore;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class AdditionalInformationSummarySectionService implements SummarySectionService<ApplicationVersion> {

  static final String FIELD_LOOKUP_PURPOSE = "Lookup field shore information for EIA screening direction summary item";

  private final SupportingInformationService supportingInformationService;

  private final ApplicationAssetService applicationAssetService;

  private final FieldService fieldService;

  private final EiaDirectionService eiaDirectionService;

  @Autowired
  AdditionalInformationSummarySectionService(SupportingInformationService supportingInformationService,
                                             ApplicationAssetService applicationAssetService,
                                             FieldService fieldService,
                                             EiaDirectionService eiaDirectionService) {
    this.supportingInformationService = supportingInformationService;
    this.applicationAssetService = applicationAssetService;
    this.fieldService = fieldService;
    this.eiaDirectionService = eiaDirectionService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion) {
    List<SummaryItem> summaryItems = new ArrayList<>();

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);

    if (primaryAsset.isField()) {
      var primaryFieldJson = fieldService.getField(primaryAsset.getFieldId(), FIELD_LOOKUP_PURPOSE);
      if (FieldShore.OFFSHORE.equals(primaryFieldJson.getShoreJson().shore())) {
        summaryItems.add(getEiaDirectionSummaryItem(applicationVersion));
      }
    }

    summaryItems.add(getSupportingInformationSummaryItem(applicationVersion));

    return Optional.of(new SummarySection(30, summaryItems));
  }

  private SummaryItem getEiaDirectionSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withGroup("EIA screening direction",
        eiaDirectionService.getEiaDirectionSummaryGroup(applicationVersion)
    );
  }

  private SummaryItem getSupportingInformationSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withGroup("Supporting information",
        supportingInformationService.getSupportingInformationSummaryGroup(applicationVersion)
    );
  }
}
