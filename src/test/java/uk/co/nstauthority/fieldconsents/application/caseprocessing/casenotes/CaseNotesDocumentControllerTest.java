package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.CONTENT_TYPE;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_ID;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_NAME_1;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUploadRequest;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = CaseNotesDocumentController.class)
class CaseNotesDocumentControllerTest extends AbstractControllerTest {

  private static final int APPLICATION_ID = 1;
  private static final Class<CaseNotesDocumentController> CONTROLLER = CaseNotesDocumentController.class;

  @MockBean
  private FileService fileService;

  @MockBean
  private CaseNotesDocumentService caseNotesDocumentService;

  @MockBean
  private ApplicationVersionFileService applicationVersionFileService;

  @Captor
  private ArgumentCaptor<Function<FileUploadRequest.Builder, FileUploadRequest>> fileUploadRequestFunctionCaptor;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void upload_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .upload(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER).download(APPLICATION_ID, FILE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void delete_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(APPLICATION_ID, FILE_ID)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void upload() throws Exception {
    var file = new MockMultipartFile("file", FILE_NAME_1, CONTENT_TYPE, new byte[]{1, 2, 3, 4, 5});
    var response = FileUploadResponse.success(FILE_ID, file);

    when(fileService.upload(any())).thenReturn(response);

    var responseString = mockMvc.perform(multipart(ReverseRouter.route(on(CONTROLLER)
            .upload(APPLICATION_ID, null, null)))
            .file(file)
            .with(csrf())
            .with(user(user))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Map<String, String> httpResponse = objectMapper.readValue(responseString, new TypeReference<>(){});
    assertThat(httpResponse)
        .containsEntry("fileId", FILE_ID.toString())
        .containsEntry("fileName", FILE_NAME_1)
        .containsEntry("contentType", CONTENT_TYPE)
        .containsEntry("size", "5")
        .containsEntry("error", null);

    var requestBuilder = FileUploadRequest.newBuilder()
        .withBucket("bucket")
        .withMaximumSize(DataSize.ofMegabytes(50))
        .withFileExtensions(Set.of("pdf"));

    verify(fileService).upload(fileUploadRequestFunctionCaptor.capture());
    var request = fileUploadRequestFunctionCaptor.getValue().apply(requestBuilder);
    assertThat(request)
        .extracting(
            FileUploadRequest::usageId,
            FileUploadRequest::usageType,
            FileUploadRequest::documentType,
            FileUploadRequest::uploadedBy
        ).containsExactly(
            null, // we add these on form submission
            null,
            null,
            user.wuaId().toString()
        );
  }

  @Test
  void download() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.download(uploadedFile)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(APPLICATION_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isOk());

    verify(caseNotesDocumentService).throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);
    verify(fileService).download(uploadedFile);
  }

  @Test
  void download_invalidFileId() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(applicationVersionFileService.getFileNotFoundException(FILE_ID, applicationVersion))
        .thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(APPLICATION_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_fileNotLinkedToApplication() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(caseNotesDocumentService)
        .throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(APPLICATION_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());

    verify(caseNotesDocumentService).throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);
  }

  @Test
  void delete() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.delete(uploadedFile)).thenReturn(FileDeleteResponse.success(FILE_ID));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(APPLICATION_ID, FILE_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

    verify(caseNotesDocumentService).throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);
    verify(fileService).delete(uploadedFile);
  }

  @Test
  void delete_invalidFileId() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(applicationVersionFileService.getFileNotFoundException(FILE_ID, applicationVersion))
        .thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(APPLICATION_ID, FILE_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_fileNotLinkedToApplication() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(caseNotesDocumentService)
        .throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(APPLICATION_ID, FILE_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isNotFound());

    verify(caseNotesDocumentService).throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);
  }
}
