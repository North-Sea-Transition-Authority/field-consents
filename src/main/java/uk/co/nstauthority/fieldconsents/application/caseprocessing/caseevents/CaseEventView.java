package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.time.Instant;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

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

    private List<UploadedFile> uploadedFiles = List.of();

    private Builder() {
    }

    public Builder withApplicationVersion(ApplicationVersion applicationVersion) {
      this.applicationVersionNumber = applicationVersion.getVersion().toString();
      return this;
    }

    public Builder withEventDateTime(Instant eventInstant) {
      this.eventDateTimeText = DateUtils.format(eventInstant, DateUtils.DATE_TIME);
      return this;
    }

    public Builder withEventText(String eventText) {
      this.eventText = eventText;
      return this;
    }

    public Builder withMainUser(EnergyPortalUserDto mainUser) {
      this.mainUserInvolvedFullName = mainUser.displayName();
      return this;
    }

    public Builder withOtherUser(EnergyPortalUserDto otherUser) {
      this.otherUserInvolvedFullName = otherUser.displayName();
      return this;
    }

    public Builder withEventType(CaseEventType caseEventType) {
      this.headerText = caseEventType.getCaseEventHeader();
      this.mainUserInvolvedLabel = caseEventType.getCaseEventUserLabel();
      this.otherUserInvolvedLabel = caseEventType.getOtherEventUserLabel();
      this.eventDateTimeLabel = caseEventType.getCaseEventDateTimeLabel();
      this.eventTextLabel = caseEventType.getCaseEventTextLabel();
      return this;
    }

    public Builder withFiles(List<UploadedFile> files) {
      if (files != null) {
        //Want to preserve the empty list
        this.uploadedFiles = files;
      }
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
