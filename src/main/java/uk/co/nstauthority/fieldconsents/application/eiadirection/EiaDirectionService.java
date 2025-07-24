package uk.co.nstauthority.fieldconsents.application.eiadirection;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted.HaveSubmittedForm;
import uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted.HaveSubmittedFormValidator;
import uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting.NeedsSubmittingForm;
import uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting.NeedsSubmittingFormValidator;
import uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose.ProjectPurposeForm;
import uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose.ProjectPurposeFormValidator;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationJson;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationRestController;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;

@Service
public class EiaDirectionService {

  static final String EIA_DIRECTION_LOOKUP_REQUEST_PURPOSE = "EIA direction lookup for application summary";

  private final EiaDirectionRepository eiaDirectionRepository;
  private final PetsApplicationService petsApplicationService;
  private final ProjectPurposeFormValidator projectPurposeFormValidator;
  private final HaveSubmittedFormValidator haveSubmittedFormValidator;
  private final NeedsSubmittingFormValidator needsSubmittingFormValidator;

  @Autowired
  EiaDirectionService(
      EiaDirectionRepository eiaDirectionRepository,
      PetsApplicationService petsApplicationService,
      ProjectPurposeFormValidator projectPurposeFormValidator,
      HaveSubmittedFormValidator haveSubmittedFormValidator,
      NeedsSubmittingFormValidator needsSubmittingFormValidator
  ) {
    this.eiaDirectionRepository = eiaDirectionRepository;
    this.petsApplicationService = petsApplicationService;
    this.projectPurposeFormValidator = projectPurposeFormValidator;
    this.haveSubmittedFormValidator = haveSubmittedFormValidator;
    this.needsSubmittingFormValidator = needsSubmittingFormValidator;
  }

  public String getEiaDirectionRestUrl() {
    return ReverseRouter.route(on(PetsApplicationRestController.class).getEiaDirectionSearchResults(null))
        .replace("?term", "");
  }

  public TaskListLabel getEiaDirectionTaskListLabel(ApplicationVersion applicationVersion) {
    var eiaDirectionOptional = findEiaDirection(applicationVersion);
    if (eiaDirectionOptional.isEmpty()) {
      return TaskListLabel.NOT_STARTED;
    }

    var eiaDirection = eiaDirectionOptional.get();

    var projectPurposeForm = ProjectPurposeForm.from(eiaDirection);
    var projectPurposeBindingResult = new BeanPropertyBindingResult(projectPurposeForm, "form");
    projectPurposeFormValidator.validate(projectPurposeForm, projectPurposeBindingResult);
    if (projectPurposeBindingResult.hasErrors()) {
      return TaskListLabel.IN_PROGRESS;
    }

    if (Boolean.FALSE.equals(eiaDirection.getForPurposeOfEiaRegs())) {
      return TaskListLabel.COMPLETED;
    }

    var haveSubmittedForm = HaveSubmittedForm.from(eiaDirection);
    var haveSubmittedBindingResult = new BeanPropertyBindingResult(haveSubmittedForm, "form");
    haveSubmittedFormValidator.validate(haveSubmittedForm, haveSubmittedBindingResult);
    if (haveSubmittedBindingResult.hasErrors()) {
      return TaskListLabel.IN_PROGRESS;
    }

    if (Boolean.TRUE.equals(eiaDirection.getHaveSubmittedEiaDirection())) {
      return TaskListLabel.COMPLETED;
    }

    var needsSubmittingForm = NeedsSubmittingForm.from(eiaDirection);
    var needsSubmittingBindingResult = new BeanPropertyBindingResult(needsSubmittingForm, "form");
    needsSubmittingFormValidator.validate(needsSubmittingForm, needsSubmittingBindingResult);
    if (needsSubmittingBindingResult.hasErrors()) {
      return TaskListLabel.IN_PROGRESS;
    }

    return TaskListLabel.COMPLETED;
  }

