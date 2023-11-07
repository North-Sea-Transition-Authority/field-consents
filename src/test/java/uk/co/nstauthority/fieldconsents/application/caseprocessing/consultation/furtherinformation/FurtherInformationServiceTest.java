package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_FURTHER_INFORMATION_REQUESTED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_FURTHER_INFORMATION_RESPONDED;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
class FurtherInformationServiceTest {

  private static final int CONSULTATION_ID = 1;
  private static final int FURTHER_INFORMATION_ID = 10;
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().withWuaId(1L).build();
  private static final String REQUEST_TEXT = "request text";
  private static final String RESPONSE_TEXT = "response text";

  private static final Instant NOW = Instant.now();

  @Mock(strictness = LENIENT)
  private Clock clock;

  @Mock
  private FurtherInformationRepository repository;

  @Mock
  private ApplicationWorkAreaPriorityService priorityService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Spy
  @InjectMocks
  private FurtherInformationService furtherInformationService;

  @Captor
  private ArgumentCaptor<FurtherInformation> furtherInformationArgumentCaptor;

  private ApplicationVersion applicationVersion;
  private Consultation consultation;
  private FurtherInformation furtherInformation;

  @BeforeEach
  void setUp() {
    when(clock.instant()).thenReturn(NOW);

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);
    consultation.setRequestApplicationVersion(applicationVersion);

