package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@Service
public class CaseNoteEventService implements CaseEventService<Application> {

  private final CaseNotesService caseNotesService;
  private final FieldConsentsFileService fieldConsentsFileService;

  CaseNoteEventService(
      CaseNotesService caseNotesService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.caseNotesService = caseNotesService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    return caseNotesService
        .getCaseNotesByApplication(application)
        .stream()
        .map(this::convertToCaseEvent)
        .toList();
  }

  private CaseEvent convertToCaseEvent(CaseNote caseNote) {
    var applicationId = caseNote.getApplicationVersion().getApplication().getId();
    var caseNoteId = caseNote.getId();
    var fileSummaryViews = fieldConsentsFileService
        .getUploadedFiles(CaseNoteFileUsage.fromCaseNote(caseNote))
        .stream()
        .map(uploadedFile -> this.getSummaryFileView(uploadedFile, applicationId, caseNoteId))
        .toList();

    return CaseEvent.builder(caseNote.getApplicationVersion())
        .withEventType(CaseEventType.CASE_NOTE_ADDED)
        .withMainEventUserWuaId(caseNote.getAddedByWuaId())
        .withEventDateTime(caseNote.getAddedDateTime())
        .withEventText(caseNote.getCaseNoteText())
        .withFileSummaryViews(fileSummaryViews)
        .build();
  }

  private SummaryFileView getSummaryFileView(UploadedFile uploadedFile, Integer applicationId, Integer caseNoteId) {
    return SummaryFileView.from(
        uploadedFile,
        ReverseRouter.route(on(CaseNoteFileController.class).download(applicationId, caseNoteId, uploadedFile.getId(), null))
    );
  }

}
