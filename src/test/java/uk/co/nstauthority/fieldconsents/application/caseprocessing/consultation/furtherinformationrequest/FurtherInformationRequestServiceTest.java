package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequestStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.FURTHER_INFORMATION_REQUEST_OPENED;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class FurtherInformationRequestServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().withWuaId(1L).build();
  private static final String REQUEST_TEXT = "request text";

  private static final Instant NOW = Instant.now();

  @Mock(strictness = LENIENT)
  private Clock clock;

  @Mock
  private FurtherInformationRequestRepository repository;

  @Mock
  private ApplicationWorkAreaPriorityService priorityService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private FurtherInformationRequestService furtherInformationRequestService;

  @Captor
  private ArgumentCaptor<FurtherInformationRequest> furtherInformationRequestArgumentCaptor;

  private FurtherInformationRequest furtherInformationRequest;
  private Consultation consultation;
  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    when(clock.instant()).thenReturn(NOW);

    furtherInformationRequest = new FurtherInformationRequest();
    furtherInformationRequest.setRequestedByWuaId(USER.wuaId());
    furtherInformationRequest.setRequestedAtDatetime(NOW);
    furtherInformationRequest.setRequestText(REQUEST_TEXT);

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    consultation = new Consultation();
    consultation.setRequestApplicationVersion(applicationVersion);
  }

  @Test
  void findLatestOpenFurtherInformationRequest() {
    when(repository.findByConsultationAndStatus(consultation, OPEN)).thenReturn(Optional.of(furtherInformationRequest));
    assertThat(furtherInformationRequestService.findLatestOpenFurtherInformationRequest(consultation)).contains(furtherInformationRequest);
  }

  @Test
  void findLatestOpenFurtherInformationRequest_whenDoesNotExist() {
    when(repository.findByConsultationAndStatus(consultation, OPEN)).thenReturn(Optional.empty());
    assertThat(furtherInformationRequestService.findLatestOpenFurtherInformationRequest(consultation)).isEmpty();
  }

  @Test
  void getAllFurtherInformationRequests() {
    var consultations = Collections.singletonList(consultation);
    var furtherInformationRequests = IntStream.range(0, 5).mapToObj(i -> new FurtherInformationRequest()).toList();
    when(repository.findAllByConsultationInOrderById(consultations)).thenReturn(furtherInformationRequests);
    assertThat(furtherInformationRequestService.getAllFurtherInformationRequests(consultations)).isEqualTo(furtherInformationRequests);
  }

  @Test
  void saveFurtherInformationRequest() {
    furtherInformationRequestService.saveFurtherInformationRequest(consultation, USER, REQUEST_TEXT);
    verify(priorityService).prioritiseApplicationInWorkArea(applicationVersion, USER, FURTHER_INFORMATION_REQUEST_OPENED, REGULATOR);
    verify(priorityService).prioritiseApplicationInWorkArea(applicationVersion, USER, FURTHER_INFORMATION_REQUEST_OPENED, CONSULTEE);

    verify(repository).save(furtherInformationRequestArgumentCaptor.capture());
    assertThat(furtherInformationRequestArgumentCaptor.getValue())
        .extracting(
            FurtherInformationRequest::getStatus,
            FurtherInformationRequest::getConsultation,
            FurtherInformationRequest::getRequestedAtDatetime,
            FurtherInformationRequest::getRequestedByWuaId,
            FurtherInformationRequest::getRequestText
        ).containsExactly(
            FurtherInformationRequestStatus.OPEN,
            consultation,
            NOW,
            USER.wuaId(),
            REQUEST_TEXT
        );
  }

  @Test
  void getFurtherInformationRequestView() {
    var requestedByUser = "Example user";
    var energyPortalUser = mock(EnergyPortalUserDto.class);
    when(energyPortalUser.displayName()).thenReturn(requestedByUser);

    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(USER.wuaId()))).thenReturn(energyPortalUser);

    assertThat(furtherInformationRequestService.getFurtherInformationRequestView(furtherInformationRequest))
        .extracting(
            FurtherInformationRequestView::requestedAtTimestamp,
            FurtherInformationRequestView::requestedByUser,
            FurtherInformationRequestView::requestText
        ).containsExactly(
            DateUtils.format(NOW, DateUtils.DATE_TIME),
            requestedByUser,
            REQUEST_TEXT
        );
  }
}
