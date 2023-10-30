package uk.co.nstauthority.fieldconsents.fee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/fee-periods")
@HasPermission(permissions = RolePermission.MANAGE_FEE_PERIODS)
public class FeePeriodController {

  static final String CREATE_PAGE_TITLE = "Create fee period";
  static final String CREATE_SUBMIT_BUTTON_TEXT = "Create";
  static final String EDIT_PAGE_TITLE = "Edit fee period";
  static final String EDIT_SUBMIT_BUTTON_TEXT = "Save";

  private final FieldConsentsFeePeriodService fieldConsentsFeePeriodService;
  private final FeePeriodService feePeriodService;
  private final FeePeriodFormValidator feePeriodFormValidator;

  @Autowired
  FeePeriodController(
      FieldConsentsFeePeriodService fieldConsentsFeePeriodService,
      FeePeriodService feePeriodService,
      FeePeriodFormValidator feePeriodFormValidator
  ) {
    this.fieldConsentsFeePeriodService = fieldConsentsFeePeriodService;
    this.feePeriodService = feePeriodService;
    this.feePeriodFormValidator = feePeriodFormValidator;
  }

  @GetMapping
  public ModelAndView getFeePeriods() {
    return new ModelAndView("fcs/payment/feePeriods")
        .addObject("feePeriodSummaryViews", fieldConsentsFeePeriodService.getFeePeriodSummaryViews())
        .addObject(
            "createFeePeriodUrl",
            ReverseRouter.route(on(FeePeriodController.class).getCreateFeePeriod())
        );
  }

  @GetMapping("/{feePeriodId}")
  public ModelAndView getViewFeePeriod(@PathVariable UUID feePeriodId) {
    var feePeriodDto = feePeriodService.getFeePeriodDtoByIdOrThrow(feePeriodId);
    var feeLineDtos = feePeriodService.getFeeLineDtosByFeePeriodId(feePeriodId);

    return new ModelAndView("fcs/payment/viewFeePeriod")
        .addObject("pageTitle", FeePeriodUtil.getTitle(feePeriodDto))
        .addObject("backLinkUrl", ReverseRouter.route(on(FeePeriodController.class).getFeePeriods()))
        .addObject(
            "feeLineViews",
            fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos)
        );
  }

  @GetMapping("/create")
  public ModelAndView getCreateFeePeriod() {
    var latestFeePeriodFeeLineDtos = feePeriodService.getLatestFeePeriodFeeLineDtos();
    return getCreateFeePeriodModelAndView(new FeePeriodForm(), latestFeePeriodFeeLineDtos);
  }

  @PostMapping("/create")
  public ModelAndView createFeePeriod(
      @ModelAttribute("form") FeePeriodForm form,
      BindingResult bindingResult,
      ServiceUserDetail user,
      RedirectAttributes redirectAttributes
  ) {
    var latestFeePeriodFeeLineDtos = feePeriodService.getLatestFeePeriodFeeLineDtos();

    feePeriodFormValidator.validate(form, null, latestFeePeriodFeeLineDtos, bindingResult);

    if (bindingResult.hasErrors()) {
      return getCreateFeePeriodModelAndView(form, latestFeePeriodFeeLineDtos);
    }

    fieldConsentsFeePeriodService.createFeePeriod(form, user);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Fee period created");

    return ReverseRouter.redirect(on(FeePeriodController.class).getFeePeriods());
  }

  private ModelAndView getCreateFeePeriodModelAndView(
      FeePeriodForm form,
      List<FeeLineDto> latestFeePeriodFeeLineDtos
  ) {
    return new ModelAndView("fcs/payment/createOrEditFeePeriod")
        .addObject("form", form)
        .addObject("pageTitle", CREATE_PAGE_TITLE)
        .addObject("submitButtonText", CREATE_SUBMIT_BUTTON_TEXT)
        .addObject(
            "feeLineViews",
            fieldConsentsFeePeriodService.getFeeLineViews(latestFeePeriodFeeLineDtos)
        )
        .addObject("cancelUrl", ReverseRouter.route(on(FeePeriodController.class).getFeePeriods()));
  }

  @GetMapping("/{feePeriodId}/edit")
  public ModelAndView getEditFeePeriod(@PathVariable UUID feePeriodId, RedirectAttributes redirectAttributes) {
    var feePeriodDto = feePeriodService.getFeePeriodDtoByIdOrThrow(feePeriodId);

    if (!fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto)) {
      applyFeePeriodNotEditableNotificationBanner(redirectAttributes);

      return ReverseRouter.redirect(on(FeePeriodController.class).getFeePeriods());
    }

    var feeLineDtos = feePeriodService.getFeeLineDtosByFeePeriodId(feePeriodId);
    var form = fieldConsentsFeePeriodService.getPrefilledFeePeriodForm(feePeriodDto, feeLineDtos);

    return getEditFeePeriodModelAndView(form, feeLineDtos);
  }

  @PostMapping("/{feePeriodId}/edit")
  public ModelAndView editFeePeriod(
      @PathVariable UUID feePeriodId,
      @ModelAttribute("form") FeePeriodForm form,
      BindingResult bindingResult,
      ServiceUserDetail user,
      RedirectAttributes redirectAttributes
  ) {
    var feePeriodDto = feePeriodService.getFeePeriodDtoByIdOrThrow(feePeriodId);

    if (!fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto)) {
      applyFeePeriodNotEditableNotificationBanner(redirectAttributes);

      return ReverseRouter.redirect(on(FeePeriodController.class).getFeePeriods());
    }

    var feeLineDtos = feePeriodService.getFeeLineDtosByFeePeriodId(feePeriodId);

    feePeriodFormValidator.validate(form, feePeriodDto, feeLineDtos, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditFeePeriodModelAndView(form, feeLineDtos);
    }

    fieldConsentsFeePeriodService.editFeePeriod(feePeriodDto, form, user);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Fee period edited");

    return ReverseRouter.redirect(on(FeePeriodController.class).getFeePeriods());
  }

  private void applyFeePeriodNotEditableNotificationBanner(RedirectAttributes redirectAttributes) {
    NotificationBannerUtil.applyNotificationBanner(
        redirectAttributes,
        NotificationBanner.builder()
            .withBannerType(NotificationBannerType.INFO)
            .withTitle("Fee period is not editable")
            .withHeadingContent("Fee periods which have started cannot be edited")
            .build()
    );
  }

  private ModelAndView getEditFeePeriodModelAndView(FeePeriodForm form, List<FeeLineDto> feeLineDtos) {
    return new ModelAndView("fcs/payment/createOrEditFeePeriod")
        .addObject("form", form)
        .addObject("pageTitle", EDIT_PAGE_TITLE)
        .addObject("submitButtonText", EDIT_SUBMIT_BUTTON_TEXT)
        .addObject(
            "feeLineViews",
            fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos)
        )
        .addObject("cancelUrl", ReverseRouter.route(on(FeePeriodController.class).getFeePeriods()));
  }
}
