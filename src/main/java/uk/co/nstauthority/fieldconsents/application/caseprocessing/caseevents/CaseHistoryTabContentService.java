package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class CaseHistoryTabContentService {

  private final CaseHistoryEventService caseHistoryEventService;
  private final EnergyPortalUserService energyPortalUserService;

  public CaseHistoryTabContentService(CaseHistoryEventService caseHistoryEventService,
                                      EnergyPortalUserService energyPortalUserService) {
    this.caseHistoryEventService = caseHistoryEventService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public List<CaseEventView> getCaseHistoryTabContent(Application application) {

    var allEvents = caseHistoryEventService.getCaseHistoryEvents(application);

    var allUsers = allEvents.stream()
        .flatMap(e -> Stream.of(
            Optional.ofNullable(e.mainEventUserWuaId()), Optional.ofNullable(e.otherEventUserWuaId()))
        )
        .flatMap(Optional::stream)
        .distinct()
        .map(WebUserAccountId::new)
        .toList();

    var portalUserDtosMap = energyPortalUserService.findByWuaIds(allUsers).stream()
        .collect(Collectors.toMap(
            EnergyPortalUserDto::webUserAccountId,
            Function.identity())
        );

    return allEvents
        .stream()
        .map(historyEvent -> convertToCaseEventView(historyEvent, portalUserDtosMap))
        .toList();
  }

  private CaseEventView convertToCaseEventView(CaseEvent caseEvent, Map<Long, EnergyPortalUserDto> portalUserDtosMap) {
    var view = CaseEventView.builder()
        .withApplicationVersion(caseEvent.applicationVersion())
        .withEventType(caseEvent.eventType())
        .withEventText(caseEvent.eventText())
        .withMainUser(portalUserDtosMap.get(caseEvent.mainEventUserWuaId()))
        .withEventDateTime(caseEvent.eventDateTime())
        .withFileSummaryViews(caseEvent.summaryFileViews());

    if (caseEvent.otherEventUserWuaId() != null) {
      view.withOtherUser(portalUserDtosMap.get(caseEvent.otherEventUserWuaId()));
    }

    return view.build();
  }
}
