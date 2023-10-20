package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus.OPEN;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;

@ExtendWith(MockitoExtension.class)
class FurtherInformationEventServiceTest {

  private static final int FURTHER_INFORMATION_REQUEST_ID = 1;
  private static final String FURTHER_INFORMATION_REQUEST_TEXT = "request text";
  private static final long REQUESTING_USER_WUA_ID = 1L;

  private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  @Mock
  private ConsultationService consultationService;

  @Mock
  private FurtherInformationService furtherInformationService;

  @Spy
  @InjectMocks
  private FurtherInformationEventService furtherInformationEventService;

  private ApplicationVersion applicationVersion;

  private Application application;

  private Consultation consultation;
  private FurtherInformation furtherInformation;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    application = applicationVersion.getApplication();

    consultation = new Consultation();
    consultation.setId(1);
    consultation.setRequestApplicationVersion(applicationVersion);

    furtherInformation = new FurtherInformation();
    furtherInformation.setId(FURTHER_INFORMATION_REQUEST_ID);
    furtherInformation.setRequestText(FURTHER_INFORMATION_REQUEST_TEXT);
    furtherInformation.setRequestedAtDatetime(NOW);
    furtherInformation.setRequestedByWuaId(REQUESTING_USER_WUA_ID);
    furtherInformation.setConsultation(consultation);
    furtherInformation.setStatus(OPEN);
  }

  @Test
  void getCaseEvents() {
    var consultations = Collections.singletonList(consultation);

    when(consultationService.getConsultationsByApplication(application)).thenReturn(consultations);
    when(furtherInformationService.getAllFurtherInformation(consultations)).thenReturn(Collections.singletonList(furtherInformation));

    var caseEvent = mock(CaseEvent.class);
    doReturn(Optional.of(caseEvent)).when(furtherInformationEventService).getRequestedCaseEvent(furtherInformation);

    assertThat(furtherInformationEventService.getCaseEvents(application)).containsExactly(caseEvent);
  }

  @Test
  void getCaseEvents_noFurtherInformationFound() {
    var consultations = Collections.singletonList(consultation);

    when(consultationService.getConsultationsByApplication(application)).thenReturn(consultations);
    when(furtherInformationService.getAllFurtherInformation(consultations)).thenReturn(Collections.emptyList());

    assertThat(furtherInformationEventService.getCaseEvents(application)).isEmpty();
  }

  @Test
  void getRequestedCaseEvent() {
    assertThat(furtherInformationEventService.getRequestedCaseEvent(furtherInformation))
        .isPresent()
        .get()
        .extracting(
            CaseEvent::applicationVersion,
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::eventDateTime,
            CaseEvent::eventText
        ).containsExactly(
            applicationVersion,
            CaseEventType.FURTHER_INFORMATION_REQUEST_OPENED,
            REQUESTING_USER_WUA_ID,
            NOW,
            FURTHER_INFORMATION_REQUEST_TEXT
        );
  }

  @Test
  void getRequestedCaseEvent_requestedAtDateTimeIsNull() {
    furtherInformation.setRequestedAtDatetime(null);
    assertThat(furtherInformationEventService.getRequestedCaseEvent(furtherInformation)).isEmpty();
  }

  @Test
  void getRequestedCaseEvent_requestedByWuaIdIsNull() {
    furtherInformation.setRequestedByWuaId(null);
    assertThat(furtherInformationEventService.getRequestedCaseEvent(furtherInformation)).isEmpty();
  }

}
