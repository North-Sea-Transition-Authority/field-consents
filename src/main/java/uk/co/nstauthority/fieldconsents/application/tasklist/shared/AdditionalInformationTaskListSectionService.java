package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.generated.types.FieldShore;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionController;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationController;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class AdditionalInformationTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  static final String FIELD_LOOKUP_PURPOSE = "Lookup field shore information for EIA screening direction tasklist item";

  private final SupportingInformationService supportingInformationService;

  private final ApplicationAssetService applicationAssetService;

  private final FieldService fieldService;

  private final EiaDirectionService eiaDirectionService;


  @Autowired
  public AdditionalInformationTaskListSectionService(SupportingInformationService supportingInformationService,
                                                     ApplicationAssetService applicationAssetService,
                                                     FieldService fieldService,
                                                     EiaDirectionService eiaDirectionService) {
    this.supportingInformationService = supportingInformationService;
    this.applicationAssetService = applicationAssetService;
    this.fieldService = fieldService;
    this.eiaDirectionService = eiaDirectionService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {
    List<TaskListItem> items = new ArrayList<>();

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);

    if (primaryAsset.isField()) {
      var primaryFieldJson = fieldService.getField(primaryAsset.getFieldId(), FIELD_LOOKUP_PURPOSE);
      if (FieldShore.OFFSHORE.equals(primaryFieldJson.getShoreJson().shore())) {
        items.add(getEiaDirectionTaskListItem(applicationVersion));
      }
    }

    items.add(getSupportingInformationTaskListItem(applicationVersion));

    return Optional.of(new TaskListSection("Additional information", 30, items));
  }

  private TaskListItem getEiaDirectionTaskListItem(ApplicationVersion applicationVersion) {
    return new TaskListItem("EIA screening direction",
        TaskListLabel.notStartedOrCompleteByOptional(eiaDirectionService.findEiaDirection(applicationVersion)),
        ReverseRouter.route(on(EiaDirectionController.class).getEiaDirectionForm(
            applicationVersion.getApplication().getId())));
  }

  private TaskListItem getSupportingInformationTaskListItem(ApplicationVersion applicationVersion) {
    return new TaskListItem("Supporting information",
        TaskListLabel.notStartedOrCompleteByOptional(supportingInformationService.findSupportingInformation(applicationVersion)),
        ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(
            applicationVersion.getApplication().getId())));
  }
}
