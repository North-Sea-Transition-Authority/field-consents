package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;

import java.util.UUID;
import java.util.function.Function;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;


@RestController
@RequestMapping("applications/{applicationId}/add-case-note/documents")
public class CaseNotesDocumentController {

  private final FileService fileService;

  private final CaseNotesDocumentService caseNoteDocumentService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationVersionFileService applicationVersionFileService;


  public CaseNotesDocumentController(FileService fileService,
                                     CaseNotesDocumentService caseNoteDocumentService,
                                     ApplicationVersionService applicationVersionService,
                                     ApplicationVersionFileService applicationVersionFileService) {
    this.fileService = fileService;
    this.caseNoteDocumentService = caseNoteDocumentService;
    this.applicationVersionService = applicationVersionService;
    this.applicationVersionFileService = applicationVersionFileService;
  }

  @PostMapping
  @ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
  FileUploadResponse upload(@PathVariable Integer applicationId,
                            MultipartFile file,
                            ServiceUserDetail userDetail) {
    return fileService.upload(builder -> builder
        .withMultipartFile(file)
        .withUploadedBy(userDetail.wuaId().toString())
        .build());
  }

  @GetMapping("{fileId}")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS)
  ResponseEntity<InputStreamResource> download(@PathVariable Integer applicationId, @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, fileId, fileService::download);
  }

  @PostMapping("{fileId}")
  @ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
  FileDeleteResponse delete(@PathVariable Integer applicationId, @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, fileId, fileService::delete);
  }

  private <T> T findFileAndThen(Integer applicationId, UUID fileId, Function<UploadedFile, T> andThen) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> applicationVersionFileService.getFileNotFoundException(fileId, applicationVersion));

    caseNoteDocumentService.throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);

    return andThen.apply(uploadedFile);
  }
}
