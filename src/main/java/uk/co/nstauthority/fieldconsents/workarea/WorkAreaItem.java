package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record WorkAreaItem(
    Integer applicationId,
    String type,
    String duration,
    String reference,
    String operator,
    String asset,
    String seaLocation,
    String status,
    String submittedDateTime,
    String submittedBy
) {
  // TODO: We need to cater for switching between IN_PROGRESS case redirection and SUBMITTED ones.
  //       Also redirect regulators and industry users to the right screen.
  public String url() {
    return ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}
