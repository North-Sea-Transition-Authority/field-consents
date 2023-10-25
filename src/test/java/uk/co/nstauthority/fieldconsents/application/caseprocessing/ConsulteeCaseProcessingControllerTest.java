package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.FURTHER_INFORMATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.VIEW_APPLICATION;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

@ContextConfiguration(classes = ConsulteeCaseProcessingController.class)
class ConsulteeCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ConsulteeCaseProcessingController> CONTROLLER_CLASS = ConsulteeCaseProcessingController.class;
  private static final String PAGE_TITLE = "This is the page title";
  private static final String VIEW_NAME = "fcs/application/consultation/caseProcessing";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private CaseProcessingTabService caseProcessingTabService;

  @MockBean
  private FurtherInformationService furtherInformationService;

  private ApplicationVersion applicationVersion;

  private List<CaseProcessingActionView> caseProcessingActionViews;

  private List<SummarySection> summarySections;

  private List<CaseProcessingTab> caseProcessingTabs;

  private List<FurtherInformation> furtherInformationList;

  private List<FurtherInformationView> furtherInformationViews;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    caseProcessingActionViews = List.of(
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class)
    );
    summarySections = Collections.emptyList();
    caseProcessingTabs = EnumSet.allOf(CaseProcessingTab.class).stream().toList();

    furtherInformationList = Collections.emptyList();
    furtherInformationViews = Collections.emptyList();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void caseProcessing_unauthorised() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void caseProcessing_viewApplication_withoutConsultation() throws Exception {
    setUpMocksWithConsultation(null);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
        .caseProcessing(APPLICATION_ID, null, null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION))
        .andExpect(model().attribute("summarySections", summarySections))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()));
  }

  @Test
  void caseProcessing_viewApplication_withConsultation() throws Exception {
    var consultation = new Consultation();
    consultation.setRequestDeadline(Instant.now());

    setUpMocksWithConsultation(consultation);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION))
        .andExpect(model().attribute("summarySections", summarySections))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("consultationRequestView", ConsultationRequestView.from(consultation)));
  }

  @Test
  void caseProcessing_furtherInformation_withoutConsultation() throws Exception {
    setUpMocksWithConsultation(null);

    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication())).thenReturn(Collections.emptyList());
    when(furtherInformationService.getAllFurtherInformation(Collections.emptyList())).thenReturn(Collections.emptyList());
    when(furtherInformationService.getFurtherInformationViews(Collections.emptyList())).thenReturn(furtherInformationViews);

    var tabParam = "?tab=%s".formatted(FURTHER_INFORMATION.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)) + tabParam)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(FURTHER_INFORMATION))
        .andExpect(model().attribute("furtherInformationViews", furtherInformationViews));
  }

  @Test
  void caseProcessing_furtherInformation_withConsultation() throws Exception {
    var consultation = new Consultation();
    consultation.setRequestDeadline(Instant.now());

    setUpMocksWithConsultation(consultation);

    var consultations = List.of(consultation);
    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication())).thenReturn(consultations);
    when(furtherInformationService.getAllFurtherInformation(consultations)).thenReturn(furtherInformationList);
    when(furtherInformationService.getFurtherInformationViews(furtherInformationList)).thenReturn(furtherInformationViews);

    var tabParam = "?tab=%s".formatted(FURTHER_INFORMATION.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)) + tabParam)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(FURTHER_INFORMATION))
        .andExpect(model().attribute("consultationRequestView", ConsultationRequestView.from(consultation)))
        .andExpect(model().attribute("furtherInformationViews", furtherInformationViews));
  }

  private ResultMatcher[] commonAttributesForTab(CaseProcessingTab tab) {
    var applicationType = applicationVersion.getApplication().getType();
    return new ResultMatcher[] {
        view().name(VIEW_NAME),
        model().attribute("controllerUrl", ReverseRouter.route(on(CONTROLLER_CLASS).caseProcessing(APPLICATION_ID, null, null))),
        model().attribute("pageTitle", PAGE_TITLE),
        model().attribute("selectedTab", tab),
        model().attribute("actionList", caseProcessingActionViews),
        model().attribute("caseProcessingTabs", caseProcessingTabs),
        model().attribute("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
    };
  }

  private void setUpMocksWithConsultation(@Nullable Consultation consultation) {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(PAGE_TITLE);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user)).thenReturn(caseProcessingActionViews);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(Optional.ofNullable(consultation));
    when(caseProcessingTabService.getTabsAvailableToUser(user)).thenReturn(caseProcessingTabs);

    var applicationType = applicationVersion.getApplication().getType();

    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
          .addObject("summarySections", summarySections)
          .addObject("accordionId", applicationVersion.getId())
          .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType));
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class));
  }
}
