package uk.co.nstauthority.fieldconsents.breadcrumb;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

class BreadcrumbsUtilTest {

  @Test
  void buildIntoModelAndView_whenBuilderMethodsApplied_assertCrumbListInRightOrder() {
    var modelAndView = new ModelAndView();
    var taskListUrl = "/task-list";
    var currentPage = "My current page";
    var breadcrumbPrompt = "Other page name";
    var breadcrumbEndpoint = "Other page URL";

    var breadcrumbs = Breadcrumbs.builder(currentPage)
        .addWorkAreaBreadcrumb()
        .addTaskListBreadcrumb(taskListUrl)
        .addBreadcrumb(breadcrumbPrompt, breadcrumbEndpoint)
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    Map<String, String> expectedCrumbList = new LinkedHashMap<>();
    expectedCrumbList.put(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), "Work area");
    expectedCrumbList.put(taskListUrl, "Task list");
    expectedCrumbList.put(breadcrumbEndpoint, breadcrumbPrompt);

    assertThat(modelAndView.getModelMap()).containsOnly(
        entry(BreadcrumbsUtil.CURRENT_PAGE_MODEL_ATRR_NAME, currentPage),
        entry(BreadcrumbsUtil.MAP_MODEL_ATRR_NAME, expectedCrumbList)
    );

  }

}
