package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getCaseEventForType;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getCaseEventViewForType;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getPortalUsersDtosMap;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationCaseEventService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNoteEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewCaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalCaseEventService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class CaseHistoryTabContentServiceTest {

  @Mock
  private CaseHistoryEventService caseHistoryEventService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private CaseNoteEventService caseNoteEventService;

  @Mock
  private WithdrawalCaseEventService withdrawalCaseEventService;

  @Mock
  private ApplicationCaseEventService applicationCaseEventService;

  @Mock
  private TechnicalReviewCaseEventService technicalReviewCaseEventService;

  @InjectMocks
  private CaseHistoryTabContentService caseHistoryTabContentService;

  private ApplicationVersion applicationVersion;

  private Application application;

  private Map<Long, EnergyPortalUserDto> portalUsersDtoMap;

  private CaseEvent applicationCreatedCaseEvent;

  private CaseEvent applicationSubmittedCaseEvent;

  private CaseEventView applicationCreatedCaseEventView;

  private CaseEventView applicationSubmittedCaseEventView;

  @BeforeEach
  void setUp() {
    portalUsersDtoMap = getPortalUsersDtosMap();
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(energyPortalUserService.findByWuaIds(anyList()))
        .thenReturn(portalUsersDtoMap.values().stream().toList());

    application = applicationVersion.getApplication();
    applicationCreatedCaseEvent = getCaseEventForType(applicationVersion, CaseEventType.APPLICATION_CREATED);
    applicationSubmittedCaseEvent = getCaseEventForType(applicationVersion, CaseEventType.APPLICATION_SUBMITTED);

    applicationCreatedCaseEventView = getCaseEventViewForType(CaseEventType.APPLICATION_CREATED);
    applicationSubmittedCaseEventView = getCaseEventViewForType(CaseEventType.APPLICATION_SUBMITTED);
    when(applicationCaseEventService.getCaseEventViewForApplication(applicationCreatedCaseEvent, portalUsersDtoMap))
        .thenReturn(applicationCreatedCaseEventView);
    when(applicationCaseEventService.getCaseEventViewForApplication(applicationSubmittedCaseEvent, portalUsersDtoMap))
        .thenReturn(applicationSubmittedCaseEventView);
  }

  @Test
  void getCaseHistoryTabContent_whenApplicationIsSubmitted() {
    var caseEvents = List.of(
        applicationSubmittedCaseEvent,
        applicationCreatedCaseEvent
    );

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(caseEvents);

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    assertThat(caseEventViews)
        .containsExactly(
            applicationSubmittedCaseEventView,
            applicationCreatedCaseEventView
        );
  }

  @Test
  void getCaseHistoryTabContent_whenCaseNoteAdded() {
    var caseNoteAddedCaseEvent = getCaseEventForType(applicationVersion, CaseEventType.CASE_NOTE_ADDED);
    var caseNoteAddedCaseEventView = getCaseEventViewForType(CaseEventType.CASE_NOTE_ADDED);
    when(caseNoteEventService.getCaseEventViewForNewCaseNote(caseNoteAddedCaseEvent, portalUsersDtoMap))
        .thenReturn(caseNoteAddedCaseEventView);

    var caseEvents = List.of(
        caseNoteAddedCaseEvent,
        applicationSubmittedCaseEvent,
        applicationCreatedCaseEvent
    );

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(caseEvents);

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    assertThat(caseEventViews)
        .containsExactly(
            caseNoteAddedCaseEventView,
            applicationSubmittedCaseEventView,
            applicationCreatedCaseEventView
        );
  }

  @Test
  void getCaseHistoryTabContent_whenWithdrawalRequested() {
    var withdrawalRequestedCaseEvent = getCaseEventForType(applicationVersion, CaseEventType.APPLICATION_WITHDRAWAL_REQUESTED);
    var withdrawalRequestedCaseEventView = getCaseEventViewForType(CaseEventType.APPLICATION_WITHDRAWAL_REQUESTED);
    when(withdrawalCaseEventService.getCaseEventViewForApplicationWithdrawal(withdrawalRequestedCaseEvent, portalUsersDtoMap))
        .thenReturn(withdrawalRequestedCaseEventView);

    var caseEvents = List.of(
        withdrawalRequestedCaseEvent,
        applicationSubmittedCaseEvent,
        applicationCreatedCaseEvent
    );

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(caseEvents);

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    assertThat(caseEventViews)
        .containsExactly(
            withdrawalRequestedCaseEventView,
            applicationSubmittedCaseEventView,
            applicationCreatedCaseEventView
        );
  }

  @Test
  void getCaseHistoryTabContent_whenWithdrawalResponded() {
    var withdrawalRespondedCaseEvent = getCaseEventForType(applicationVersion, CaseEventType.APPLICATION_WITHDRAWAL_RESPONDED);
    var withdrawalRespondedCaseEventView = getCaseEventViewForType(CaseEventType.APPLICATION_WITHDRAWAL_RESPONDED);
    when(withdrawalCaseEventService.getCaseEventViewForApplicationWithdrawal(withdrawalRespondedCaseEvent, portalUsersDtoMap))
        .thenReturn(withdrawalRespondedCaseEventView);

    var caseEvents = List.of(
        withdrawalRespondedCaseEvent,
        applicationSubmittedCaseEvent,
        applicationCreatedCaseEvent
    );

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(caseEvents);

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    assertThat(caseEventViews)
        .containsExactly(
            withdrawalRespondedCaseEventView,
            applicationSubmittedCaseEventView,
            applicationCreatedCaseEventView
        );
  }

  @Test
  void getCaseHistoryTabContent_whenTechnicalReviewRequested() {
    var technicalReviewRequestedCaseEvent = getCaseEventForType(applicationVersion, CaseEventType.TECHNICAL_REVIEW_REQUESTED);
    var technicalReviewRequestedCaseEventView = getCaseEventViewForType(CaseEventType.TECHNICAL_REVIEW_REQUESTED);
    when(technicalReviewCaseEventService.getCaseEventViewForTechnicalReviewRequest(technicalReviewRequestedCaseEvent, portalUsersDtoMap))
        .thenReturn(technicalReviewRequestedCaseEventView);

    var caseEvents = List.of(
        technicalReviewRequestedCaseEvent,
        applicationSubmittedCaseEvent,
        applicationCreatedCaseEvent
    );

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(caseEvents);

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    assertThat(caseEventViews)
        .containsExactly(
            technicalReviewRequestedCaseEventView,
            applicationSubmittedCaseEventView,
            applicationCreatedCaseEventView
        );
  }

  @Test
  void getCaseHistoryTabContent_whenTechnicalReviewCompleted() {
    var technicalReviewCompletedCaseEvent = getCaseEventForType(applicationVersion, CaseEventType.TECHNICAL_REVIEW_COMPLETED);
    var technicalReviewCompletedCaseEventView = getCaseEventViewForType(CaseEventType.TECHNICAL_REVIEW_COMPLETED);
    when(technicalReviewCaseEventService.getCaseEventViewForTechnicalReviewResponse(technicalReviewCompletedCaseEvent, portalUsersDtoMap))
        .thenReturn(technicalReviewCompletedCaseEventView);

    var caseEvents = List.of(
        technicalReviewCompletedCaseEvent,
        applicationSubmittedCaseEvent,
        applicationCreatedCaseEvent
    );

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(caseEvents);

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    assertThat(caseEventViews)
        .containsExactly(
            technicalReviewCompletedCaseEventView,
            applicationSubmittedCaseEventView,
            applicationCreatedCaseEventView
        );
  }
}
