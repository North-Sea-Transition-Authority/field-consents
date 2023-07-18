package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.util.List;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;

public class CaseEventView {

  private String headerText;

  private String mainUserInvolvedLabel;

  private String mainUserInvolvedFullName;

  private String otherUserInvolvedLabel;

  private String otherUserInvolvedFullName;

  private String eventDateTimeLabel;

  private String eventDateTimeText;

  private String applicationVersionNumber;

  private String eventTextLabel;

  private String eventText;

  private List<UploadedFile> uploadedFiles;

  public CaseEventView(String headerText,
                       String mainUserInvolvedLabel,
                       String mainUserInvolvedFullName,
                       String otherUserInvolvedLabel,
                       String otherUserInvolvedFullName,
                       String eventDateTimeLabel,
                       String eventDateTimeText,
                       String applicationVersionNumber,
                       String eventTextLabel,
                       String eventText,
                       List<UploadedFile> uploadedFiles) {
    this.headerText = headerText;
    this.mainUserInvolvedLabel = mainUserInvolvedLabel;
    this.mainUserInvolvedFullName = mainUserInvolvedFullName;
    this.otherUserInvolvedLabel = otherUserInvolvedLabel;
    this.otherUserInvolvedFullName = otherUserInvolvedFullName;
    this.eventDateTimeLabel = eventDateTimeLabel;
    this.eventDateTimeText = eventDateTimeText;
    this.applicationVersionNumber = applicationVersionNumber;
    this.eventTextLabel = eventTextLabel;
    this.eventText = eventText;
    this.uploadedFiles = uploadedFiles;
  }

  public CaseEventView() {
  }

  public String getHeaderText() {
    return headerText;
  }

  public String getMainUserInvolvedLabel() {
    return mainUserInvolvedLabel;
  }

  public String getMainUserInvolvedFullName() {
    return mainUserInvolvedFullName;
  }

  public String getOtherUserInvolvedLabel() {
    return otherUserInvolvedLabel;
  }

  public String getOtherUserInvolvedFullName() {
    return otherUserInvolvedFullName;
  }

  public String getEventDateTimeLabel() {
    return eventDateTimeLabel;
  }

  public String getEventDateTimeText() {
    return eventDateTimeText;
  }

  public String getApplicationVersion() {
    return applicationVersionNumber;
  }

  public String getEventTextLabel() {
    return eventTextLabel;
  }

  public String getEventText() {
    return eventText;
  }

  public List<UploadedFile> getUploadedFiles() {
    return uploadedFiles;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String headerText;

    private String mainUserInvolvedLabel;

    private String mainUserInvolvedFullName;

    private String otherUserInvolvedLabel;

    private String otherUserInvolvedFullName;

    private String eventDateTimeLabel;

    private String eventDateTimeText;

    private String applicationVersionNumber;

    private String eventTextLabel;

    private String eventText;

    private List<UploadedFile> uploadedFiles;

    private Builder() {
    }

    public Builder withApplicationVersionNumber(String applicationVersionNumber) {
      this.applicationVersionNumber = applicationVersionNumber;
      return this;
    }

    public Builder withHeaderText(String headerText) {
      this.headerText = headerText;
      return this;
    }

    public Builder withEventDateTimeLabel(String eventDateTimeLabel) {
      this.eventDateTimeLabel = eventDateTimeLabel;
      return this;
    }

    public Builder withEventDateTimeText(String eventDateTimeText) {
      this.eventDateTimeText = eventDateTimeText;
      return this;
    }

    public Builder withEventTextLabel(String eventTextLabel) {
      this.eventTextLabel = eventTextLabel;
      return this;
    }

    public Builder withEventText(String eventText) {
      this.eventText = eventText;
      return this;
    }

    public Builder withMainUserInvolvedLabel(String mainUserInvolvedLabel) {
      this.mainUserInvolvedLabel = mainUserInvolvedLabel;
      return this;
    }

    public Builder withMainUserInvolvedFullName(String mainUserInvolvedFullName) {
      this.mainUserInvolvedFullName = mainUserInvolvedFullName;
      return this;
    }

    public Builder withOtherUserInvolvedLabel(String otherUserInvolvedLabel) {
      this.otherUserInvolvedLabel = otherUserInvolvedLabel;
      return this;
    }

    public Builder withOtherUserInvolvedFullName(String otherUserInvolvedFullName) {
      this.otherUserInvolvedFullName = otherUserInvolvedFullName;
      return this;
    }

    public Builder withUploadedFiles(List<UploadedFile> uploadedFiles) {
      this.uploadedFiles = uploadedFiles;
      return this;
    }

    public CaseEventView build() {
      return new CaseEventView(
          headerText,
          mainUserInvolvedLabel,
          mainUserInvolvedFullName,
          otherUserInvolvedLabel,
          otherUserInvolvedFullName,
          eventDateTimeLabel,
          eventDateTimeText,
          applicationVersionNumber,
          eventTextLabel,
          eventText,
          uploadedFiles
      );
    }
  }
}
