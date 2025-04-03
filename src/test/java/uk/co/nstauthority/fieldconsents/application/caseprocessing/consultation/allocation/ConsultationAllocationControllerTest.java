package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.allocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ConsultationAllocationController.class)
class ConsultationAllocationControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ConsultationAllocationController> CONTROLLER_CLASS = ConsultationAllocationController.class;
  private static final String VIEW_NAME = "fcs/application/consultation/manageConsulteeResponder";
  private static final String PAGE_TITLE = "Assign or reassign consultation responder";
  private static final String APPLICATION_REFERENCE = "Application reference";
  private static final EnergyPortalUserDto ENERGY_PORTAL_USER_DTO = new EnergyPortalUserDto(
      1L,
      1L,
      "Title",
      "Forename",
      "Surname",
      "Email",
      "Telephone",
      false,
      true
  );
  private static final ServiceUserDetail RESPONDER_SERVICE_USER = ServiceUserDetail.from(ENERGY_PORTAL_USER_DTO);
  private static final List<TeamMemberView> TEAM_MEMBER_VIEWS = List.of(
      TeamMemberViewTestUtil.newBuilder().build(),
      TeamMemberViewTestUtil.newBuilder().build(),
      TeamMemberViewTestUtil.newBuilder().build()
  );
  private static final Map<String, String> TEAM_MEMBER_VIEWS_AS_MAP = TEAM_MEMBER_VIEWS
      .stream()
      .collect(StreamUtils.toLinkedHashMap(
          teamMemberView -> teamMemberView.wuaId().toString(),
          TeamMemberView::getDisplayName
      ));

  @MockitoBean
  private ApplicationService applicationService;

  @MockitoBean
  private ConsultationService consultationService;

  @MockitoBean
  private EnergyPortalUserService energyPortalUserService;

  private ApplicationVersion applicationVersion;

  private Application application;

  private Consultation consultation;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    application = applicationVersion.getApplication();
    consultation = new Consultation();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
    when(consultationService.getLatestOpenConsultation(application)).thenReturn(consultation);
    when(consultationService.getAllAvailableConsultationRespondersForConsultation(consultation)).thenReturn(TEAM_MEMBER_VIEWS);
    when(caseProcessingActionService.getTaskListActionItems(applicationVersion, user))
        .thenReturn(Set.of(CaseProcessingActionItem.CONSULTATION_MANAGE_RESPONDER));
  }

  @SecurityTest
  void getResponderAllocationForm_notAuthorised() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getResponderAllocationForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitResponderAllocationForm_notAuthorised() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitResponderAllocationForm(APPLICATION_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getResponderAllocationForm() throws Exception {
    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getResponderAllocationForm(APPLICATION_ID)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsEntry("form", ConsultationAllocationForm.empty())
        .containsEntry("pageTitle", PAGE_TITLE)
        .containsEntry("backLinkUrl", ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null)))
        .containsEntry("applicationReference", APPLICATION_REFERENCE)
        .containsEntry("availableRespondersMap", TEAM_MEMBER_VIEWS_AS_MAP);
  }

  @Test
  void getResponderAllocationForm_responderAlreadyAssigned() throws Exception {
    consultation.setResponderWuaId(1L);
    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(1L))).thenReturn(ENERGY_PORTAL_USER_DTO);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getResponderAllocationForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsEntry("form", new ConsultationAllocationForm(WebUserAccountId.from(ENERGY_PORTAL_USER_DTO.webUserAccountId())))
        .containsEntry("pageTitle", PAGE_TITLE)
        .containsEntry("backLinkUrl", ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null)))
        .containsEntry("applicationReference", APPLICATION_REFERENCE)
        .containsEntry("availableRespondersMap", TEAM_MEMBER_VIEWS_AS_MAP);
  }

  @Test
  void submitResponderAllocationForm() throws Exception {
    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(1L))).thenReturn(ENERGY_PORTAL_USER_DTO);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitResponderAllocationForm(APPLICATION_ID, null, null, null, null)))
            .param("allocatedResponder", "1")
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))));

    verify(consultationService).assignResponderToConsultation(consultation, user, RESPONDER_SERVICE_USER);
  }

  @Test
  void submitResponderAllocationForm_noResponderSubmitted() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitResponderAllocationForm(APPLICATION_ID, null, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(consultationService, never()).assignResponderToConsultation(any(), any(), any());
  }

}