    furtherInformation = new FurtherInformation();
    furtherInformation.setStatus(OPEN);
    furtherInformation.setId(FURTHER_INFORMATION_ID);
    furtherInformation.setRequestedByWuaId(USER.wuaId());
    furtherInformation.setRequestedAtDatetime(NOW);
    furtherInformation.setRequestText(REQUEST_TEXT);
    furtherInformation.setConsultation(consultation);
  }

  @Test
  void findLatestOpenFurtherInformation() {
    when(repository.findByConsultationAndStatus(consultation, OPEN)).thenReturn(Optional.of(furtherInformation));
    assertThat(furtherInformationService.findLatestOpenFurtherInformation(consultation)).contains(furtherInformation);
  }

  @Test
  void findLatestOpenFurtherInformation_whenDoesNotExist() {
    when(repository.findByConsultationAndStatus(consultation, OPEN)).thenReturn(Optional.empty());
    assertThat(furtherInformationService.findLatestOpenFurtherInformation(consultation)).isEmpty();
  }

  @Test
  void getAllFurtherInformation() {
    var consultations = Collections.singletonList(consultation);
    var furtherInformationList = IntStream.range(0, 5).mapToObj(i -> new FurtherInformation()).toList();
    when(repository.findAllByConsultationInOrderById(consultations)).thenReturn(furtherInformationList);
    assertThat(furtherInformationService.getAllFurtherInformation(consultations)).isEqualTo(furtherInformationList);
  }

  @Test
  void getLatestOpenFurtherInformation() {
    when(repository.findByConsultationAndStatus(consultation, OPEN)).thenReturn(Optional.of(furtherInformation));
    assertThat(furtherInformationService.getLatestOpenFurtherInformation(consultation)).isEqualTo(furtherInformation);
  }

  @Test
  void getLatestOpenFurtherInformation_whenDoesNotExist() {
    when(repository.findByConsultationAndStatus(consultation, OPEN)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> furtherInformationService.getLatestOpenFurtherInformation(consultation))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Open further information not found for consultation [%s]".formatted(CONSULTATION_ID));
  }

  @Test
  void saveFurtherInformation() {
    furtherInformationService.saveFurtherInformationRequest(consultation, USER, REQUEST_TEXT);
    verify(priorityService).prioritiseApplicationInWorkArea(applicationVersion, USER,
        CONSULTATION_FURTHER_INFORMATION_REQUESTED, REGULATOR);
    verify(priorityService).prioritiseApplicationInWorkArea(applicationVersion, USER,
        CONSULTATION_FURTHER_INFORMATION_REQUESTED, CONSULTEE);

    verify(repository).save(furtherInformationArgumentCaptor.capture());
    assertThat(furtherInformationArgumentCaptor.getValue())
        .extracting(
            FurtherInformation::getStatus,
            FurtherInformation::getConsultation,
            FurtherInformation::getRequestedAtDatetime,
            FurtherInformation::getRequestedByWuaId,
            FurtherInformation::getRequestText
        ).containsExactly(
            FurtherInformationStatus.OPEN,
            consultation,
            NOW,
            USER.wuaId(),
            REQUEST_TEXT
        );
  }

  @Test
  void saveFurtherInformationResponse() {
    furtherInformationService.saveFurtherInformationResponse(furtherInformation, USER, RESPONSE_TEXT);

    verify(priorityService).prioritiseApplicationInWorkArea(applicationVersion, USER,
        CONSULTATION_FURTHER_INFORMATION_RESPONDED, REGULATOR);
    verify(priorityService).prioritiseApplicationInWorkArea(applicationVersion, USER,
        CONSULTATION_FURTHER_INFORMATION_RESPONDED, CONSULTEE);

    verify(repository).save(furtherInformationArgumentCaptor.capture());
    assertThat(furtherInformationArgumentCaptor.getValue())
        .extracting(
            FurtherInformation::getStatus,
            FurtherInformation::getRespondedAtDatetime,
            FurtherInformation::getRespondedByWuaId,
            FurtherInformation::getResponseText
        ).containsExactly(
            CLOSED,
            NOW,
            USER.wuaId(),
            RESPONSE_TEXT
        );
  }

  @ParameterizedTest
  @EnumSource(value = FurtherInformationStatus.class, names = "OPEN", mode = Mode.EXCLUDE)
  void saveFurtherInformationResponse_whenNotOpen(FurtherInformationStatus status) {
    var furtherInformation = mock(FurtherInformation.class);
    when(furtherInformation.getId()).thenReturn(FURTHER_INFORMATION_ID);
    when(furtherInformation.getStatus()).thenReturn(status);

    assertThatThrownBy(() -> furtherInformationService.saveFurtherInformationResponse(furtherInformation, USER, RESPONSE_TEXT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expected further information [%s] to be OPEN when saving response".formatted(FURTHER_INFORMATION_ID));

    verify(furtherInformation).getStatus();
    verify(furtherInformation).getId();
    verifyNoMoreInteractions(furtherInformation);

    verify(repository, never()).save(any());
    verify(priorityService, never()).prioritiseApplicationInWorkArea(any(), any(), any(), any());
  }

  @Test
  void getFurtherInformationView() {
    var view = mock(FurtherInformationView.class);
    doReturn(Collections.singletonList(view))
        .when(furtherInformationService)
        .getFurtherInformationViews(Collections.singleton(furtherInformation));

    assertThat(furtherInformationService.getFurtherInformationView(furtherInformation)).isEqualTo(view);
  }

  @Test
  void getFurtherInformationViews() {
    var energyPortalUser = mock(EnergyPortalUserDto.class);
    var furtherInformationViews = Collections.<FurtherInformationView>emptyList();

    doReturn(furtherInformationViews)
        .when(furtherInformationService)
        .getFurtherInformationViews(Collections.singleton(furtherInformation), Map.of(USER.wuaId(), energyPortalUser));

    when(energyPortalUserService.getEnergyPortalUserMap(Collections.singleton(WebUserAccountId.from(USER.wuaId()))))
        .thenReturn(Map.of(WebUserAccountId.from(USER.wuaId()), energyPortalUser));

    assertThat(furtherInformationService.getFurtherInformationViews(Collections.singleton(furtherInformation)))
        .containsExactlyElementsOf(furtherInformationViews);
  }

  @Test
  void getFurtherInformationViews_withProvidedEnergyPortalUsers() {
    var furtherInformationList = List.of(
        createFurtherInformation(1L, NOW, REQUEST_TEXT, OPEN, null, null, null),
        createFurtherInformation(1L, NOW, REQUEST_TEXT, CLOSED, 2L, NOW, RESPONSE_TEXT),
        createFurtherInformation(2L, NOW, REQUEST_TEXT, CLOSED, 1L, NOW, RESPONSE_TEXT)
    );

    var userDisplayName = "Example user";
    var energyPortalUser = mock(EnergyPortalUserDto.class);
    when(energyPortalUser.displayName()).thenReturn(userDisplayName);

    var user2DisplayName = "Example user 2";
    var energyPortalUser2 = mock(EnergyPortalUserDto.class);
    when(energyPortalUser2.displayName()).thenReturn(user2DisplayName);

    var energyPortalUserByWuaId = Map.of(1L, energyPortalUser, 2L, energyPortalUser2);

    assertThat(furtherInformationService.getFurtherInformationViews(furtherInformationList, energyPortalUserByWuaId))
        .extracting(
            FurtherInformationView::requestedAtTimestamp,
            FurtherInformationView::requestedByUser,
            FurtherInformationView::requestText,
            FurtherInformationView::isClosed,
            FurtherInformationView::respondedAtTimestamp,
            FurtherInformationView::respondedByUser,
            FurtherInformationView::responseText
        ).containsExactly(
            tuple(
                DateUtils.format(NOW, DateUtils.DATE_TIME),
                userDisplayName,
                REQUEST_TEXT,
                false,
                null,
                null,
                null
            ),
            tuple(
                DateUtils.format(NOW, DateUtils.DATE_TIME),
                userDisplayName,
                REQUEST_TEXT,
                true,
                DateUtils.format(NOW, DateUtils.DATE_TIME),
                user2DisplayName,
                RESPONSE_TEXT
            ),
            tuple(
                DateUtils.format(NOW, DateUtils.DATE_TIME),
                user2DisplayName,
                REQUEST_TEXT,
                true,
                DateUtils.format(NOW, DateUtils.DATE_TIME),
                userDisplayName,
                RESPONSE_TEXT
            )
        );

    verifyNoInteractions(energyPortalUserService);
  }

  private FurtherInformation createFurtherInformation(
      Long requestedByWuaId,
      Instant requestedAtTimestamp,
      String requestText,
      FurtherInformationStatus status,
      Long respondedByWuaId,
      Instant respondedAtTimestamp,
      String responseText
  ) {
    var furtherInforation = new FurtherInformation();

    furtherInforation.setRequestedByWuaId(requestedByWuaId);
    furtherInforation.setRequestedAtDatetime(requestedAtTimestamp);
    furtherInforation.setRequestText(requestText);
    furtherInforation.setStatus(status);
    furtherInforation.setRespondedByWuaId(respondedByWuaId);
    furtherInforation.setRespondedAtDatetime(respondedAtTimestamp);
    furtherInforation.setResponseText(responseText);

    return furtherInforation;
  }

}
