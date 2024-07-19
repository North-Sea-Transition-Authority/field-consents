package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;

@ContextConfiguration(classes = ApplicationUpdateController.class)
class ApplicationUpdateControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ApplicationUpdateController> CONTROLLER_CLASS = ApplicationUpdateController.class;
  private static final String VIEW_NAME = "fcs/application/update/applicationUpdates";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationUpdateSummaryService applicationUpdateSummaryService;

  private Application application;

  private ApplicationVersion applicationVersion;

  private List<CaseProcessingActionView> actionList;

  private List<SummaryItem> summaryItems;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();
    actionList = Collections.emptyList();
    summaryItems = Collections.emptyList();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void getApplicationUpdates_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getApplicationUpdates(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getApplicationUpdates_doesNotHavePermission() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getApplicationUpdates(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getApplicationUpdates() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, CaseProcessingActionGroup.APPLICATION_UPDATES)).thenReturn(actionList);
    when(applicationService.getApplicationReference(any(ApplicationVersion.class), any(String.class))).thenReturn(APPLICATION_REFERENCE);
    when(applicationUpdateSummaryService.getApplicationUpdateSummaryItems(application)).thenReturn(summaryItems);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getApplicationUpdates(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("applicationUpdateSummaryItems", summaryItems))
        .andExpect(model().attribute("captionTitle", APPLICATION_REFERENCE))
        .andExpect(model().attribute("actionList", actionList))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(APPLICATION_ID,
            null, null, null
        ))));
  }

}
