package uk.co.nstauthority.fieldconsents.application.tasklist.production;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
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
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class ProductionInformationTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final ConsentLengthService consentLengthService;

  private final ShortTermProductionService shortTermProductionService;

  private final AnnualProductionService annualProductionService;

  private final LongTermProductionService longTermProductionService;

  @Autowired
  public ProductionInformationTaskListSectionService(ConsentLengthService consentLengthService,
                                                     ShortTermProductionService shortTermProductionService,
                                                     AnnualProductionService annualProductionService,
                                                     LongTermProductionService longTermProductionService) {
    this.consentLengthService = consentLengthService;
    this.shortTermProductionService = shortTermProductionService;
    this.annualProductionService = annualProductionService;
    this.longTermProductionService = longTermProductionService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    if (applicationVersion.getApplication().getType() != ApplicationType.PRODUCTION) {
      return Optional.empty();
    }

    var consentLengthDetailsOptional =
        consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return Optional.empty();
    }

    ConsentLengthDetails consentLengthDetails = consentLengthDetailsOptional.get();

    var items = Collections.singletonList(
        getProductionConsentTaskListItem(applicationVersion, consentLengthDetails)
    );

    return Optional.of(new TaskListSection("Production information", 20, items));
  }

  TaskListItem getProductionConsentTaskListItem(ApplicationVersion applicationVersion,
                                                ConsentLengthDetails consentLengthDetails) {

    var applicationId = applicationVersion.getApplication().getId();

    var consentLengthType = consentLengthDetails.getConsentLength();

    return switch (consentLengthType) {
      case SHORT_TERM ->
          new TaskListItem(consentLengthType.getDisplayName(),
              getShortTermProductionTaskListLabel(applicationVersion),
              ReverseRouter.route(on(ShortTermProductionController.class)
                  .getShortTermProductionRequestForm(applicationId)));
      case ANNUAL ->
          new TaskListItem(consentLengthType.getDisplayName(),
              getAnnualProductionTaskListLabel(applicationVersion),
              ReverseRouter.route(on(AnnualProductionController.class)
                  .getAnnualProductionRequestForm(applicationId)));
      case LONG_TERM ->
          new TaskListItem(consentLengthType.getDisplayName(),
              getLongTermProductionTaskListLabel(applicationVersion),
              ReverseRouter.route(on(LongTermProductionController.class)
                  .getLongTermProductionRequestForm(applicationId)));
    };
  }

  private TaskListLabel getShortTermProductionTaskListLabel(ApplicationVersion applicationVersion) {
    if (!shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)) {
      return TaskListLabel.NOT_STARTED;
    } else if (shortTermProductionService.shortTermProductionMonthsComplete(applicationVersion)) {
      return TaskListLabel.COMPLETED;
    } else {
      // we know here that short term production months data exists, but it isn't complete
      return TaskListLabel.IN_PROGRESS;
    }
  }

  private TaskListLabel getAnnualProductionTaskListLabel(ApplicationVersion applicationVersion) {
    if (!annualProductionService.annualProductionMonthsExist(applicationVersion)) {
      return TaskListLabel.NOT_STARTED;
    } else if (annualProductionService.annualProductionMonthsComplete(applicationVersion)) {
      return TaskListLabel.COMPLETED;
    } else {
      // we know here that annual production months data exists, but it isn't complete
      return TaskListLabel.IN_PROGRESS;
    }
  }

  private TaskListLabel getLongTermProductionTaskListLabel(ApplicationVersion applicationVersion) {
    if (!longTermProductionService.longTermProductionYearsExist(applicationVersion)) {
      return TaskListLabel.NOT_STARTED;
    } else if (longTermProductionService.longTermProductionYearsComplete(applicationVersion)) {
      return TaskListLabel.COMPLETED;
    } else {
      // we know here that long term production years data exists, but it isn't complete
      return TaskListLabel.IN_PROGRESS;
    }
  }
}
