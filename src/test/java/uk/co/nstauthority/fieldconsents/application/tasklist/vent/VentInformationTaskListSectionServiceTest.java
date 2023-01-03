package uk.co.nstauthority.fieldconsents.application.tasklist.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_VENT_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.VENTS_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.VENT_INFORMATION_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.VENT_REPORT_TASK_LIST_ITEM;
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
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class VentInformationTaskListSectionServiceTest {

  @Mock
  private VentService ventService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private VentAnnualService ventAnnualService;

  @Mock
  private VentShortTermService ventShortTermService;

  @Mock
  private VentReportPeriodService ventReportPeriodService;

  private VentInformationTaskListSectionService ventInformationTaskListSectionService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthDetails annualConsentLengthDetails;

  private ConsentLengthDetails shortTermConsentLengthDetails;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    ventInformationTaskListSectionService =
        new VentInformationTaskListSectionService(
            ventService,
            consentLengthService,
            ventAnnualService,
            ventShortTermService,
            ventReportPeriodService
        );
    annualConsentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    shortTermConsentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
  }

  @Test
  void getSection_whenNotVentApplication() {
    ApplicationVersion ventApplicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

    assertThat(ventInformationTaskListSectionService.getSection(ventApplicationVersion)).isEmpty();
  }

  @Test
  void getSection_noConsentDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.empty());

    assertThat(ventInformationTaskListSectionService.getSection(applicationVersion)).isEmpty();
  }

  @Test
  void getSection_flareTaskListSection() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(annualConsentLengthDetails));

    Optional<TaskListSection> taskListSectionOptional = ventInformationTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    assertTaskListSection(taskListSection, VENT_INFORMATION_SECTION, FLARE_VENT_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getSection_withNonEmptyListOfVents() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(annualConsentLengthDetails));
    when(ventService.getVentsForApplicationVersion(applicationVersion))
        .thenReturn(VentTestUtil.vents);
    when(ventAnnualService.ventAnnualMonthsExist(applicationVersion)).thenReturn(false);

    Optional<TaskListSection> taskListSectionOptional = ventInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(3);

    assertTaskListItem(
        taskListItems.get(0),
        VENTS_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(VentController.class).viewVentsSummary(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        VENT_REPORT_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentReportPeriodController.class)
            .getVentReportPeriodForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(2),
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentAnnualController.class)
            .getVentAnnualForm(applicationVersion.getApplication().getId()))
    );

  }

  @Test
  void getSection_withEmptyListOfVents() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(annualConsentLengthDetails));
    when(ventAnnualService.ventAnnualMonthsExist(applicationVersion)).thenReturn(false);

    Optional<TaskListSection> taskListSectionOptional = ventInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(3);

    assertTaskListItem(
        taskListItems.get(0),
        VENTS_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentController.class).addVent(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        VENT_REPORT_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentReportPeriodController.class)
            .getVentReportPeriodForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(2),
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentAnnualController.class)
            .getVentAnnualForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getVentsTaskListItem_notStarted() {
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());
    TaskListItem item = ventInformationTaskListSectionService.getVentsTaskListItem(applicationVersion);

    assertTaskListItem(
        item,
        VENTS_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentController.class).addVent(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getVentsTaskListItem_completed() {
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(VentTestUtil.vents);
    TaskListItem item = ventInformationTaskListSectionService.getVentsTaskListItem(applicationVersion);

    assertTaskListItem(
        item,
        VENTS_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(VentController.class).viewVentsSummary(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getVentConsentTaskListItem_annualNotStarted() {
    when(ventAnnualService.ventAnnualMonthsExist(applicationVersion)).thenReturn(false);

    TaskListItem item = ventInformationTaskListSectionService
        .getVentConsentTaskListItem(applicationVersion, annualConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentAnnualController.class)
            .getVentAnnualForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getVentConsentTaskListItem_annualComplete() {
    when(ventAnnualService.ventAnnualMonthsComplete(applicationVersion)).thenReturn(true);

    TaskListItem item = ventInformationTaskListSectionService
        .getVentConsentTaskListItem(applicationVersion, annualConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(VentAnnualController.class)
            .getVentAnnualForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getVentConsentTaskListItem_annualInProgress() {
    when(ventAnnualService.ventAnnualMonthsExist(applicationVersion)).thenReturn(true);
    when(ventAnnualService.ventAnnualMonthsComplete(applicationVersion)).thenReturn(false);

    TaskListItem item = ventInformationTaskListSectionService
        .getVentConsentTaskListItem(applicationVersion, annualConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(VentAnnualController.class)
            .getVentAnnualForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getVentConsentTaskListItem_shortTermNotStarted() {
    when(ventShortTermService.ventShortTermMonthsExist(applicationVersion)).thenReturn(false);

    TaskListItem item = ventInformationTaskListSectionService
        .getVentConsentTaskListItem(applicationVersion, shortTermConsentLengthDetails);

    assertTaskListItem(item,
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(VentShortTermController.class)
            .getVentShortTermForm(applicationVersion.getApplication().getId())));
  }

  @Test
  void getVentConsentTaskListItem_longTerm() {
    ConsentLengthDetails longTermConsentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);

    assertThatThrownBy(() -> ventInformationTaskListSectionService
        .getVentConsentTaskListItem(applicationVersion, longTermConsentLengthDetails))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Incorrect consent length type: " + ConsentLengthType.LONG_TERM);
  }

}