package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.time.Instant;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public record CaseEvent(ApplicationVersion applicationVersion,
                        CaseEventType eventType,
                        Long mainEventUserWuaId,
                        Long otherEventUserWuaId,
                        Instant eventDateTime,
                        String eventText,
                        List<UploadedFile> eventFiles) {

  public static Builder builder(ApplicationVersion applicationVersion) {
    return new Builder(applicationVersion);
  }

  public static class Builder {

    private final ApplicationVersion applicationVersion;

    private CaseEventType eventType;

    private Long mainEventUserWuaId;

    private Long otherEventUserWuaId;

    private Instant eventDateTime;

    private String eventText;

    private List<UploadedFile> eventFiles;

    private Builder(ApplicationVersion applicationVersion) {
      this.applicationVersion = applicationVersion;
    }

    public Builder withEventType(CaseEventType eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder withMainEventUserWuaId(Long mainEventUserWuaId) {
      this.mainEventUserWuaId = mainEventUserWuaId;
      return this;
    }

    public Builder withOtherEventUserWuaId(Long otherEventUserWuaId) {
      this.otherEventUserWuaId = otherEventUserWuaId;
      return this;
    }

    public Builder withEventDateTime(Instant eventDateTime) {
      this.eventDateTime = eventDateTime;
      return this;
    }

    public Builder withEventText(String eventText) {
      this.eventText = eventText;
      return this;
    }

    public Builder withEventFiles(List<UploadedFile> eventFiles) {
      this.eventFiles = eventFiles;
      return this;
    }

    public CaseEvent build() {
      return new CaseEvent(
          applicationVersion,
          eventType,
          mainEventUserWuaId,
          otherEventUserWuaId,
          eventDateTime,
          eventText,
          eventFiles
      );
    }
  }
}
