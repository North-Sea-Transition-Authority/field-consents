package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceSummaryView;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsentPreparationController.class)
class ConsentPreparationControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @MockBean
  private ConsentDataService consentDataService;

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
    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class)
        .viewDocumentInstances(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void viewDocumentInstances_doesNotHavePermission() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class)
            .viewDocumentInstances(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void viewDocumentInstances() throws Exception {
    var documentInstanceSummaryView = new DocumentInstanceSummaryView("title", "description", "/");
    var documentInstanceSummarySummaryViews = List.of(documentInstanceSummaryView);
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentDataView = ConsentDataView.from(consentData);

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(fieldConsentsDocumentInstanceService.getDocumentInstanceSummaryViews(application)).thenReturn(documentInstanceSummarySummaryViews);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.of(consentData));

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class).viewDocumentInstances(APPLICATION_ID)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/consentPreparation"))
        .andExpect(model().attribute("pageTitle", "Consent preparation"))
        .andExpect(model().attribute("documentInstanceSummaryViews", documentInstanceSummarySummaryViews))
        .andExpect(model().attribute("consentDataView", consentDataView))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(model().attribute("consentDataEditUrl", ReverseRouter.route(on(ConsentDataController.class)
            .editConsentData(APPLICATION_ID))));
  }

  @Test
  void viewDocumentInstances_noConsentDataFound() throws Exception {
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class).viewDocumentInstances(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentDataController.class).editConsentData(APPLICATION_ID))));
  }
}
