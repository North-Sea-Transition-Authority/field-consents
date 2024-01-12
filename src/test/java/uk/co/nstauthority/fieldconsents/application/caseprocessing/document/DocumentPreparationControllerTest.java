package uk.co.nstauthority.fieldconsents.application.caseprocessing.document;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceSummaryView;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = DocumentPreparationController.class)
class DocumentPreparationControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  private Application application;
  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void viewDocumentInstances_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentPreparationController.class)
        .viewDocumentInstances(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void viewDocumentInstances_doesNotHavePermission() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(get(ReverseRouter.route(on(DocumentPreparationController.class)
            .viewDocumentInstances(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void viewDocumentInstances() throws Exception {
    var documentInstanceSummaryView = new DocumentInstanceSummaryView("title", "description", "/");
    var documentInstanceSummarySummaryViews = List.of(documentInstanceSummaryView);

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(fieldConsentsDocumentInstanceService.getDocumentInstanceSummaryViews(application)).thenReturn(documentInstanceSummarySummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentPreparationController.class).viewDocumentInstances(APPLICATION_ID)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/document/documentPreparation"))
        .andExpect(model().attribute("pageTitle", "Document preparation"))
        .andExpect(model().attribute("documentInstanceSummaryViews", documentInstanceSummarySummaryViews));
  }
}
