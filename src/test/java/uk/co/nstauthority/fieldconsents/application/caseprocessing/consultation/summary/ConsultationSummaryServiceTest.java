package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.EiaRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.HabitatsRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response.ConsultationResponseFileController;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ConsultationSummaryServiceTest {

  private static final int CONSULTATION_ID = 1;
  private static final Instant REQUESTED_ON = Instant.now();
  private static final Instant CONSULTATION_DEADLINE = Instant.now();
  private static final Instant RESPONDED_ON = Instant.now();
  private static final Team CONSULTATION_TEAM = new TeamTestUtil.TeamBuilder().withTeamType(TeamType.OPRED).build();
  private static final WebUserAccountId USER_WUA_ID = WebUserAccountId.from(1L);
  private static final WebUserAccountId REQUESTER_WUA_ID = WebUserAccountId.from(2L);
  private static final WebUserAccountId RESPONDER_WUA_ID = WebUserAccountId.from(3L);
  private static final WebUserAccountId RESPONDER2_WUA_ID = WebUserAccountId.from(4L);
  private static final String HABITATS_RESPONSE_DESCRIPTION = "habitats response description";
  private static final String EIA_RESPONSE_DESCRIPTION = "EIA response description";

  @Mock
  private ConsultationService consultationService;

  @Mock
  private FurtherInformationService furtherInformationService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Spy
  @InjectMocks
  private ConsultationSummaryService consultationSummaryService;

  @Captor
  private ArgumentCaptor<Map<Long, EnergyPortalUserDto>> energyPortalUserByWuaIdCaptor;

  @Captor
  private ArgumentCaptor<Consultation> consultationCaptor;

  @Captor
  private ArgumentCaptor<Integer> integerCaptor;

  @Captor
  private ArgumentCaptor<List<FurtherInformation>> furtherInformationCaptor;

  private Application application;

  private Consultation consultation;

  private FurtherInformation furtherInformation;

  private UploadedFile uploadedFile;

  @BeforeEach
  void setUp() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    consultation = Consultation.newBuilder()
        .withId(CONSULTATION_ID)
        .withRequestApplicationVersion(applicationVersion)
        .build();

    furtherInformation = new FurtherInformation();
    furtherInformation.setConsultation(consultation);

    uploadedFile = new UploadedFile();
    uploadedFile.setName("example.pdf");
    uploadedFile.setDescription("description");
  }

  @Test
  void getConsultationSummaryItems() {
    consultation.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    consultation.setResponderWuaId(RESPONDER_WUA_ID.id());
    consultation.setRespondedByWuaId(RESPONDER2_WUA_ID.id());
    consultation.setRequestedAtDatetime(Instant.now().plus(1, ChronoUnit.DAYS));

    var consultation2 = Consultation.newBuilder()
        .withId(CONSULTATION_ID + 1)
        .withRequestedBy(REQUESTER_WUA_ID)
        .withResponder(RESPONDER_WUA_ID)
        .withRespondedBy(RESPONDER2_WUA_ID)
        .withRequestedAt(Instant.now())
        .build();

    furtherInformation.setRequestedAtDatetime(Instant.now().plus(1, ChronoUnit.DAYS));

    var furtherInformation2 = new FurtherInformation();
    furtherInformation2.setConsultation(consultation2);
    furtherInformation2.setRequestedAtDatetime(Instant.now());

    var user = mock(EnergyPortalUserDto.class);
    var energyPortalUserByWebUserAccountId = Map.of(
        REQUESTER_WUA_ID, user,
        RESPONDER_WUA_ID, user,
        RESPONDER2_WUA_ID, user
    );

    when(consultationService.getConsultationsByApplication(application)).thenReturn(List.of(consultation2, consultation));
    when(furtherInformationService.getAllFurtherInformation(List.of(consultation, consultation2))).thenReturn(List.of(furtherInformation2, furtherInformation));
    when(energyPortalUserService.getEnergyPortalUserMap(Set.of(REQUESTER_WUA_ID, RESPONDER_WUA_ID, RESPONDER2_WUA_ID))).thenReturn(energyPortalUserByWebUserAccountId);

    var furtherInformationView = mock(FurtherInformationView.class);
    var furtherInformationViews = List.of(furtherInformationView);
    when(furtherInformationService.getFurtherInformationViews(furtherInformationCaptor.capture(), energyPortalUserByWuaIdCaptor.capture())).thenReturn(furtherInformationViews);

    var summaryItem = mock(SummaryItem.class);
    doReturn(summaryItem)
        .when(consultationSummaryService)
        .getConsultationSummaryItem(integerCaptor.capture(), consultationCaptor.capture(), eq(furtherInformationViews), energyPortalUserByWuaIdCaptor.capture());

    assertThat(consultationSummaryService.getConsultationSummaryItems(application)).containsExactly(summaryItem, summaryItem);
    assertThat(furtherInformationCaptor.getAllValues()).containsExactly(
        Collections.singletonList(furtherInformation),
        Collections.singletonList(furtherInformation2)
    );

    assertThat(integerCaptor.getAllValues()).containsExactly(2, 1);
    assertThat(furtherInformationCaptor.getAllValues()).contains(
        Collections.singletonList(furtherInformation),
        Collections.singletonList(furtherInformation2)
    );

    assertThat(energyPortalUserByWuaIdCaptor.getAllValues()).allSatisfy(capturedMap ->
        assertThat(capturedMap).containsExactlyInAnyOrderEntriesOf(Map.of(
            REQUESTER_WUA_ID.id(), user,
            RESPONDER_WUA_ID.id(), user,
            RESPONDER2_WUA_ID.id(), user
        ))
    );
  }

  @Test
  void getConsultationSummaryItems_noConsultationsExist() {
    when(consultationService.getConsultationsByApplication(application)).thenReturn(Collections.emptyList());
    assertThat(consultationSummaryService.getConsultationSummaryItems(application)).isEmpty();
  }

  @Test
  void getConsultationSummaryItems_someUsersNotReturned() {
    consultation.setRequestedByWuaId(REQUESTER_WUA_ID.id());
    consultation.setResponderWuaId(RESPONDER_WUA_ID.id());
    consultation.setRespondedByWuaId(RESPONDER2_WUA_ID.id());

    var user = mock(EnergyPortalUserDto.class);

    var energyPortalUserByWebUserAccountId = Map.of(
        REQUESTER_WUA_ID, user,
        RESPONDER_WUA_ID, user
        // not account was found for RESPONDER2_WUA_ID
    );

    var consultations = List.of(consultation);
    var furtherInformationList = List.of(furtherInformation);

    when(consultationService.getConsultationsByApplication(application)).thenReturn(consultations);
    when(furtherInformationService.getAllFurtherInformation(consultations)).thenReturn(furtherInformationList);
    when(energyPortalUserService.getEnergyPortalUserMap(Set.of(REQUESTER_WUA_ID, RESPONDER_WUA_ID, RESPONDER2_WUA_ID))).thenReturn(energyPortalUserByWebUserAccountId);

    assertThatThrownBy(() -> consultationSummaryService.getConsultationSummaryItems(application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Fetched 2 energy portal users but needed 3 to complete successfully");
  }

  @Test
  void getConsultationSummaryItem() {
    var furtherInformationView = mock(FurtherInformationView.class);
    var furtherInformationViews = Collections.singletonList(furtherInformationView);
    var energyPortalUserByWuaId = Map.<Long, EnergyPortalUserDto>of();

    var consultationDetailsCard = mock(SummaryCard.class);
    doReturn(consultationDetailsCard)
        .when(consultationSummaryService)
        .getConsultationSummaryCard(consultation, energyPortalUserByWuaId);

    var filesSummaryCard = mock(SummaryCard.class);
    doReturn(Optional.of(filesSummaryCard))
        .when(consultationSummaryService)
        .getFilesSummaryCard(consultation);

    var furtherInformationSummaryCard = mock(SummaryCard.class);
    when(furtherInformationView.toSummaryCardWithHeading("Further information request 1")).thenReturn(furtherInformationSummaryCard);

    var expectedSummaryCards = List.of(consultationDetailsCard, filesSummaryCard, furtherInformationSummaryCard);

    assertThat(consultationSummaryService.getConsultationSummaryItem(9, consultation, furtherInformationViews, energyPortalUserByWuaId))
        .extracting(SummaryItem::displayName, SummaryItem::summaryCards)
        .containsExactly("Consultation 9", expectedSummaryCards);
  }

  @Test
  void getConsultationSummaryItem_multipleFurtherInformationRequests() {
    var furtherInformationView1 = mock(FurtherInformationView.class);
    var furtherInformationView2 = mock(FurtherInformationView.class);

    var furtherInformationViews = List.of(furtherInformationView1, furtherInformationView2);

    var energyPortalUserByWuaId = Map.<Long, EnergyPortalUserDto>of();

    var consultationDetailsCard = mock(SummaryCard.class);
    doReturn(consultationDetailsCard)
        .when(consultationSummaryService)
        .getConsultationSummaryCard(consultation, energyPortalUserByWuaId);

    var filesSummaryCard = mock(SummaryCard.class);
    doReturn(Optional.of(filesSummaryCard))
        .when(consultationSummaryService)
        .getFilesSummaryCard(consultation);

    var furtherInformationSummaryCard = mock(SummaryCard.class);
    when(furtherInformationView1.toSummaryCardWithHeading("Further information request 2")).thenReturn(furtherInformationSummaryCard);
    when(furtherInformationView2.toSummaryCardWithHeading("Further information request 1")).thenReturn(furtherInformationSummaryCard);

    var expectedSummaryCards = List.of(consultationDetailsCard, filesSummaryCard, furtherInformationSummaryCard, furtherInformationSummaryCard);

    assertThat(consultationSummaryService.getConsultationSummaryItem(1, consultation, furtherInformationViews, energyPortalUserByWuaId))
        .extracting(SummaryItem::displayName, SummaryItem::summaryCards)
        .containsExactly("Consultation 1", expectedSummaryCards);
  }

  @Test
  void getConsultationSummaryItem_withoutFurtherInformation() {
    var energyPortalUserByWuaId = Map.<Long, EnergyPortalUserDto>of();

    var consultationDetailsCard = mock(SummaryCard.class);
    doReturn(consultationDetailsCard)
        .when(consultationSummaryService)
        .getConsultationSummaryCard(consultation, energyPortalUserByWuaId);

    var filesSummaryCard = mock(SummaryCard.class);
    doReturn(Optional.of(filesSummaryCard))
        .when(consultationSummaryService)
        .getFilesSummaryCard(consultation);

    var expectedSummaryCards = List.of(consultationDetailsCard, filesSummaryCard);

    assertThat(consultationSummaryService.getConsultationSummaryItem(2, consultation, Collections.emptyList(), energyPortalUserByWuaId))
        .extracting(SummaryItem::displayName, SummaryItem::summaryCards)
        .containsExactly("Consultation 2", expectedSummaryCards);
  }

  @Test
  void getConsultationSummaryItem_withoutFiles() {
    var furtherInformationView = mock(FurtherInformationView.class);
    var furtherInformationViews = Collections.singletonList(furtherInformationView);
    var energyPortalUserByWuaId = Map.<Long, EnergyPortalUserDto>of();

    var consultationDetailsCard = mock(SummaryCard.class);
    doReturn(consultationDetailsCard)
        .when(consultationSummaryService)
        .getConsultationSummaryCard(consultation, energyPortalUserByWuaId);

    doReturn(Optional.empty())
        .when(consultationSummaryService)
        .getFilesSummaryCard(consultation);

    var furtherInformationSummaryCard = mock(SummaryCard.class);
    when(furtherInformationView.toSummaryCardWithHeading("Further information request 1")).thenReturn(furtherInformationSummaryCard);

    var expectedSummaryCards = List.of(consultationDetailsCard, furtherInformationSummaryCard);

    assertThat(consultationSummaryService.getConsultationSummaryItem(1, consultation, furtherInformationViews, energyPortalUserByWuaId))
        .extracting(SummaryItem::displayName, SummaryItem::summaryCards)
        .containsExactly("Consultation 1", expectedSummaryCards);
  }

  @Test
  void getConsultationSummaryItem_withoutFurtherInformation_withoutFiles() {
    var energyPortalUserByWuaId = Map.<Long, EnergyPortalUserDto>of();

    var consultationDetailsCard = mock(SummaryCard.class);
    doReturn(consultationDetailsCard)
        .when(consultationSummaryService)
        .getConsultationSummaryCard(consultation, energyPortalUserByWuaId);

    doReturn(Optional.empty())
        .when(consultationSummaryService)
        .getFilesSummaryCard(consultation);

    var expectedSummaryCards = Collections.singletonList(consultationDetailsCard);

    assertThat(consultationSummaryService.getConsultationSummaryItem(3, consultation, Collections.emptyList(), energyPortalUserByWuaId))
        .extracting(SummaryItem::displayName, SummaryItem::summaryCards)
        .containsExactly("Consultation 3", expectedSummaryCards);
  }

  @ParameterizedTest
  @MethodSource({
      "getConsultationSummaryCard_arguments_production",
      "getConsultationSummaryCard_arguments_flare",
      "getConsultationSummaryCard_arguments_vent",
  })
  void getConsultationSummaryCard(
      Consultation consultation,
      SummaryDataView summaryDataView,
      Map<Long, EnergyPortalUserDto> energyPortalUserByWuaId
  ) {
    assertThat(consultationSummaryService.getConsultationSummaryCard(consultation, energyPortalUserByWuaId))
        .extracting(SummaryCard::displayName, SummaryCard::summaryCardType, SummaryCard::summaryData)
        .containsExactly(null, SummaryCardType.SIMPLE_SUMMARY, summaryDataView);
  }

  private static Stream<Arguments> getConsultationSummaryCard_arguments_production() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    var requester = mock(EnergyPortalUserDto.class);
    when(requester.displayName()).thenReturn("Requesting User");

    var responder = mock(EnergyPortalUserDto.class);
    when(responder.displayName()).thenReturn("Responding User");

    var responder2 = mock(EnergyPortalUserDto.class);
    when(responder2.displayName()).thenReturn("Responding User 2");

    return Stream.of(
        // when the consultation is open, and a responder has been assigned
        arguments(
            Consultation.newBuilder()
                .withStatus(ConsultationStatus.OPEN)
                .withRequestDeadline(CONSULTATION_DEADLINE)
                .withConsultationTeam(CONSULTATION_TEAM)
                .withResponder(RESPONDER_WUA_ID)
                .withRequestApplicationVersion(applicationVersion)
                .withRequestedBy(REQUESTER_WUA_ID)
                .withRequestedAt(REQUESTED_ON)
                .build(),
            SummaryDataView
                .newWithKeyValue("Consultation status", ConsultationStatus.OPEN.getDisplayName())
                .addKeyValue("Deadline", DateUtils.format(CONSULTATION_DEADLINE, DateUtils.DATE_TIME))
                .addKeyValue("Consultee", CONSULTATION_TEAM.getDisplayName())
                .addKeyValue("Responder", responder.displayName())
                .addKeyValue("Request application version", applicationVersion.getVersion())
                .addKeyValue("Requested by", requester.displayName())
                .addKeyValue("Requested on", DateUtils.format(REQUESTED_ON, DateUtils.DATE_TIME)),
            Map.of(
                REQUESTER_WUA_ID.id(), requester,
                RESPONDER_WUA_ID.id(), responder
            )
        ),
        // when the consultation is open, but the responder has not been assigned
        arguments(
            Consultation.newBuilder()
                .withStatus(ConsultationStatus.OPEN)
                .withRequestDeadline(CONSULTATION_DEADLINE)
                .withConsultationTeam(CONSULTATION_TEAM)
                .withRequestApplicationVersion(applicationVersion)
                .withRequestedBy(REQUESTER_WUA_ID)
                .withRequestedAt(REQUESTED_ON)
                .build(),
            SummaryDataView
                .newWithKeyValue("Consultation status", ConsultationStatus.OPEN.getDisplayName())
                .addKeyValue("Deadline", DateUtils.format(CONSULTATION_DEADLINE, DateUtils.DATE_TIME))
                .addKeyValue("Consultee", CONSULTATION_TEAM.getDisplayName())
                .addKeyValue("Responder", "")
                .addKeyValue("Request application version", applicationVersion.getVersion())
                .addKeyValue("Requested by", requester.displayName())
                .addKeyValue("Requested on", DateUtils.format(REQUESTED_ON, DateUtils.DATE_TIME)),
            Map.of(REQUESTER_WUA_ID.id(), requester)
        ),
        // when the consultation is closed
        arguments(
            Consultation.newBuilder()
                .withStatus(ConsultationStatus.CLOSED)
                .withRequestDeadline(CONSULTATION_DEADLINE)
                .withConsultationTeam(CONSULTATION_TEAM)
                .withResponder(RESPONDER_WUA_ID)
                .withRequestApplicationVersion(applicationVersion)
                .withRequestedBy(REQUESTER_WUA_ID)
                .withRequestedAt(REQUESTED_ON)
                .withRespondedBy(RESPONDER2_WUA_ID)
                .withRespondedAt(RESPONDED_ON)
                .withHabitatsRegsResponseType(HabitatsRegsResponseType.AGREE)
                .withHabitatsRegsResponseDescription(HABITATS_RESPONSE_DESCRIPTION)
                .withEiaRegsResponseType(EiaRegsResponseType.AGREE)
                .withEiaRegsResponseDescription(EIA_RESPONSE_DESCRIPTION)
                .build(),
            SummaryDataView
                .newWithKeyValue("Consultation status", ConsultationStatus.CLOSED.getDisplayName())
                .addKeyValue("Deadline", DateUtils.format(CONSULTATION_DEADLINE, DateUtils.DATE_TIME))
                .addKeyValue("Consultee", CONSULTATION_TEAM.getDisplayName())
                .addKeyValue("Responder", responder.displayName())
                .addKeyValue("Request application version", applicationVersion.getVersion())
                .addKeyValue("Requested by", requester.displayName())
                .addKeyValue("Requested on", DateUtils.format(REQUESTED_ON, DateUtils.DATE_TIME))
                .addKeyValue("Responded by", responder2.displayName())
                .addKeyValue("Responded on", DateUtils.format(RESPONDED_ON, DateUtils.DATE_TIME))
                .addKeyValue("Habitats regulations response", HabitatsRegsResponseType.AGREE.getDisplayName())
                .addKeyValue("Habitats regulations response description", HABITATS_RESPONSE_DESCRIPTION)
                .addKeyValue("EIA regulations response", EiaRegsResponseType.AGREE.getDisplayName())
                .addKeyValue("EIA regulations response description", EIA_RESPONSE_DESCRIPTION),
            Map.of(
                REQUESTER_WUA_ID.id(), requester,
                RESPONDER_WUA_ID.id(), responder,
                RESPONDER2_WUA_ID.id(), responder2
            )
        )
    );
  }

  private static Stream<Arguments> getConsultationSummaryCard_arguments_flare() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);

    var user = mock(EnergyPortalUserDto.class);
    when(user.displayName()).thenReturn("Field Consents User");

    return Stream.of(
        // check that EIA regs questions are not displayed because they are not applicable to Flare applications
        arguments(
            Consultation.newBuilder()
                .withStatus(ConsultationStatus.CLOSED)
                .withRequestDeadline(CONSULTATION_DEADLINE)
                .withConsultationTeam(CONSULTATION_TEAM)
                .withResponder(USER_WUA_ID)
                .withRequestApplicationVersion(applicationVersion)
                .withRequestedBy(USER_WUA_ID)
                .withRequestedAt(REQUESTED_ON)
                .withRespondedBy(USER_WUA_ID)
                .withRespondedAt(RESPONDED_ON)
                .withHabitatsRegsResponseType(HabitatsRegsResponseType.AGREE)
                .withHabitatsRegsResponseDescription(HABITATS_RESPONSE_DESCRIPTION)
                .withEiaRegsResponseType(EiaRegsResponseType.AGREE)
                .withEiaRegsResponseDescription(EIA_RESPONSE_DESCRIPTION)
                .build(),
            SummaryDataView
                .newWithKeyValue("Consultation status", ConsultationStatus.CLOSED.getDisplayName())
                .addKeyValue("Deadline", DateUtils.format(CONSULTATION_DEADLINE, DateUtils.DATE_TIME))
                .addKeyValue("Consultee", CONSULTATION_TEAM.getDisplayName())
                .addKeyValue("Responder", user.displayName())
                .addKeyValue("Request application version", applicationVersion.getVersion())
                .addKeyValue("Requested by", user.displayName())
                .addKeyValue("Requested on", DateUtils.format(REQUESTED_ON, DateUtils.DATE_TIME))
                .addKeyValue("Responded by", user.displayName())
                .addKeyValue("Responded on", DateUtils.format(RESPONDED_ON, DateUtils.DATE_TIME))
                .addKeyValue("Habitats regulations response", HabitatsRegsResponseType.AGREE.getDisplayName())
                .addKeyValue("Habitats regulations response description", HABITATS_RESPONSE_DESCRIPTION),
            Map.of(USER_WUA_ID.id(), user)
        )
    );
  }

  private static Stream<Arguments> getConsultationSummaryCard_arguments_vent() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);

    var user = mock(EnergyPortalUserDto.class);
    when(user.displayName()).thenReturn("Field Consents User");

    return Stream.of(
        // check that EIA regs questions are not displayed because they are not applicable to Vent applications
        arguments(
            Consultation.newBuilder()
                .withStatus(ConsultationStatus.CLOSED)
                .withRequestDeadline(CONSULTATION_DEADLINE)
                .withConsultationTeam(CONSULTATION_TEAM)
                .withResponder(USER_WUA_ID)
                .withRequestApplicationVersion(applicationVersion)
                .withRequestedBy(USER_WUA_ID)
                .withRequestedAt(REQUESTED_ON)
                .withRespondedBy(USER_WUA_ID)
                .withRespondedAt(RESPONDED_ON)
                .withHabitatsRegsResponseType(HabitatsRegsResponseType.AGREE)
                .withHabitatsRegsResponseDescription(HABITATS_RESPONSE_DESCRIPTION)
                .withEiaRegsResponseType(EiaRegsResponseType.AGREE)
                .withEiaRegsResponseDescription(EIA_RESPONSE_DESCRIPTION)
                .build(),
            SummaryDataView
                .newWithKeyValue("Consultation status", ConsultationStatus.CLOSED.getDisplayName())
                .addKeyValue("Deadline", DateUtils.format(CONSULTATION_DEADLINE, DateUtils.DATE_TIME))
                .addKeyValue("Consultee", CONSULTATION_TEAM.getDisplayName())
                .addKeyValue("Responder", user.displayName())
                .addKeyValue("Request application version", applicationVersion.getVersion())
                .addKeyValue("Requested by", user.displayName())
                .addKeyValue("Requested on", DateUtils.format(REQUESTED_ON, DateUtils.DATE_TIME))
                .addKeyValue("Responded by", user.displayName())
                .addKeyValue("Responded on", DateUtils.format(RESPONDED_ON, DateUtils.DATE_TIME))
                .addKeyValue("Habitats regulations response", HabitatsRegsResponseType.AGREE.getDisplayName())
                .addKeyValue("Habitats regulations response description", HABITATS_RESPONSE_DESCRIPTION),
            Map.of(USER_WUA_ID.id(), user)
        )
    );
  }

  @Test
  void getFilesSummaryCard() {
    when(fieldConsentsFileService.getUploadedFiles(ConsultationFileUsage.responseUsageFrom(consultation))).thenReturn(Collections.singletonList(uploadedFile));

    var summaryFileViews = Collections.singletonList(SummaryFileView.from(
        uploadedFile,
        ReverseRouter.route(on(ConsultationResponseFileController.class).download(APPLICATION_ID, CONSULTATION_ID, null, null))
    ));

    assertThat(consultationSummaryService.getFilesSummaryCard(consultation))
        .isPresent()
        .get()
        .extracting(SummaryCard::displayName, SummaryCard::summaryCardType, SummaryCard::summaryData)
        .containsExactly("Response documents", SummaryCardType.FILES_SUMMARY, summaryFileViews);
  }

  @Test
  void getFilesSummaryCard_noUploadedFiles() {
    when(fieldConsentsFileService.getUploadedFiles(ConsultationFileUsage.responseUsageFrom(consultation))).thenReturn(Collections.emptyList());
    assertThat(consultationSummaryService.getFilesSummaryCard(consultation)).isEmpty();
  }

}
