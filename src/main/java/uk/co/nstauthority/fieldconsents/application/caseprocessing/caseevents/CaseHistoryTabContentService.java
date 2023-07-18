package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationCaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNoteEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewCaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalCaseEventService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class CaseHistoryTabContentService {

  private final CaseHistoryEventService caseHistoryEventService;
  private final EnergyPortalUserService energyPortalUserService;
  private final CaseNoteEventService caseNoteEventService;
  private final WithdrawalCaseEventService withdrawalCaseEventService;
  private final ApplicationCaseEventService applicationCaseEventService;
  private final TechnicalReviewCaseEventService technicalReviewCaseEventService;

  public CaseHistoryTabContentService(CaseHistoryEventService caseHistoryEventService,
                                      EnergyPortalUserService energyPortalUserService,
                                      CaseNoteEventService caseNoteEventService,
                                      WithdrawalCaseEventService withdrawalCaseEventService,
                                      ApplicationCaseEventService applicationCaseEventService,
                                      TechnicalReviewCaseEventService technicalReviewCaseEventService) {
    this.caseHistoryEventService = caseHistoryEventService;
    this.energyPortalUserService = energyPortalUserService;
    this.caseNoteEventService = caseNoteEventService;
    this.withdrawalCaseEventService = withdrawalCaseEventService;
    this.applicationCaseEventService = applicationCaseEventService;
    this.technicalReviewCaseEventService = technicalReviewCaseEventService;
  }

  public List<CaseEventView> getCaseHistoryTabContent(Application application) {

    var mainEventUserWuaIdsStream = caseHistoryEventService.getCaseHistoryEvents(application)
        .stream()
        .map(CaseEvent::mainEventUserWuaId)
        .map(WebUserAccountId::new);

    var otherEventUserWuaIdsStream = caseHistoryEventService.getCaseHistoryEvents(application)
        .stream()
        .map(CaseEvent::otherEventUserWuaId)
        .filter(Objects::nonNull)
        .map(WebUserAccountId::new);

    var portalUserDtosMap =
        energyPortalUserService.findByWuaIds(
                Stream.concat(mainEventUserWuaIdsStream, otherEventUserWuaIdsStream)
                    .distinct()
                    .toList()
            )
            .stream()
            .collect(Collectors.toMap(
                EnergyPortalUserDto::webUserAccountId,
                Function.identity())
            );

    return caseHistoryEventService.getCaseHistoryEvents(application)
        .stream()
        .map(historyEvent -> getCaseEventView(historyEvent, portalUserDtosMap))
        .toList();
  }

  private CaseEventView getCaseEventView(CaseEvent caseEvent, Map<Long, EnergyPortalUserDto> portalUserDtosMap) {

    CaseEventView caseEventView = new CaseEventView();

    switch (caseEvent.eventType()) {
      case
          APPLICATION_CREATED,
          APPLICATION_SUBMITTED ->
          caseEventView = applicationCaseEventService.getCaseEventViewForApplication(caseEvent, portalUserDtosMap);

      case
          APPLICATION_WITHDRAWAL_REQUESTED,
          APPLICATION_WITHDRAWAL_RESPONDED ->
          caseEventView = withdrawalCaseEventService.getCaseEventViewForApplicationWithdrawal(caseEvent, portalUserDtosMap);

      // TODO: Include CASE_OFFICER_ASSIGNED once the assignment date time has been resolved on FCS-397

      case CASE_NOTE_ADDED ->
        // TODO: This needs to include the list of uploaded files retrieved on FCS-397
        caseEventView = caseNoteEventService.getCaseEventViewForNewCaseNote(caseEvent, portalUserDtosMap);

      case TECHNICAL_REVIEW_REQUESTED ->
        caseEventView = technicalReviewCaseEventService.getCaseEventViewForTechnicalReviewRequest(caseEvent, portalUserDtosMap);

      case TECHNICAL_REVIEW_COMPLETED ->
        caseEventView = technicalReviewCaseEventService.getCaseEventViewForTechnicalReviewResponse(caseEvent, portalUserDtosMap);

      default -> {
        return caseEventView;
      }
    }

    return caseEventView;
  }
}
