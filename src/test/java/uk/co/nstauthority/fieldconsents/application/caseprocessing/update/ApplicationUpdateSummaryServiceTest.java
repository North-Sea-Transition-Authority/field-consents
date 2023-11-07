package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateSummaryServiceTest {

  private static final WebUserAccountId REQUESTER_WUA_ID = WebUserAccountId.from(1L);
  private static final WebUserAccountId RESPONDER_WUA_ID = WebUserAccountId.from(2L);
  private static final Instant REQUESTED_AT = Instant.now();
  private static final Instant DEADLINE = REQUESTED_AT.plus(5, ChronoUnit.DAYS);
  private static final Instant RESPONDED_AT = DEADLINE.minus(1, ChronoUnit.DAYS);
  private static final String REQUEST_TEXT = "request text";
  private static final String RESPONSE_TEXT = "response text";

  @Mock
  private ApplicationUpdateService applicationUpdateService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock(strictness = Mock.Strictness.LENIENT)
  private EnergyPortalUserDto requesterUser;

  @Mock(strictness = Mock.Strictness.LENIENT)
  private EnergyPortalUserDto responderUser;

  @Spy
  @InjectMocks
  private ApplicationUpdateSummaryService applicationUpdateSummaryService;

  @Captor
  private ArgumentCaptor<Set<WebUserAccountId>> webUserAccountIdsCaptor;

  @Captor
  private ArgumentCaptor<Map<Long, EnergyPortalUserDto>> energyPortalUserByWuaIdCaptor;

  @Captor
  private ArgumentCaptor<String> stringCaptor;

  @Captor
  private ArgumentCaptor<ApplicationUpdate> applicationUpdateCaptor;

  private ApplicationVersion applicationVersion;

  private ApplicationVersion responseApplicationVersion;

  private ApplicationUpdate applicationUpdate;

  private Map<WebUserAccountId, EnergyPortalUserDto> energyPortalUserByWebUserAccountId;

  private Map<Long, EnergyPortalUserDto> energyPortalUserDtoByWuaId;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    responseApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    responseApplicationVersion.setVersion(responseApplicationVersion.getVersion() + 1);

    applicationUpdate = new ApplicationUpdate();

    when(requesterUser.displayName()).thenReturn("Requesting user");
    when(responderUser.displayName()).thenReturn("Responding user");

    energyPortalUserByWebUserAccountId = Map.of(
        REQUESTER_WUA_ID, requesterUser,
        RESPONDER_WUA_ID, responderUser
    );

    energyPortalUserDtoByWuaId = Map.of(
        REQUESTER_WUA_ID.id(), requesterUser,
        RESPONDER_WUA_ID.id(), responderUser
    );
  }

  @Test
  void getApplicationUpdateSummaryItems_noUpdatesExist() {
    var application = applicationVersion.getApplication();
    when(applicationUpdateService.getApplicationUpdatesByApplication(application)).thenReturn(Collections.emptyList());
    assertThat(applicationUpdateSummaryService.getApplicationUpdateSummaryItems(application)).isEmpty();
  }

  @Test
  void getApplicationUpdateSummaryItems_someUsersNotFound() {
    applicationUpdate.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    applicationUpdate.setRespondedByWuaId(RESPONDER_WUA_ID.id());

    var application = applicationVersion.getApplication();

    when(applicationUpdateService.getApplicationUpdatesByApplication(application)).thenReturn(List.of(applicationUpdate));

    // the responder user is not part of this map, so an exception should be thrown
    var energyPortalUserByWuaId = Map.of(REQUESTER_WUA_ID, mock(EnergyPortalUserDto.class));
    when(energyPortalUserService.getEnergyPortalUserMap(webUserAccountIdsCaptor.capture())).thenReturn(energyPortalUserByWuaId);

    assertThatThrownBy(() -> applicationUpdateSummaryService.getApplicationUpdateSummaryItems(application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Fetched 1 energy portal users but needed 2 to complete successfully");

    assertThat(webUserAccountIdsCaptor.getValue()).containsExactly(REQUESTER_WUA_ID, RESPONDER_WUA_ID);
  }

  @Test
  void getApplicationUpdateSummaryItems() {
    // this should be shown first because it was requested later than the one below
    applicationUpdate.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    applicationUpdate.setRespondedByWuaId(RESPONDER_WUA_ID.id());
    applicationUpdate.setRequestedDateTime(Instant.now().plus(1, ChronoUnit.DAYS));

    var applicationUpdate2 = new ApplicationUpdate();
    applicationUpdate2.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    applicationUpdate2.setRespondedByWuaId(RESPONDER_WUA_ID.id());
    applicationUpdate2.setRequestedDateTime(Instant.now());

    var application = applicationVersion.getApplication();

    when(applicationUpdateService.getApplicationUpdatesByApplication(application)).thenReturn(List.of(applicationUpdate2, applicationUpdate));
    when(energyPortalUserService.getEnergyPortalUserMap(webUserAccountIdsCaptor.capture())).thenReturn(energyPortalUserByWebUserAccountId);

    var summaryItem = mock(SummaryItem.class);
    doReturn(summaryItem)
        .when(applicationUpdateSummaryService)
        .getSummaryItem(stringCaptor.capture(), applicationUpdateCaptor.capture(), energyPortalUserByWuaIdCaptor.capture());

    assertThat(applicationUpdateSummaryService.getApplicationUpdateSummaryItems(application)).containsExactly(summaryItem, summaryItem);

    assertThat(stringCaptor.getAllValues()).containsExactly("Application update 2", "Application update 1");
    assertThat(applicationUpdateCaptor.getAllValues()).containsExactlyElementsOf(List.of(applicationUpdate, applicationUpdate2));
    assertThat(energyPortalUserByWuaIdCaptor.getAllValues()).allSatisfy(capturedMap ->
        assertThat(capturedMap).containsExactlyInAnyOrderEntriesOf(energyPortalUserDtoByWuaId)
    );

    verify(energyPortalUserService).getEnergyPortalUserMap(webUserAccountIdsCaptor.capture());
    assertThat(webUserAccountIdsCaptor.getValue()).containsExactly(REQUESTER_WUA_ID, RESPONDER_WUA_ID);
  }

  @Test
  void getSummaryItem() {
    var energyPortalUserByWuaId = Collections.<Long, EnergyPortalUserDto>emptyMap();
    var summaryCard = mock(SummaryCard.class);
    var heading = "Heading";

    doReturn(summaryCard)
        .when(applicationUpdateSummaryService)
        .getSummaryCard(eq(applicationUpdate), energyPortalUserByWuaIdCaptor.capture());

    assertThat(applicationUpdateSummaryService.getSummaryItem(heading, applicationUpdate, energyPortalUserByWuaId))
        .extracting(SummaryItem::displayName, SummaryItem::summaryCards)
        .containsExactly(heading, Collections.singletonList(summaryCard));

    assertThat(energyPortalUserByWuaIdCaptor.getValue()).isEqualTo(energyPortalUserByWuaId);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationUpdateStatus.class, names = "CLOSED", mode = Mode.EXCLUDE)
  void getSummaryCard_notClosed(ApplicationUpdateStatus status) {
    applicationUpdate.setApplicationUpdateStatus(status);
    applicationUpdate.setApplicationVersion(applicationVersion);
    applicationUpdate.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    applicationUpdate.setRequestedDateTime(REQUESTED_AT);
    applicationUpdate.setRequestText(REQUEST_TEXT);

    assertThat(applicationUpdateSummaryService.getSummaryCard(applicationUpdate, energyPortalUserDtoByWuaId))
        .extracting(SummaryCard::displayName, SummaryCard::summaryCardType, SummaryCard::summaryData)
        .containsExactly(
            null,
            SummaryCardType.SIMPLE_SUMMARY,
            SummaryDataView
                .newWithKeyValue("Update status", applicationUpdate.getApplicationUpdateStatus().getDisplayName())
                .addKeyValue("Request application version", applicationUpdate.getApplicationVersion().getVersion())
                .addKeyValue("Requested by", requesterUser.displayName())
                .addKeyValue("Requested on", DateUtils.format(applicationUpdate.getRequestedDateTime(), DateUtils.DATE_TIME))
                .addKeyValue("Request details", applicationUpdate.getRequestText())
                .addKeyValue("Deadline", DateUtils.format(applicationUpdate.getDeadlineDateTime(), DateUtils.DATE_TIME))
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationUpdateResponseType.class, names = "OTHER_CHANGES", mode = Mode.EXCLUDE)
  void getSummaryCard_closed_notOtherChanges(ApplicationUpdateResponseType responseType) {
    applicationUpdate.setApplicationUpdateStatus(ApplicationUpdateStatus.CLOSED);
    applicationUpdate.setApplicationVersion(applicationVersion);
    applicationUpdate.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    applicationUpdate.setRequestedDateTime(REQUESTED_AT);
    applicationUpdate.setRequestText(REQUEST_TEXT);

    applicationUpdate.setResponseApplicationVersion(responseApplicationVersion);
    applicationUpdate.setRespondedByWuaId(RESPONDER_WUA_ID.id());
    applicationUpdate.setRespondedDateTime(RESPONDED_AT);
    applicationUpdate.setResponseType(responseType);

    assertThat(applicationUpdateSummaryService.getSummaryCard(applicationUpdate, energyPortalUserDtoByWuaId))
        .extracting(SummaryCard::displayName, SummaryCard::summaryCardType, SummaryCard::summaryData)
        .containsExactly(
            null,
            SummaryCardType.SIMPLE_SUMMARY,
            SummaryDataView
                .newWithKeyValue("Update status", applicationUpdate.getApplicationUpdateStatus().getDisplayName())
                .addKeyValue("Request application version", applicationUpdate.getApplicationVersion().getVersion())
                .addKeyValue("Requested by", requesterUser.displayName())
                .addKeyValue("Requested on", DateUtils.format(applicationUpdate.getRequestedDateTime(), DateUtils.DATE_TIME))
                .addKeyValue("Request details", applicationUpdate.getRequestText())
                .addKeyValue("Deadline", DateUtils.format(applicationUpdate.getDeadlineDateTime(), DateUtils.DATE_TIME))
                .addKeyValue("Response application version", applicationUpdate.getResponseApplicationVersion().getVersion())
                .addKeyValue("Responded by", responderUser.displayName())
                .addKeyValue("Responded on", DateUtils.format(applicationUpdate.getRespondedDateTime(), DateUtils.DATE_TIME))
                .addKeyValue("Update type", applicationUpdate.getResponseType().getDisplayName())
        );
  }

  @Test
  void getSummaryCard_closed_otherChanges() {
    applicationUpdate.setApplicationUpdateStatus(ApplicationUpdateStatus.CLOSED);
    applicationUpdate.setApplicationVersion(applicationVersion);
    applicationUpdate.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    applicationUpdate.setRequestedDateTime(REQUESTED_AT);
    applicationUpdate.setRequestText(REQUEST_TEXT);

    applicationUpdate.setResponseApplicationVersion(responseApplicationVersion);
    applicationUpdate.setRespondedByWuaId(RESPONDER_WUA_ID.id());
    applicationUpdate.setRespondedDateTime(RESPONDED_AT);
    applicationUpdate.setResponseType(ApplicationUpdateResponseType.OTHER_CHANGES);
    applicationUpdate.setResponseText(RESPONSE_TEXT);

    assertThat(applicationUpdateSummaryService.getSummaryCard(applicationUpdate, energyPortalUserDtoByWuaId))
        .extracting(SummaryCard::displayName, SummaryCard::summaryCardType, SummaryCard::summaryData)
        .containsExactly(
            null,
            SummaryCardType.SIMPLE_SUMMARY,
            SummaryDataView
                .newWithKeyValue("Update status", applicationUpdate.getApplicationUpdateStatus().getDisplayName())
                .addKeyValue("Request application version", applicationUpdate.getApplicationVersion().getVersion())
                .addKeyValue("Requested by", requesterUser.displayName())
                .addKeyValue("Requested on", DateUtils.format(applicationUpdate.getRequestedDateTime(), DateUtils.DATE_TIME))
                .addKeyValue("Request details", applicationUpdate.getRequestText())
                .addKeyValue("Deadline", DateUtils.format(applicationUpdate.getDeadlineDateTime(), DateUtils.DATE_TIME))
                .addKeyValue("Response application version", applicationUpdate.getResponseApplicationVersion().getVersion())
                .addKeyValue("Responded by", responderUser.displayName())
                .addKeyValue("Responded on", DateUtils.format(applicationUpdate.getRespondedDateTime(), DateUtils.DATE_TIME))
                .addKeyValue("Update type", applicationUpdate.getResponseType().getDisplayName())
                .addKeyValue("Other changes description", applicationUpdate.getResponseText())
        );
  }

}
