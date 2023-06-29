package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_ID;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_NAME_1;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.getFileUploadComponentAttributesWithPath;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.getUploadedFileFormWithDescription;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = SupportingInformationController.class)
class SupportingInformationControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/supportingInformationForm";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private SupportingInformationService supportingInformationService;

  @MockBean
  private SupportingInformationDocumentService supportingInformationDocumentService;

  @MockBean
  private SupportingInformationFormValidator supportingInformationFormValidator;

  @MockBean
  private ApplicationVersionFileService applicationVersionFileService;

  private ApplicationVersion applicationVersion;

  private SupportingInformationForm form;

  private FileUploadComponentAttributes fileUploadComponentAttributes;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    form = new SupportingInformationForm();
    when(supportingInformationService.getSupportingInformationForm(applicationVersion)).thenReturn(form);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(applicationVersion.getApplication());
  }

  @ParameterizedTest
  @MethodSource("getSupportingInformationForm_withValidUserAndApplication_arguments")
  void getSupportingInformationForm_withValidUserAndApplication(
      ApplicationType applicationType,
      String applicationTypeString,
      boolean erapInformationAllowed
  ) throws Exception {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    when(applicationService.getApplicationById(APPLICATION_ID))
        .thenReturn(applicationVersion.getApplication());

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersion);

    when(supportingInformationService.getSupportingInformationForm(applicationVersion))
        .thenReturn(form);

    fileUploadComponentAttributes = getFileUploadComponentAttributesWithPath("form.supportingDocuments");
    when(supportingInformationDocumentService.fileUploadComponentAttributes(eq(applicationVersion), anyList()))
        .thenReturn(fileUploadComponentAttributes);

    var model = mockMvc.perform(get(ReverseRouter.route(on(SupportingInformationController.class)
            .getSupportingInformationForm(ApplicationTestUtil.APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .isNotNull()
        .containsEntry("erapInformationAllowed", erapInformationAllowed)
        .containsEntry("submitUrl", ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(APPLICATION_ID)))
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    // applicationType is nullable
    assertThat(model.get("applicationType")).isEqualTo(applicationTypeString);
    assertThat(model).containsEntry("form", form);
  }

  private static Stream<Arguments> getSupportingInformationForm_withValidUserAndApplication_arguments() {
    return Stream.of(
        Arguments.of(ApplicationType.FLARE, "flaring", true),
        Arguments.of(ApplicationType.VENT, "venting", true),
        Arguments.of(ApplicationType.PRODUCTION, null, false)
    );
  }

  @SecurityTest
  void getSupportingInformationForm_withUnauthorisedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SupportingInformationController.class)
            .getSupportingInformationForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveSupportingInformation_withValidUser() throws Exception {
    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<SupportingInformationForm> supportingInformationFormArgumentCaptor =
        ArgumentCaptor.forClass(SupportingInformationForm.class);

    mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID))));

    verify(supportingInformationService, times(1))
        .saveSupportingInformation(applicationVersionArgumentCaptor.capture(), supportingInformationFormArgumentCaptor.capture());
  }

  @SecurityTest
  void saveSupportingInformation_withUnauthorisedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveSupportingInformation_validationFailure() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("notes", "invalid", "example message");
      return null;
    })
        .when(supportingInformationFormValidator)
        .validate(any(), any());

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);

    fileUploadComponentAttributes = getFileUploadComponentAttributesWithPath("form.supportingDocuments");
    when(supportingInformationDocumentService.fileUploadComponentAttributes(eq(applicationVersion), anyList()))
        .thenReturn(fileUploadComponentAttributes);

    mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME));
  }

  @Test
  void saveSupportingInformation_validationFailure_checkFileDescription() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("notes", "invalid", "example message");
      return null;
    })
        .when(supportingInformationFormValidator)
        .validate(any(), any());

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);

    fileUploadComponentAttributes = getFileUploadComponentAttributesWithPath("form.supportingDocuments");
    when(supportingInformationDocumentService.fileUploadComponentAttributes(eq(applicationVersion), anyList()))
        .thenReturn(fileUploadComponentAttributes);

    // when the form is put back into the model and view, it will need to fetch the file name, file size etc.
    // it also gets back the file description, but we want to use the file description that's in the form
    // because it may have been updated as part of this form submission.
    var persistedFileAsForm = getUploadedFileFormWithDescription(FILE_NAME_1, "old description");

    when(applicationVersionFileService.getUploadedFileForms(Collections.singleton(FILE_ID)))
        .thenReturn(Collections.singletonList(persistedFileAsForm));

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf())
            .param("supportingDocuments[0].uploadedFileId", FILE_ID.toString())
            .param("supportingDocuments[0].uploadedFileInstant", Instant.now().toString())
            .param("supportingDocuments[0].uploadedFileDescription", "new description"))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView.getModel().get("form"))
        .isInstanceOf(SupportingInformationForm.class)
        .asInstanceOf(InstanceOfAssertFactories.type(SupportingInformationForm.class))
        .extracting(SupportingInformationForm::getSupportingDocuments)
        .extracting(list -> list.get(0))
        .extracting(UploadedFileForm::getFileDescription)
        .isEqualTo("new description"); // the description was copied forward into the new form
  }

}