  public Optional<EiaDirection> findEiaDirection(ApplicationVersion applicationVersion) {
    return eiaDirectionRepository.findByApplicationVersion(applicationVersion);
  }

  @Transactional
  public void updateEiaDirection(ApplicationVersion applicationVersion, ProjectPurposeForm form) {
    var eiaDirection = eiaDirectionRepository.findByApplicationVersion(applicationVersion).orElseGet(EiaDirection::new);
    if (haveEiaPurposeAndRationaleRemainedUnchanged(form, eiaDirection)) {
      return;
    }

    eiaDirection.setApplicationVersion(applicationVersion);
    eiaDirection.setForPurposeOfEiaRegs(form.forPurposeOfEiaRegs());
    if (Boolean.TRUE.equals(form.forPurposeOfEiaRegs())) {
      eiaDirection.setRationaleForPurposeOfEiaRegs(form.rationaleForPurposeOfEiaRegs().getInputValue());
    } else if (Boolean.FALSE.equals(form.forPurposeOfEiaRegs())) {
      eiaDirection.setRationaleForPurposeOfEiaRegs(form.getRationaleNotForPurposeOfEiaRegs().getInputValue());
    }

    // null out the remaining fields because they may now be invalid
    eiaDirection.setHaveSubmittedEiaDirection(null);
    eiaDirection.setSatId(null);
    eiaDirection.setHaveEiaDirectionToSubmit(null);
    eiaDirection.setWhyNoEiaDirection(null);
    eiaDirection.setLatestDateToBeSubmitted(null);
    eiaDirection.setCachedSatRef(null);

    eiaDirectionRepository.save(eiaDirection);
  }

  private static boolean haveEiaPurposeAndRationaleRemainedUnchanged(ProjectPurposeForm form, EiaDirection eiaDirection) {
    if (!Objects.equals(eiaDirection.getForPurposeOfEiaRegs(), form.forPurposeOfEiaRegs())) {
      return false;
    }
    return (Boolean.TRUE.equals(form.forPurposeOfEiaRegs())
        && Objects.equals(eiaDirection.getRationaleForPurposeOfEiaRegs(),
        form.rationaleForPurposeOfEiaRegs().getInputValue()))
        || (Boolean.FALSE.equals(form.forPurposeOfEiaRegs())
        && Objects.equals(eiaDirection.getRationaleForPurposeOfEiaRegs(),
        form.getRationaleNotForPurposeOfEiaRegs().getInputValue()));
  }

  @Transactional
  public void updateEiaDirection(ApplicationVersion applicationVersion,
                                 Boolean haveSubmittedEiaDirection,
                                 @Nullable PetsApplicationJson petsApplication) {
    var eiaDirection = eiaDirectionRepository.findByApplicationVersion(applicationVersion)
        .orElseThrow(() -> new IllegalStateException(getEiaDirectionDoesNotExistMessage(applicationVersion)));

    if (Boolean.TRUE.equals(haveSubmittedEiaDirection)) {
      Objects.requireNonNull(petsApplication, "Expected a non-null PetsApplication");
      var satId = petsApplication.satId();
      eiaDirection.setSatId(satId);
      eiaDirection.setCachedSatRef(petsApplication.satRef());
    } else {
      eiaDirection.setSatId(null);
      eiaDirection.setCachedSatRef(null);
    }

    eiaDirection.setHaveSubmittedEiaDirection(haveSubmittedEiaDirection);

    // null out the remaining fields because they may now be invalid
    eiaDirection.setHaveEiaDirectionToSubmit(null);
    eiaDirection.setWhyNoEiaDirection(null);
    eiaDirection.setLatestDateToBeSubmitted(null);

    eiaDirectionRepository.save(eiaDirection);
  }

