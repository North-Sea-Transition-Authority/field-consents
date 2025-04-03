package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.EXISTING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_UPLOAD_COMPONENT_ATTRIBUTES;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = SupportingInformationController.class)
class SupportingInformationControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/supportingInformationForm";

  @MockitoBean
  private ApplicationService applicationService;

  @MockitoBean
  private SupportingInformationService supportingInformationService;

  @MockitoBean
  private FileControllerHelperService fileControllerHelperService;

  @MockitoBean
  private SupportingInformationFormValidator supportingInformationFormValidator;

  private ApplicationVersion applicationVersion;

  private SupportingInformationForm form;

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
      boolean erapInformationAllowed,
      String expectedHintText
  ) throws Exception {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    when(applicationService.getApplicationById(APPLICATION_ID))
        .thenReturn(applicationVersion.getApplication());

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersion);

    when(supportingInformationService.getSupportingInformationForm(applicationVersion))
        .thenReturn(form);

    var supportingInformationFileController = mock(SupportingInformationFileController.class);

    when(fileControllerHelperService.fileUploadComponentAttributes(
        eq(EXISTING_DOCUMENTS),
        eq(SupportingInformationFileController.class),
        assertArg(downloadFunction -> {
          downloadFunction.apply(supportingInformationFileController);
          verify(supportingInformationFileController).download(applicationVersion.getId(), null, null);
        }),
        assertArg(deleteFunction -> {
          deleteFunction.apply(supportingInformationFileController);
          verify(supportingInformationFileController).delete(applicationVersion.getId(), null, null);
        })
    ))
        .thenReturn(FILE_UPLOAD_COMPONENT_ATTRIBUTES);

    var model = mockMvc.perform(get(ReverseRouter.route(on(SupportingInformationController.class)
            .getSupportingInformationForm(ApplicationTestUtil.APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .isNotNull()
        .containsEntry("form", form)
        .containsEntry("applicationType", applicationTypeString)
        .containsEntry("erapInformationAllowed", erapInformationAllowed)
        .containsEntry("notesHintText", expectedHintText)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null)));
  }

  private static Stream<Arguments> getSupportingInformationForm_withValidUserAndApplication_arguments() {
    return Stream.of(
        Arguments.of(
            ApplicationType.FLARE,
            "flaring",
            true,
            "Add additional information to support the application in the box provided below. Describe the method " +
                "used to split Cat A, Cat B and Cat C in the application here. There is also an option to attach " +
                "files to the application towards the bottom of this page if more detailed supporting information " +
                "is required."
        ),
        Arguments.of(
            ApplicationType.VENT,
            "venting",
            true,
            "Add additional information to support the application in the box provided below. Describe the method " +
                "used to split Cat A, Cat B and Cat C in the application here. There is also an option to attach " +
                "files to the application towards the bottom of this page if more detailed supporting information " +
                "is required."
        ),
        Arguments.of(
            ApplicationType.PRODUCTION,
            "production",
            false,
            "Add additional information to support the application in the box provided below. There is also an " +
                "option to attach files to the application towards the bottom of this page if more detailed " +
                "supporting information is required."
        )
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
        .andExpect(view().name("redirect:" + ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null))));

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

    when(fileControllerHelperService.fileUploadComponentAttributes(eq(EXISTING_DOCUMENTS), eq(SupportingInformationFileController.class), any(), any()))
        .thenReturn(FILE_UPLOAD_COMPONENT_ATTRIBUTES);

    mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME));
  }

}
