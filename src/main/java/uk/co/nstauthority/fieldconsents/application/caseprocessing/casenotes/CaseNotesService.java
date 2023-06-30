package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class CaseNotesService {

  private final Clock clock;
  private final CaseNotesRepository caseNotesRepository;
  private final CaseNotesDocumentService caseNotesDocumentService;

  public CaseNotesService(Clock clock, CaseNotesRepository caseNotesRepository,
                          CaseNotesDocumentService caseNotesDocumentService) {
    this.clock = clock;
    this.caseNotesRepository = caseNotesRepository;
    this.caseNotesDocumentService = caseNotesDocumentService;
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
    caseNotesRepository.save(caseNote);
    caseNotesDocumentService.saveDocuments(caseNote, caseNoteDocuments);
  }
}
