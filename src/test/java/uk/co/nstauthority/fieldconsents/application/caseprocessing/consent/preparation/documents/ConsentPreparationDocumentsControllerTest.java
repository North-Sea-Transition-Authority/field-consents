package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_ID;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_UPLOAD_COMPONENT_ATTRIBUTES;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivum.fileuploadlibrary.core.UploadedFileTestUtil;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceSummaryViewTestUtil;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceControllerHelperService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsentPreparationDocumentsController.class)
class ConsentPreparationDocumentsControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsentPreparationDocumentService consentDocumentService;

  @MockBean
  private ConsentPreparationSupportingDocumentsFormValidator consentPreparationSupportingDocumentsFormValidator;

  @MockBean
  private FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService;

  @MockBean
  private FieldConsentsFileService fieldConsentsFileService;

  @MockBean
  private FileService fileService;

  @Captor
  private ArgumentCaptor<ConsentPreparationSupportingDocumentsForm> consentSupportingDocumentsFormCaptor;

  private ApplicationVersion applicationVersion;
  private Application application;
  private FieldConsentsFileUsage fieldConsentsFileUsage;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();
    fieldConsentsFileUsage = ApplicationFileUsage.supportingConsentDocumentFrom(application);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void editDocuments_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
            .editDocuments(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void editDocuments_doesNotHavePermission() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
            .editDocuments(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void editDocuments() throws Exception {
    var documentInstanceSummaryViews = List.of(DocumentInstanceSummaryViewTestUtil.newBuilder().build());
    var uploadedFileForms = List.of(new UploadedFileForm());
    var consentSupportingDocumentForm = new ConsentPreparationSupportingDocumentsForm(uploadedFileForms);

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consentDocumentService.getConsentSupportingDocumentsForm(application)).thenReturn(consentSupportingDocumentForm);
    when(fieldConsentsFileService.fileUploadComponentAttributes(uploadedFileForms)).thenReturn(FILE_UPLOAD_COMPONENT_ATTRIBUTES);
    when(fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceSummaryViews(application))
        .thenReturn(documentInstanceSummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationDocumentsController.class).editDocuments(APPLICATION_ID)))
        .with(user(user)))
        .andExpect(view().name("fcs/application/consent/documents/consentDocumentsForm"))
        .andExpect(model().attribute("documentInstanceSummaryViews", documentInstanceSummaryViews))
        .andExpect(model().attribute("form", consentSupportingDocumentForm))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(APPLICATION_ID))))
        .andExpect(model().attribute("fileUploadAttributes", FILE_UPLOAD_COMPONENT_ATTRIBUTES));
  }

  @Test
  void saveDocuments() throws Exception {
    var uploadedFile = UploadedFileTestUtil.newBuilder().build();

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);

    mockMvc.perform(post(ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
        .saveDocuments(APPLICATION_ID, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("documents[0].fileId", uploadedFile.getId().toString())
        .param("documents[0].fileName", uploadedFile.getName())
        .param("documents[0].fileSize", String.valueOf(uploadedFile.getContentLength()))
        .param("documents[0].fileDescription", uploadedFile.getDescription())
        .param("documents[0].fileUploadedAt", uploadedFile.getUploadedAt().toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(APPLICATION_ID))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Consent documents saved")
            .build()));

    verify(consentPreparationSupportingDocumentsFormValidator).validate(consentSupportingDocumentsFormCaptor.capture(), any(BindingResult.class));
    assertThat(consentSupportingDocumentsFormCaptor.getValue().getDocuments())
        .hasSize(1)
        .first()
        .extracting(UploadedFileForm::getFileId)
        .isEqualTo(uploadedFile.getId());

    ArgumentCaptor<Collection<UploadedFileForm>> collectionCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(consentDocumentService).saveSupportingConsentDocuments(eq(application), collectionCaptor.capture());
    assertThat(collectionCaptor.getValue())
        .hasSize(1)
        .first()
        .extracting(UploadedFileForm::getFileId)
        .isEqualTo(uploadedFile.getId());
  }

  @Test
  void saveDocuments_withValidationError() throws Exception {
    var documentInstanceSummaryViews = List.of(DocumentInstanceSummaryViewTestUtil.newBuilder().build());
    var uploadedFile = UploadedFileTestUtil.newBuilder().withDescription(null).build();
    var consentSupportingDocumentForm = ConsentPreparationSupportingDocumentsForm.from(List.of(uploadedFile));

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("documents[0].fileDescription", "invalid", "Error message");
      return null;
    })
        .when(consentPreparationSupportingDocumentsFormValidator)
        .validate(any(ConsentPreparationSupportingDocumentsForm.class), any(BindingResult.class));

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consentDocumentService.getConsentSupportingDocumentsForm(application)).thenReturn(consentSupportingDocumentForm);

    ArgumentCaptor<List<UploadedFileForm>> uploadedFileFormsCaptor = ArgumentCaptor.forClass(List.class);
    when(fieldConsentsFileService.fileUploadComponentAttributes(uploadedFileFormsCaptor.capture())).thenReturn(FILE_UPLOAD_COMPONENT_ATTRIBUTES);

    when(fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceSummaryViews(application))
        .thenReturn(documentInstanceSummaryViews);

    var model = mockMvc.perform(post(ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
            .saveDocuments(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("documents[0].fileId", uploadedFile.getId().toString())
            .param("documents[0].fileName", uploadedFile.getName())
            .param("documents[0].fileSize", String.valueOf(uploadedFile.getContentLength()))
            .param("documents[0].fileUploadedAt", uploadedFile.getUploadedAt().toString()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name("fcs/application/consent/documents/consentDocumentsForm"))
        .andExpect(model().attribute("documentInstanceSummaryViews", documentInstanceSummaryViews))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(APPLICATION_ID))))
        .andExpect(model().attribute("fileUploadAttributes", FILE_UPLOAD_COMPONENT_ATTRIBUTES))
        .andExpect(model().attributeExists("form"))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model.get("form"))
        .asInstanceOf(type(ConsentPreparationSupportingDocumentsForm.class))
        .extracting(ConsentPreparationSupportingDocumentsForm::getDocuments)
        .satisfies(documents ->
          assertThat(documents)
              .hasSize(1)
              .first()
              .extracting(UploadedFileForm::getFileId)
              .isEqualTo(uploadedFile.getId())
        );

    assertThat(uploadedFileFormsCaptor.getValue())
        .hasSize(1)
        .first()
        .extracting(UploadedFileForm::getFileId)
        .isEqualTo(uploadedFile.getId());

    verify(consentPreparationSupportingDocumentsFormValidator).validate(any(ConsentPreparationSupportingDocumentsForm.class), any(BindingResult.class));
    verify(consentDocumentService, never()).saveSupportingConsentDocuments(any(), any());
  }

  @Test
  void download() throws Exception {
    var uploadedFile = UploadedFileTestUtil.newBuilder().build();

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fileService.download(uploadedFile)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
        .download(APPLICATION_ID, FILE_ID)))
        .with(user(user)))
        .andExpect(status().is2xxSuccessful());

    verify(fieldConsentsFileService).throwIfFileDoesNotBelongToUsage(uploadedFile, fieldConsentsFileUsage);
  }

  @Test
  void download_fileNotFound() throws Exception {
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());

    doThrow(new ResponseStatusException(NOT_FOUND, "File not found"))
        .when(fieldConsentsFileService)
        .getFileNotFoundException(FILE_ID, fieldConsentsFileUsage);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
            .download(APPLICATION_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }
}
