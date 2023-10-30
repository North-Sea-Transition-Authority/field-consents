package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_ID;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUploadRequest;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsultationResponseDocumentController.class)
class ConsultationResponseDocumentControllerTest extends AbstractControllerTest {

  private static final int CONSULTATION_ID = 1;
  private static final Class<ConsultationResponseDocumentController> CONTROLLER = ConsultationResponseDocumentController.class;

  @MockBean
  private FileService fileService;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private FieldConsentsFileService fieldConsentsFileService;

  @Captor
  private ArgumentCaptor<Function<FileUploadRequest.Builder, FileUploadRequest>> fileUploadRequestFunctionCaptor;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private ApplicationVersion applicationVersion;

  private Application application;

  private Consultation consultation;

  private ConsultationFileUsage fileUsage;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    application = applicationVersion.getApplication();

    consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);

    fileUsage = ConsultationFileUsage.responseUsageFrom(consultation);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
  }

  @SecurityTest
  void download_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER).download(APPLICATION_ID, CONSULTATION_ID, FILE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void download() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consultationService.getConsultationByIdAndApplication(CONSULTATION_ID, application)).thenReturn(consultation);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    when(fileService.download(uploadedFile)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(APPLICATION_ID, CONSULTATION_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isOk());

    verify(fieldConsentsFileService).throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage);
    verify(fileService).download(uploadedFile);
  }

  @Test
  void download_invalidFileId() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consultationService.getConsultationByIdAndApplication(CONSULTATION_ID, application)).thenReturn(consultation);
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(fieldConsentsFileService.getFileNotFoundException(FILE_ID, fileUsage)).thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(APPLICATION_ID, CONSULTATION_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_fileNotLinkedToApplication() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consultationService.getConsultationByIdAndApplication(CONSULTATION_ID, application)).thenReturn(consultation);
    when(fieldConsentsFileService.getFileNotFoundException(FILE_ID, fileUsage)).thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(fieldConsentsFileService)
        .throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(APPLICATION_ID, CONSULTATION_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

}
