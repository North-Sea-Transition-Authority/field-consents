package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class SupportingInformationForm {

  private final StringInput notes;

  private final StringInput erapNotes;

  private ApplicationVersion applicationVersion;

  public SupportingInformationForm() {
    this.notes = new StringInput("notes", "Notes");
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

  public static SupportingInformationForm from(SupportingInformation supportingInformation) {
    SupportingInformationForm form = new SupportingInformationForm();

    form.setNotes(supportingInformation.getNotes());
    form.setErapNotes(supportingInformation.getErapNotes());

    return form;
  }
}
