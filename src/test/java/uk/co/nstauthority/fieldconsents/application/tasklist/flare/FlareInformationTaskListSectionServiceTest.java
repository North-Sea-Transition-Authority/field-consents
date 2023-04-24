package uk.co.nstauthority.fieldconsents.application.tasklist.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARES_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_GAS_PROPERTIES_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_INFORMATION_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_REPORT_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_VENT_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListItem;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas.FlareReportGasData;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas.FlareReportGasDataController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas.FlareReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class FlareInformationTaskListSectionServiceTest {

  @Mock
  private FlareService flareService;

  @Mock
  private FlareReportPeriodService flareReportPeriodService;

  @Mock
  private FlareReportService flareReportService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private FlareAnnualService flareAnnualService;

  @Mock
  private FlareShortTermService flareShortTermService;

  @Mock
  private FlareReportGasDataService flareReportGasDataService;

  private FlareInformationTaskListSectionService flareInformationTaskListSectionService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthDetails annualConsentLengthDetails;

  private ConsentLengthDetails shortTermConsentLengthDetails;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    flareInformationTaskListSectionService = new FlareInformationTaskListSectionService(
        flareService,
        flareReportPeriodService,
        flareReportService,
        consentLengthService,
        flareAnnualService,
        flareShortTermService,
        flareReportGasDataService
    );
    annualConsentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    shortTermConsentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
  }

  @Test
  void getSection_whenNotFlareApplication() {
    ApplicationVersion ventApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    assertThat(flareInformationTaskListSectionService.getSection(ventApplicationVersion)).isEmpty();
  }

  @Test
  void getSection_noConsentDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.empty());

    assertThat(flareInformationTaskListSectionService.getSection(applicationVersion)).isEmpty();
  }

  @Test
  void getSection_flareTaskListSection() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(annualConsentLengthDetails));

    Optional<TaskListSection> taskListSectionOptional = flareInformationTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    assertTaskListSection(taskListSection, FLARE_INFORMATION_SECTION, FLARE_VENT_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getSection_withNonEmptyListOfFlares() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(annualConsentLengthDetails));
    when(flareService.getFlaresForApplicationVersion(applicationVersion))
        .thenReturn(FlareTestUtil.flares);
    when(flareAnnualService.flareAnnualMonthsExist(applicationVersion)).thenReturn(false);

    Optional<TaskListSection> taskListSectionOptional = flareInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(4);

    assertTaskListItem(
        taskListItems.get(0),
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(2),
        FLARE_GAS_PROPERTIES_TASK_LIST_ITEM,
        TaskListLabel.BLOCKED,
        ReverseRouter.route(on(FlareReportGasDataController.class)
            .getFlareReportGasDataForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(3),
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareAnnualController.class)
            .getFlareAnnualForm(applicationVersion.getApplication().getId()))
    );

  }

  @Test
  void getSection_withEmptyListOfFlares() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(annualConsentLengthDetails));
    when(flareAnnualService.flareAnnualMonthsExist(applicationVersion)).thenReturn(false);

    Optional<TaskListSection> taskListSectionOptional = flareInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(4);

    assertTaskListItem(
        taskListItems.get(0),
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareController.class).addFlare(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(2),
        FLARE_GAS_PROPERTIES_TASK_LIST_ITEM,
        TaskListLabel.BLOCKED,
        ReverseRouter.route(on(FlareReportGasDataController.class)
            .getFlareReportGasDataForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(3),
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareAnnualController.class)
            .getFlareAnnualForm(applicationVersion.getApplication().getId()))
    );

  }

  @Test
  void getFlaresTaskListItem_notCompleted() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());
    TaskListItem item = flareInformationTaskListSectionService.getFlaresTaskListItem(applicationVersion);

    assertTaskListItem(
        item,
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareController.class).addFlare(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlaresTaskListItem_completed() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(FlareTestUtil.flares);
    TaskListItem item = flareInformationTaskListSectionService.getFlaresTaskListItem(applicationVersion);

    assertTaskListItem(
        item,
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportTaskListItem_completed() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(true);
    when(flareReportService.flareReportMonthsComplete(applicationVersion)).thenReturn(true);

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareReportController.class)
            .getFlareReportForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportTaskListItem_inProgress() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(true);
    when(flareReportService.flareReportMonthsComplete(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(FlareReportController.class)
            .getFlareReportForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportTaskListItem_notCompleted() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(false);
    when(flareReportService.flareReportMonthsComplete(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportGasDataTaskListItem_blocked() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportGasDataTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_GAS_PROPERTIES_TASK_LIST_ITEM,
        TaskListLabel.BLOCKED,
        ReverseRouter.route(on(FlareReportGasDataController.class)
            .getFlareReportGasDataForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportGasDataTaskListItem_notStarted() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(true);
    when(flareReportGasDataService.findFlareReportGasData(applicationVersion)).thenReturn(Optional.empty());

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportGasDataTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_GAS_PROPERTIES_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareReportGasDataController.class)
            .getFlareReportGasDataForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportGasDataTaskListItem_completed() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(true);

    FlareReportGasData data = FlareVentReportGasTestUtil.getCompleteAndValidFlareReportGasData();
    when(flareReportGasDataService.findFlareReportGasData(applicationVersion)).thenReturn(Optional.of(data));

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportGasDataTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_GAS_PROPERTIES_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareReportGasDataController.class)
            .getFlareReportGasDataForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareConsentTaskListItem_annualNotStarted() {
    when(flareAnnualService.flareAnnualMonthsExist(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService
        .getFlareConsentTaskListItem(applicationVersion, annualConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareAnnualController.class)
            .getFlareAnnualForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareConsentTaskListItem_annualComplete() {
    when(flareAnnualService.flareAnnualMonthsComplete(applicationVersion)).thenReturn(true);

    TaskListItem item = flareInformationTaskListSectionService
        .getFlareConsentTaskListItem(applicationVersion, annualConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareAnnualController.class)
            .getFlareAnnualForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareConsentTaskListItem_annualInProgress() {
    when(flareAnnualService.flareAnnualMonthsExist(applicationVersion)).thenReturn(true);
    when(flareAnnualService.flareAnnualMonthsComplete(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService
        .getFlareConsentTaskListItem(applicationVersion, annualConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(FlareAnnualController.class)
            .getFlareAnnualForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareConsentTaskListItem_shortTermNotStarted() {
    when(flareShortTermService.flareShortTermMonthsExist(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService
        .getFlareConsentTaskListItem(applicationVersion, shortTermConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareShortTermController.class)
            .getFlareShortTermForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareConsentTaskListItem_shortTermComplete() {
    when(flareShortTermService.flareShortTermMonthsComplete(applicationVersion)).thenReturn(true);

    TaskListItem item = flareInformationTaskListSectionService
        .getFlareConsentTaskListItem(applicationVersion, shortTermConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareShortTermController.class)
            .getFlareShortTermForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareConsentTaskListItem_shortTermInProgress() {
    when(flareShortTermService.flareShortTermMonthsExist(applicationVersion)).thenReturn(true);
    when(flareShortTermService.flareShortTermMonthsComplete(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService
        .getFlareConsentTaskListItem(applicationVersion, shortTermConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(FlareShortTermController.class)
            .getFlareShortTermForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareConsentTaskListItem_longTerm() {
    ConsentLengthDetails longTermConsentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);

    assertThatThrownBy(() -> flareInformationTaskListSectionService
        .getFlareConsentTaskListItem(applicationVersion, longTermConsentLengthDetails))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Incorrect consent length type: " + ConsentLengthType.LONG_TERM);
  }

}