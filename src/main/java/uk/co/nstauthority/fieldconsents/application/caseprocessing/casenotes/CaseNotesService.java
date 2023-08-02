package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@Service
public class CaseNotesService {

  private final Clock clock;
  private final CaseNotesRepository caseNotesRepository;
  private final FieldConsentsFileService fieldConsentsFileService;

  CaseNotesService(
      Clock clock,
      CaseNotesRepository caseNotesRepository,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.clock = clock;
    this.caseNotesRepository = caseNotesRepository;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @Transactional
  public void saveCaseNote(ApplicationVersion applicationVersion,
                           String caseNoteText,
                           List<UploadedFileForm> caseNoteDocuments,
                           ServiceUserDetail user) {
    var caseNote = new CaseNote();
    caseNote.setApplicationVersion(applicationVersion);
    caseNote.setCaseNoteText(caseNoteText);
    caseNote.setAddedByWuaId(user.wuaId());
    caseNote.setAddedDateTime(clock.instant());
    caseNote = caseNotesRepository.save(caseNote);

    var fileUsage = CaseNoteFileUsage.fromCaseNote(caseNote);
    fieldConsentsFileService.saveDocuments(fileUsage, caseNoteDocuments);
  }

  public List<CaseNote> getCaseNotesByApplication(Application application) {
    return caseNotesRepository.findByApplicationVersion_Application(application);
  }

  public CaseNote getCaseNoteByIdAndApplication(Integer caseNoteId, Application application) {
    return caseNotesRepository.findByIdAndApplicationVersion_Application(caseNoteId, application)
        .orElseThrow(() -> new EntityNotFoundException("Case note [%s] not found for application [%s]".formatted(
            caseNoteId, application.getId()
        )));
  }

}
