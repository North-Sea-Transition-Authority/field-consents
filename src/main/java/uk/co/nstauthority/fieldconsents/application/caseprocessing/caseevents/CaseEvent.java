package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.time.Instant;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

public record CaseEvent(ApplicationVersion applicationVersion,
                        CaseEventType eventType,
                        Long mainEventUserWuaId,
                        Long otherEventUserWuaId,
                        Instant eventDateTime,
                        String eventText,
                        List<SummaryFileView> summaryFileViews) {

  public static Builder builder(ApplicationVersion applicationVersion) {
    return new Builder(applicationVersion);
  }

  public static Builder newBuilderForAuditRevision(AuditRevision auditRevision, ApplicationVersion applicationVersion) {
    return builder(applicationVersion)
        .withEventDateTime(auditRevision.getCreatedDateTime().toInstant())
        .withMainEventUserWuaId(auditRevision.getUserWuaId());
  }

  public static class Builder {

    private final ApplicationVersion applicationVersion;

    private CaseEventType eventType;

    private Long mainEventUserWuaId;

    private Long otherEventUserWuaId;

    private Instant eventDateTime;

    private String eventText;

    private List<SummaryFileView> summaryFileViews;

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

    public Builder withFileSummaryViews(List<SummaryFileView> summaryFileViews) {
      this.summaryFileViews = summaryFileViews;
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
          summaryFileViews
      );
    }
  }
}
