package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;

public class CaseNoteForm {

  private final StringInput caseNoteText;

  private List<UploadedFileForm> documents = new ArrayList<>();

  public CaseNoteForm() {
    this.caseNoteText = new StringInput("caseNoteText", "the case note");
  }

  public StringInput getCaseNoteText() {
    return caseNoteText;
  }

  public void setCaseNoteText(String caseNoteText) {
    this.caseNoteText.setInputValue(caseNoteText);
  }

  public List<UploadedFileForm> getDocuments() {
    return documents;
  }

  public void setDocuments(List<UploadedFileForm> documents) {
    this.documents = documents;
  }
}
