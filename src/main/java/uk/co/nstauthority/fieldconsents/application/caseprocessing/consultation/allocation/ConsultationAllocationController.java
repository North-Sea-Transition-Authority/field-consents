package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.allocation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_MANAGE_RESPONDER;

import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/allocate-consultation")
@ActionEndPoint(CONSULTATION_MANAGE_RESPONDER)
public class ConsultationAllocationController {

  private static final String PAGE_TITLE = "Assign or re-assign consultation responder";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ConsultationService consultationService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TeamMemberViewService teamMemberViewService;

  ConsultationAllocationController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ConsultationService consultationService,
      EnergyPortalUserService energyPortalUserService,
      TeamMemberViewService teamMemberViewService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.consultationService = consultationService;
    this.energyPortalUserService = energyPortalUserService;
    this.teamMemberViewService = teamMemberViewService;
  }

  @GetMapping
  public ModelAndView getResponderAllocationForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var consultation = consultationService.getLatestOpenConsultation(applicationVersion.getApplication());

    if (Objects.nonNull(consultation.getResponderWuaId())) {
      var form = new ConsultationAllocationForm(WebUserAccountId.from(consultation.getResponderWuaId()));
      return getResponderAllocationModelAndView(applicationVersion, consultation, form);
    }

    var emptyForm = ConsultationAllocationForm.empty();
    return getResponderAllocationModelAndView(applicationVersion, consultation, emptyForm);
  }

  @PostMapping
  ModelAndView submitResponderAllocationForm(@PathVariable Integer applicationId,
                                             @Valid @ModelAttribute("form") ConsultationAllocationForm form,
                                             BindingResult bindingResult,
                                             ServiceUserDetail userDetail,
                                             RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var consultation = consultationService.getLatestOpenConsultation(applicationVersion.getApplication());

    if (bindingResult.hasErrors()) {
      return getResponderAllocationModelAndView(applicationVersion, consultation, form);
    }

    var responderUser = ServiceUserDetail.from(energyPortalUserService.getByWuaId(form.allocatedResponder()));
    consultationService.assignResponderToConsultation(consultation, userDetail, responderUser);
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Consultation responder assigned");

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  private ModelAndView getResponderAllocationModelAndView(ApplicationVersion applicationVersion,
                                                          Consultation consultation,
                                                          ConsultationAllocationForm form) {
    var applicationId = applicationVersion.getApplication().getId();
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var responders = consultationService.getAllAvailableConsultationRespondersForConsultation(consultation);
    var backLinkUrl = ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
        .getApplicationCaseProcessing(applicationId, null));

    return new ModelAndView("fcs/application/consultation/manageConsulteeResponder")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("backLinkUrl", backLinkUrl)
        .addObject("applicationReference", applicationReference)
        .addObject("availableRespondersMap", teamMemberViewService.getUsersMap(responders));
  }

}
