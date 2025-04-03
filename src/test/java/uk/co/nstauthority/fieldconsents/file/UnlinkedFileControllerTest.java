package uk.co.nstauthority.fieldconsents.file;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.CONTENT_TYPE;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_NAME_1;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = UnlinkedFileUploadController.class)
class UnlinkedFileControllerTest extends AbstractControllerTest {

  @MockitoBean
  private FileControllerHelperService fileControllerHelperService;

  @SecurityTest
  void upload_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(multipart(ReverseRouter.route(on(UnlinkedFileUploadController.class)
            .upload(null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void upload() throws Exception {
    var file = new MockMultipartFile("file", FILE_NAME_1, CONTENT_TYPE, new byte[]{1, 2, 3, 4, 5});

    when(fileControllerHelperService.upload(file, user)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(multipart(ReverseRouter.route(on(UnlinkedFileUploadController.class)
            .upload(null, null)))
            .file(file)
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk());
  }
}
