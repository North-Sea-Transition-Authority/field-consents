package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record WorkAreaItem(
    Integer applicationId,
    String type,
    String duration,
    String reference,
    String operator,
    String asset,
    String geographicArea,
    String status,
    String submittedDateTime,
    String submittedBy
) {
  public String url() {
    return ReverseRouter.route(on(ApplicationSummaryController.class).getApplicationSummary(applicationId, null));
  }
}
