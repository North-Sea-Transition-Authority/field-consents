package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
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

  @GetMapping("case-notes/{caseNoteId}/documents/{fileId}")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS)
  ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable Integer caseNoteId,
      @PathVariable UUID fileId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var caseNote = caseNotesService.getCaseNoteByIdAndApplication(caseNoteId, application);
    var fileUsage = CaseNoteFileUsage.fromCaseNote(caseNote);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> fieldConsentsFileService.getFileNotFoundException(fileId, fileUsage));

    fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage);

    return fileService.download(uploadedFile);
  }
}
