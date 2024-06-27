package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getPortalUsersDtosMap;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.DRAFT_APPLICATION_UPDATE_DELETED;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class CaseHistoryTabContentServiceTest {

  @Mock
  private CaseHistoryEventService caseHistoryEventService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private CaseHistoryTabContentService caseHistoryTabContentService;

  private ApplicationVersion applicationVersion;

  private Application application;

  @BeforeEach
  void setUp() {
    var portalUsersDtoMap = getPortalUsersDtosMap();
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(energyPortalUserService.getEnergyPortalUserMap(anyList()))
        .thenReturn(portalUsersDtoMap);

    application = applicationVersion.getApplication();
  }

  @Test
  void getCaseHistoryTabContent() {
    var caseEvents = List.of(
        CaseEvent
            .builder(applicationVersion)
            .withEventType(CaseEventType.TECHNICAL_REVIEW_REQUESTED)
            .withEventDateTime(Instant.now())
            .withEventText("Testing")
            .withMainEventUserWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_1.webUserAccountId())
            .withOtherEventUserWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_2.webUserAccountId())
            .build(),
        CaseEvent.builder(applicationVersion)
            .withEventType(CaseEventType.CASE_NOTE_ADDED)
            .withEventDateTime(Instant.now().minus(1, ChronoUnit.DAYS))
            .withMainEventUserWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_3.webUserAccountId())
            .build()
    );

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(caseEvents);

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    assertThat(caseEventViews)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            new CaseEventView(
                "Technical review requested", "Requested by",
                AssignmentTestUtil.ENERGY_PORTAL_USER_1.displayName(), "Technical reviewer",
                AssignmentTestUtil.ENERGY_PORTAL_USER_2.displayName(), "Requested on",
                DateUtils.format(caseEvents.get(0).eventDateTime(), DateUtils.DATE_TIME), "1", "Request details",
                "Testing",Collections.emptyList()),
            new CaseEventView(
                "Case note added", "Added by",
                AssignmentTestUtil.ENERGY_PORTAL_USER_3.displayName(), null,
                null,  "Added on",
                DateUtils.format(caseEvents.get(1).eventDateTime(), DateUtils.DATE_TIME), "1", "Case note",
                null, Collections.emptyList())
        );
  }

  // This test is to cover the scenario of deleted case events for migrated data where the main user event is not present
  @Test
  void getCaseHistoryTabContent_whenNoMainUserEvent() {
    var applicationDeletedCaseEvent =
        CaseEvent
            .builder(applicationVersion)
            .withEventType(DRAFT_APPLICATION_UPDATE_DELETED)
            .withEventDateTime(Instant.now())
            .build();

    when(caseHistoryEventService.getCaseHistoryEvents(application)).thenReturn(List.of(applicationDeletedCaseEvent));

    List<CaseEventView> caseEventViews = caseHistoryTabContentService.getCaseHistoryTabContent(application);
    var actualDeletedCaseEvent = caseEventViews.getFirst();
    assertThat(actualDeletedCaseEvent)
        .extracting(
            CaseEventView::getHeaderText,
            CaseEventView::getMainUserInvolvedLabel,
            CaseEventView::getMainUserInvolvedFullName,
            CaseEventView::getEventDateTimeLabel,
            CaseEventView::getEventDateTimeText
        ).containsExactly(
            "Draft application update deleted",
            "Deleted by",
            null,
            "Deleted on",
            DateUtils.format(applicationDeletedCaseEvent.eventDateTime(), DateUtils.DATE_TIME)
        );
  }
}
