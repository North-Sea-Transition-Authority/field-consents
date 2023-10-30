package uk.co.nstauthority.fieldconsents.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_NAME_1;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUploadRequest;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = UnlinkedFileController.class)
class UnlinkedFileControllerTest extends AbstractControllerTest {

  private static final UUID FILE_ID = UUID.randomUUID();

  @MockBean
  private FileService fileService;

  @MockBean
  private FieldConsentsFileService fieldConsentsFileService;

  @Captor
  private ArgumentCaptor<Function<FileUploadRequest.Builder, FileUploadRequest>> fileUploadRequestFunctionCaptor;

  private UploadedFile uploadedFile;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);

    objectMapper = new ObjectMapper();
  }

  @SecurityTest
  void upload_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(multipart(ReverseRouter.route(on(UnlinkedFileController.class)
            .upload(null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void upload() throws Exception {
    var file = new MockMultipartFile("file", FILE_NAME_1, CONTENT_TYPE, new byte[]{1, 2, 3, 4, 5});
    var response = FileUploadResponse.success(FILE_ID, file);

    when(fileService.upload(any())).thenReturn(response);

    var responseString = mockMvc.perform(multipart(ReverseRouter.route(on(UnlinkedFileController.class)
            .upload(null, null)))
            .file(file)
            .with(csrf())
            .with(user(user)))
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


  @SecurityTest
  void download_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
            .download(FILE_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void download_fileDoesNotBelongToUser() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fieldConsentsFileService.fileBelongsToUser(uploadedFile, user)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
            .download(FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void download_fileDoesNotExist() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
            .download(FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void download_fileHasUsage() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fieldConsentsFileService.fileHasUsage(uploadedFile)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
            .download(FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void download() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fieldConsentsFileService.fileHasUsage(uploadedFile)).thenReturn(false);
    when(fieldConsentsFileService.fileBelongsToUser(uploadedFile, user)).thenReturn(true);
    when(fileService.download(uploadedFile)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
            .download(FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().is2xxSuccessful());
  }

  @SecurityTest
  void delete_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
            .delete(FILE_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void delete_fileDoesNotBelongToUser() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fieldConsentsFileService.fileBelongsToUser(uploadedFile, user)).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
            .delete(FILE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void delete_fileDoesNotExist() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
            .delete(FILE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void delete_fileHasUsage() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fieldConsentsFileService.fileHasUsage(uploadedFile)).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
            .delete(FILE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void delete() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fieldConsentsFileService.fileHasUsage(uploadedFile)).thenReturn(false);
    when(fieldConsentsFileService.fileBelongsToUser(uploadedFile, user)).thenReturn(true);
    when(fileService.delete(uploadedFile)).thenReturn(FileDeleteResponse.success(FILE_ID));

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
            .delete(FILE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is2xxSuccessful());
  }
}
