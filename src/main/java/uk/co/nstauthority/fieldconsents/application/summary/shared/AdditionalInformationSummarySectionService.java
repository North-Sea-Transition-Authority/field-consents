package uk.co.nstauthority.fieldconsents.application.summary.shared;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.Shore;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
public class AdditionalInformationSummarySectionService implements SummarySectionService<ApplicationVersion> {

  static final String FIELD_LOOKUP_PURPOSE = "Lookup field shore information for EIA screening direction summary item";

  private final SupportingInformationService supportingInformationService;

  private final ApplicationAssetService applicationAssetService;

  private final FieldService fieldService;

  private final EiaDirectionService eiaDirectionService;

  private final OtherLegacyDataSummaryService otherLegacyDataSummaryService;

  private final TeamService teamService;

  @Autowired
  AdditionalInformationSummarySectionService(SupportingInformationService supportingInformationService,
                                             ApplicationAssetService applicationAssetService,
                                             FieldService fieldService,
                                             EiaDirectionService eiaDirectionService,
                                             OtherLegacyDataSummaryService otherLegacyDataSummaryService,
                                             TeamService teamService) {
    this.supportingInformationService = supportingInformationService;
    this.applicationAssetService = applicationAssetService;
    this.fieldService = fieldService;
    this.eiaDirectionService = eiaDirectionService;
    this.otherLegacyDataSummaryService = otherLegacyDataSummaryService;
    this.teamService = teamService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion, ServiceUserDetail user) {

    var summaryItems = new ArrayList<SummaryItem>();

    var applicationType = applicationVersion.getApplication().getType();
    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);

    if (primaryAsset.isField() && ApplicationTypeFeature.EIA_SCREENING_DIRECTION.allowed(applicationType)) {
      var primaryFieldJson = fieldService.getField(primaryAsset.getAssetId(), FIELD_LOOKUP_PURPOSE);
      if (Shore.OFFSHORE.equals(primaryFieldJson.getShore())) {
        summaryItems.add(getEiaDirectionSummaryItem(applicationVersion));
      }
    }

    if (teamService.isRegulatorUser(user) || teamService.isIndustryUser(user)) {
      summaryItems.add(getSupportingInformationSummaryItem(applicationVersion));
    }

    getOtherLegacyDataSummaryItem(applicationVersion).ifPresent(summaryItems::add);

    return Optional.of(new SummarySection(30, summaryItems));
  }

  private SummaryItem getEiaDirectionSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withCard("EIA screening direction",
        eiaDirectionService.getEiaDirectionSummaryCard(applicationVersion)
    );
  }

  private SummaryItem getSupportingInformationSummaryItem(ApplicationVersion applicationVersion) {
    var summaryCards = supportingInformationService.getSupportingInformationSummaryCards(applicationVersion);
    return SummaryItem.withCards("Supporting information", summaryCards);
  }

  private Optional<SummaryItem> getOtherLegacyDataSummaryItem(ApplicationVersion applicationVersion) {
    return otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion)
        .map(otherLegacyData -> SummaryItem.withCard("Other legacy application details",
            otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData)));
  }
}
