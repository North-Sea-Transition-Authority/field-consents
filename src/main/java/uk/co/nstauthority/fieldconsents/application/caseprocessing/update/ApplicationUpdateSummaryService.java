package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;

@Service
public class ApplicationUpdateSummaryService {

  private final ApplicationUpdateService applicationUpdateService;
  private final EnergyPortalUserService energyPortalUserService;

  ApplicationUpdateSummaryService(
      ApplicationUpdateService applicationUpdateService,
      EnergyPortalUserService energyPortalUserService
  ) {
    this.applicationUpdateService = applicationUpdateService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public List<SummaryItem> getApplicationUpdateSummaryItems(Application application) {
    var applicationUpdates = applicationUpdateService.getApplicationUpdatesByApplication(application)
        .stream()
        .sorted(Comparator.comparing(ApplicationUpdate::getRequestedDateTime).reversed())
        .toList();

    if (applicationUpdates.isEmpty()) {
      return Collections.emptyList();
    }

    var wuaIds = applicationUpdates
        .stream()
        .flatMap(applicationUpdate -> Stream.of(
            applicationUpdate.getRequestedByWuaId(),
            applicationUpdate.getRespondedByWuaId()
        ))
        .filter(Objects::nonNull)
        .map(WebUserAccountId::from)
        .collect(Collectors.toSet());

    var energyPortalUserByWuaId = energyPortalUserService.getEnergyPortalUserMap(wuaIds)
        .entrySet()
        .stream()
        .collect(Collectors.toMap(entry -> entry.getKey().id(), Map.Entry::getValue));

    if (energyPortalUserByWuaId.size() != wuaIds.size()) {
      throw new IllegalStateException("Fetched %d energy portal users but needed %d to complete successfully"
          .formatted(energyPortalUserByWuaId.size(), wuaIds.size()));
    }

    var summaryItems = new ArrayList<SummaryItem>();
    for (var i = 0; i < applicationUpdates.size(); i++) {
      var applicationUpdate = applicationUpdates.get(i);
      var heading = "Application update %d".formatted(applicationUpdates.size() - i);
      var summaryItem = getSummaryItem(heading, applicationUpdate, energyPortalUserByWuaId);
      summaryItems.add(summaryItem);
    }

    return summaryItems;
  }

  SummaryItem getSummaryItem(
      String heading,
      ApplicationUpdate applicationUpdate,
      Map<Long, EnergyPortalUserDto> energyPortalUserByWuaId
  ) {
    var card = getSummaryCard(applicationUpdate, energyPortalUserByWuaId);
    return SummaryItem.withCard(heading, card);
  }

  SummaryCard getSummaryCard(
      ApplicationUpdate applicationUpdate,
      Map<Long, EnergyPortalUserDto> energyPortalUserByWuaId
  ) {
    var updateStatus = applicationUpdate.getApplicationUpdateStatus();
    var summaryDataView = SummaryDataView
        .newWithKeyValue("Update status", updateStatus.getDisplayName())
        .addKeyValue("Request application version", applicationUpdate.getApplicationVersion().getVersion())
        .addKeyValue("Requested by", energyPortalUserByWuaId.get(applicationUpdate.getRequestedByWuaId()).displayName())
        .addKeyValue("Requested on", DateUtils.format(applicationUpdate.getRequestedDateTime(), DateUtils.DATE_TIME))
        .addKeyValue("Request details", applicationUpdate.getRequestText())
        .addKeyValue("Deadline", DateUtils.format(applicationUpdate.getDeadlineDateTime(), DateUtils.DATE_TIME));

    if (!ApplicationUpdateStatus.CLOSED.equals(updateStatus)) {
      return SummaryCard.simpleSummaryCard(summaryDataView);
    }

    var responseType = applicationUpdate.getResponseType();

    summaryDataView
        .addKeyValue("Response application version", applicationUpdate.getResponseApplicationVersion().getVersion())
        .addKeyValue("Responded by", energyPortalUserByWuaId.get(applicationUpdate.getRespondedByWuaId()).displayName())
        .addKeyValue("Responded on", DateUtils.format(applicationUpdate.getRespondedDateTime(), DateUtils.DATE_TIME))
        // null check to cope with migrated data
        .addKeyValue("Update type",
            Optional.ofNullable(responseType)
                .map(ApplicationUpdateResponseType::getDisplayName)
                .orElse(null)
        );

    if (ApplicationUpdateResponseType.OTHER_CHANGES.equals(responseType)) {
      summaryDataView.addKeyValue("Other changes description", applicationUpdate.getResponseText());
    }

    return SummaryCard.simpleSummaryCard(summaryDataView);
  }

}
