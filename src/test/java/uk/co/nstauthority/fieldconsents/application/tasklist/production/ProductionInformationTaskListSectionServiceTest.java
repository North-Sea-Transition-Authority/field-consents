package uk.co.nstauthority.fieldconsents.application.tasklist.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.PRODUCTION_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.PRODUCTION_INFORMATION_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListItem;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListSection;

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
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionController;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionController;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionController;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class ProductionInformationTaskListSectionServiceTest {

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ShortTermProductionService shortTermProductionService;

  @Mock
  private AnnualProductionService annualProductionService;

  @Mock
  private LongTermProductionService longTermProductionService;

  private ProductionInformationTaskListSectionService productionInformationTaskListSectionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    productionInformationTaskListSectionService =
        new ProductionInformationTaskListSectionService(
            consentLengthService,
            shortTermProductionService,
            annualProductionService,
            longTermProductionService);
  }

  @Test
  void getSection_whenNotProductionApplication() {
    ApplicationVersion ventApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    assertThat(productionInformationTaskListSectionService.getSection(ventApplicationVersion)).isEmpty();
  }

  @Test
  void getSection_whenConsentLengthDetailsDontExist() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.empty());

    assertThat(productionInformationTaskListSectionService.getSection(applicationVersion)).isEmpty();
  }

  @Test
  void getSection_whenConsentLengthDetailsExist() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(false);

    Optional<TaskListSection> taskListSectionOptional = productionInformationTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    var taskListSection = taskListSectionOptional.get();
    assertTaskListSection(taskListSection, PRODUCTION_INFORMATION_SECTION, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    List<TaskListItem> taskListItems = taskListSection.items();
    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems.get(0),
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(ShortTermProductionController.class)
            .getShortTermProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_shortTermNotStarted() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(false);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(ShortTermProductionController.class)
            .getShortTermProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_shortTermComplete() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(shortTermProductionService.shortTermProductionMonthsComplete(applicationVersion)).thenReturn(true);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(ShortTermProductionController.class)
            .getShortTermProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_shortTermInProgress() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(shortTermProductionService.shortTermProductionMonthsComplete(applicationVersion)).thenReturn(false);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.SHORT_TERM.getDisplayName(),
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(ShortTermProductionController.class)
            .getShortTermProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_annualNotStarted() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    when(annualProductionService.annualProductionMonthsExist(applicationVersion)).thenReturn(false);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(AnnualProductionController.class)
            .getAnnualProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_annualComplete() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    when(annualProductionService.annualProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(annualProductionService.annualProductionMonthsComplete(applicationVersion)).thenReturn(true);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(AnnualProductionController.class)
            .getAnnualProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_annualInProgress() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    when(annualProductionService.annualProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(annualProductionService.annualProductionMonthsComplete(applicationVersion)).thenReturn(false);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.ANNUAL.getDisplayName(),
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(AnnualProductionController.class)
            .getAnnualProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }


  @Test
  void getProductionConsentTaskListItem_longTermNotStarted() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(longTermProductionService.longTermProductionYearsExist(applicationVersion)).thenReturn(false);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.LONG_TERM.getDisplayName(),
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(LongTermProductionController.class)
            .getLongTermProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_longTermComplete() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(longTermProductionService.longTermProductionYearsExist(applicationVersion)).thenReturn(true);
    when(longTermProductionService.longTermProductionYearsComplete(applicationVersion)).thenReturn(true);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.LONG_TERM.getDisplayName(),
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(LongTermProductionController.class)
            .getLongTermProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getProductionConsentTaskListItem_longTermInProgress() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(longTermProductionService.longTermProductionYearsExist(applicationVersion)).thenReturn(true);
    when(longTermProductionService.longTermProductionYearsComplete(applicationVersion)).thenReturn(false);

    var taskListItem = productionInformationTaskListSectionService
        .getProductionConsentTaskListItem(applicationVersion, consentLengthDetails);

    assertTaskListItem(
        taskListItem,
        ConsentLengthType.LONG_TERM.getDisplayName(),
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(LongTermProductionController.class)
            .getLongTermProductionRequestForm(applicationVersion.getApplication().getId()))
    );
  }

}