  @Transactional
  public void updateEiaDirection(ApplicationVersion applicationVersion, NeedsSubmittingForm form) {
    var eiaDirection = eiaDirectionRepository.findByApplicationVersion(applicationVersion)
        .orElseThrow(() -> new IllegalStateException(getEiaDirectionDoesNotExistMessage(applicationVersion)));

    eiaDirection.setHaveEiaDirectionToSubmit(form.haveEiaDirectionToSubmit());
    eiaDirection.setWhyNoEiaDirection(form.whyNoEiaDirection().getInputValue());
    form.latestDateToBeSubmitted().getAsLocalDate().ifPresent(eiaDirection::setLatestDateToBeSubmitted);

    eiaDirectionRepository.save(eiaDirection);
  }

  private String getEiaDirectionDoesNotExistMessage(ApplicationVersion applicationVersion) {
    return "An EIA direction does not exist for application version %s".formatted(applicationVersion.getId());
  }

  public SummaryCard getEiaDirectionSummaryCard(ApplicationVersion applicationVersion) {
    var eiaDirectionOptional = findEiaDirection(applicationVersion);

    if (eiaDirectionOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var eiaDirection = eiaDirectionOptional.get();
    var summaryDataView = new SummaryDataView(new ArrayList<>());

    var forPurposeOfEiaRegs = eiaDirection.getForPurposeOfEiaRegs();
    summaryDataView.addKeyValue("Is this a \"project\" for the purposes of EIA Regulations 2020?", forPurposeOfEiaRegs);

    var rationaleForPurposeOfEiaRegs = eiaDirection.getRationaleForPurposeOfEiaRegs();
    if (!StringUtils.isEmpty(rationaleForPurposeOfEiaRegs)) {
      summaryDataView.addKeyValue("Rationale for the decision if this is a \"project\" for the purposes of EIA Regulations",
          rationaleForPurposeOfEiaRegs);
    }

    if (Objects.isNull(forPurposeOfEiaRegs) || Boolean.FALSE.equals(forPurposeOfEiaRegs)) {
      return SummaryCard.simpleSummaryCard(summaryDataView);
    }

    var haveSubmittedEiaDirection = eiaDirection.getHaveSubmittedEiaDirection();
    summaryDataView.addKeyValue(
        "Have you submitted an EIA screening direction to the Secretary of State or OPRED?",
        haveSubmittedEiaDirection
    );

    if (Objects.isNull(haveSubmittedEiaDirection)) {
      return SummaryCard.simpleSummaryCard(summaryDataView);
    }

    if (Boolean.TRUE.equals(haveSubmittedEiaDirection)) {
      var eiaDirectionPetsApplicationJson = petsApplicationService.getEiaDirectionByIdOrFallback(
          eiaDirection.getSatId(),
          EIA_DIRECTION_LOOKUP_REQUEST_PURPOSE,
          eiaDirection.getCachedSatRef()
      );

      summaryDataView.addKeyValue("EIA screening direction reference", eiaDirectionPetsApplicationJson.satRef());
      return SummaryCard.simpleSummaryCard(summaryDataView);
    }

    var haveEiaDirectionToSubmit = eiaDirection.getHaveEiaDirectionToSubmit();
    summaryDataView.addKeyValue(
        "Do you have an EIA screening direction that still needs to be submitted?", haveEiaDirectionToSubmit
    );

    if (Objects.isNull(haveEiaDirectionToSubmit)) {
      return SummaryCard.simpleSummaryCard(summaryDataView);
    }

    if (Boolean.TRUE.equals(haveEiaDirectionToSubmit)) {
      summaryDataView.addKeyValue(
          "What is the latest date this will be submitted?", eiaDirection.getLatestDateToBeSubmitted()
      );
    }

    if (Boolean.FALSE.equals(haveEiaDirectionToSubmit)) {
      summaryDataView.addKeyValue(
          "Explain why you don’t intend to submit an EIA screening direction", eiaDirection.getWhyNoEiaDirection()
      );
    }

    return SummaryCard.simpleSummaryCard(summaryDataView);
  }

}
