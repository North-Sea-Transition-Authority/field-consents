package uk.co.nstauthority.fieldconsents.file;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
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

  private UploadedFile uploadedFile;

  @BeforeEach
  void setUp() {
    uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
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
