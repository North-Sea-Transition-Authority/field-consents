package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.EXISTING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_ID;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_NAME_1;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_UPLOAD_COMPONENT_ATTRIBUTES;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.getFileUploadComponentAttributesBuilder;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = CaseNotesController.class)
class CaseNotesControllerTest extends AbstractApplicationControllerTest {

  static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private CaseNotesService caseNotesService;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private CaseNoteFormValidator caseNoteFormValidator;

  @MockBean
  private FieldConsentsFileService fieldConsentsFileService;

  private ApplicationVersion applicationVersion;

  private FileUploadComponentAttributes fileUploadComponentAttributes;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    fileUploadComponentAttributes = getFileUploadComponentAttributesBuilder().build();

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getNewCaseNote_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CaseNotesController.class)
        .getNewCaseNote(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getNewCaseNote_checkEndPointSecurityOnly_forbidden() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Collections.emptyList());

    mockMvc.perform(get(ReverseRouter.route(on(CaseNotesController.class)
            .getNewCaseNote(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getNewCaseNote_checkEndPointSecurityOnly_allowed() throws Exception {
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(REGULATOR_ADD_CASE_NOTE));
    when(fieldConsentsFileService.fileUploadComponentAttributes(EXISTING_DOCUMENTS))
        .thenReturn(FILE_UPLOAD_COMPONENT_ATTRIBUTES);

    mockMvc.perform(get(ReverseRouter.route(on(CaseNotesController.class)
            .getNewCaseNote(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/addCaseNote"));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getNewCaseNote(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(REGULATOR_ADD_CASE_NOTE));
    when(fieldConsentsFileService.fileUploadComponentAttributes(Collections.emptyList()))
        .thenReturn(fileUploadComponentAttributes);

    mockMvc.perform(get(ReverseRouter.route(on(CaseNotesController.class)
            .getNewCaseNote(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/addCaseNote"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("fileUploadAttributes", fileUploadComponentAttributes))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null))));
  }

  @SecurityTest
  void submitNewCaseNote_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseNotesController.class)
            .submitNewCaseNote(APPLICATION_ID, null, null, user, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitNewCaseNote_withEmptyForm(ApplicationVersion applicationVersion) throws Exception {
    var form = new CaseNoteForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(REGULATOR_ADD_CASE_NOTE));
    when(fieldConsentsFileService.fileUploadComponentAttributes(Collections.emptyList()))
        .thenReturn(fileUploadComponentAttributes);

    doCallRealMethod().when(caseNoteFormValidator).validate(any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseNotesController.class)
                .submitNewCaseNote(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/addCaseNote"));

    verifyNoInteractions(caseNotesService);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitNewCaseNote_withEmptyFormAndFileAdded(ApplicationVersion applicationVersion) throws Exception {
    var form = new CaseNoteForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(REGULATOR_ADD_CASE_NOTE));
    when(fieldConsentsFileService.fileUploadComponentAttributes(Collections.emptyList()))
        .thenReturn(fileUploadComponentAttributes);

    doCallRealMethod().when(caseNoteFormValidator).validate(any(), any());

    var persistedFileAsForm = FileUploadTestUtil.getUploadedFileFormWithDescription(FILE_NAME_1, "old description");

    when(fieldConsentsFileService.getUploadedFileForms(Collections.singleton(FILE_ID)))
        .thenReturn(Collections.singletonList(persistedFileAsForm));

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseNotesController.class)
                .submitNewCaseNote(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
                .param("caseNoteDocuments[0].uploadedFileId", FILE_ID.toString())
                .param("caseNoteDocuments[0].uploadedFileInstant", Instant.now().toString())
                .param("caseNoteDocuments[0].uploadedFileDescription", "new description"))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/addCaseNote"));

    verifyNoInteractions(caseNotesService);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitNewCaseNote_withNonEmptyForm(ApplicationVersion applicationVersion) throws Exception {
    var form = new CaseNoteForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(REGULATOR_ADD_CASE_NOTE));
    when(fieldConsentsFileService.fileUploadComponentAttributes(Collections.emptyList()))
        .thenReturn(fileUploadComponentAttributes);

    doCallRealMethod().when(caseNoteFormValidator).validate(any(), any());

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("New case note added")
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseNotesController.class)
                .submitNewCaseNote(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
                .param("caseNoteText", "test"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));
  }
  
  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
