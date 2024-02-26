package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("/applications/{applicationId}/case-notes/{caseNoteId}/files")
public class CaseNoteFileController {

  private final FileControllerHelperService fileControllerHelperService;
  private final ApplicationService applicationService;
  private final CaseNotesService caseNotesService;

  CaseNoteFileController(
      FileControllerHelperService fileControllerHelperService,
      ApplicationService applicationService,
      CaseNotesService caseNotesService
  ) {
    this.fileControllerHelperService = fileControllerHelperService;
    this.applicationService = applicationService;
    this.caseNotesService = caseNotesService;
  }

  @GetMapping("/{fileId}")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS)
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable Integer caseNoteId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.download(fileId, () -> getFileUsage(applicationId, caseNoteId), userDetail);
  }

  @PostMapping("/delete/{fileId}")
  @ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
  public ResponseEntity<FileDeleteResponse> delete(
      @PathVariable Integer applicationId,
      @PathVariable Integer caseNoteId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.delete(fileId, () -> getFileUsage(applicationId, caseNoteId), userDetail);
  }

  private FieldConsentsFileUsage getFileUsage(Integer applicationId, Integer caseNoteId) {
    var application = applicationService.getApplicationById(applicationId);
    var caseNote = caseNotesService.getCaseNoteByIdAndApplication(caseNoteId, application);
    return CaseNoteFileUsage.fromCaseNote(caseNote);
  }

}
