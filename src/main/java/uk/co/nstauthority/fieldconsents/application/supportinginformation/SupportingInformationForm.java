package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class SupportingInformationForm {

  private final StringInput notes;

  private final StringInput erapNotes;

  private ApplicationVersion applicationVersion;

  private List<UploadedFileForm> supportingDocuments = new ArrayList<>();

  public SupportingInformationForm() {
    this.notes = new StringInput("notes", "notes");
    this.erapNotes = new StringInput("erapNotes", "ERAP alignment studies and projects");
  }

  public StringInput getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes.setInputValue(notes);
  }

  public StringInput getErapNotes() {
    return erapNotes;
  }

  public void setErapNotes(String erapNotes) {
    this.erapNotes.setInputValue(erapNotes);
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public List<UploadedFileForm> getSupportingDocuments() {
    return supportingDocuments;
  }

  public void setSupportingDocuments(List<UploadedFileForm> supportingDocuments) {
    this.supportingDocuments = supportingDocuments;
  }

  public static SupportingInformationForm from(SupportingInformation supportingInformation, List<UploadedFile> files) {
    SupportingInformationForm form = new SupportingInformationForm();

    form.setNotes(supportingInformation.getNotes());
    form.setErapNotes(supportingInformation.getErapNotes());

    var fileForms = files.stream().map(FileUploadLibraryUtils::asForm).toList();
    form.setSupportingDocuments(fileForms);

    return form;
  }
}
