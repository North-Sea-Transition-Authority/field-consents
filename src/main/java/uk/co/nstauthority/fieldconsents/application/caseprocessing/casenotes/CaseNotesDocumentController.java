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
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;


@RestController
@RequestMapping("applications/{applicationId}")
public class CaseNotesDocumentController {

  private final FileService fileService;
  private final ApplicationService applicationService;
  private final CaseNotesService caseNotesService;
  private final FieldConsentsFileService fieldConsentsFileService;

  CaseNotesDocumentController(
      FileService fileService,
      ApplicationService applicationService,
      CaseNotesService caseNotesService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.fileService = fileService;
    this.applicationService = applicationService;
    this.caseNotesService = caseNotesService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @PostMapping("case-note-documents")
  @ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
  FileUploadResponse upload(@PathVariable Integer applicationId, MultipartFile file, ServiceUserDetail userDetail) {
    return fileService.upload(builder -> builder
        .withMultipartFile(file)
        .withUploadedBy(userDetail.wuaId().toString())
        .build());
  }

  @GetMapping("case-notes/{caseNoteId}/documents/{fileId}")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS)
  ResponseEntity<InputStreamResource> download(@PathVariable Integer applicationId,
                                               @PathVariable Integer caseNoteId,
                                               @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, caseNoteId, fileId, fileService::download);
  }

  @PostMapping("case-notes/{caseNoteId}/documents/delete/{fileId}")
  @ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
  FileDeleteResponse delete(@PathVariable Integer applicationId,
                            @PathVariable Integer caseNoteId,
                            @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, caseNoteId, fileId, fileService::delete);
  }

  private <T> T findFileAndThen(Integer applicationId, Integer caseNoteId, UUID fileId,
                                Function<UploadedFile, T> andThen) {
    var application = applicationService.getApplicationById(applicationId);
    var caseNote = caseNotesService.getCaseNoteByIdAndApplication(caseNoteId, application);
    var fileUsage = CaseNoteFileUsage.fromCaseNote(caseNote);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> fieldConsentsFileService.getFileNotFoundException(fileId, fileUsage));

    fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage);

    return andThen.apply(uploadedFile);
  }
}
