package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;

@Entity
@Audited
@Table(name = "application_updates")
@NamedEntityGraph(
    name = "applicationUpdate",
    attributeNodes = {@NamedAttributeNode("applicationVersion"), @NamedAttributeNode("responseApplicationVersion")}
)
public class ApplicationUpdate {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Long requestedByWuaId;

  private Instant requestedDateTime;

  private String requestText;

  private Instant deadlineDateTime;

  private Long respondedByWuaId;

  private Instant respondedDateTime;

  private String responseText;

  @Enumerated(EnumType.STRING)
  private ApplicationUpdateResponseType responseType;

  @Enumerated(EnumType.STRING)
  private ApplicationUpdateStatus applicationUpdateStatus;

  @OneToOne
  @JoinColumn(name = "response_application_version_id")
  private ApplicationVersion responseApplicationVersion;

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Long getRequestedByWuaId() {
    return requestedByWuaId;
  }

  public void setRequestedByWuaId(Long requestedByWuaId) {
    this.requestedByWuaId = requestedByWuaId;
  }

  public Instant getRequestedDateTime() {
    return requestedDateTime;
  }

  public void setRequestedDateTime(Instant requestedDateTime) {
    this.requestedDateTime = requestedDateTime;
  }

  public String getRequestText() {
    return requestText;
  }

  public void setRequestText(String requestText) {
    this.requestText = requestText;
  }

  public Instant getDeadlineDateTime() {
    return deadlineDateTime;
  }

  public void setDeadlineDateTime(Instant deadlineDateTime) {
    this.deadlineDateTime = deadlineDateTime;
  }

  public Long getRespondedByWuaId() {
    return respondedByWuaId;
  }

  public void setRespondedByWuaId(Long respondedByWuaId) {
    this.respondedByWuaId = respondedByWuaId;
  }

  public Instant getRespondedDateTime() {
    return respondedDateTime;
  }

  public void setRespondedDateTime(Instant respondedDateTime) {
    this.respondedDateTime = respondedDateTime;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  public ApplicationUpdateResponseType getResponseType() {
    return responseType;
  }

  public void setResponseType(ApplicationUpdateResponseType responseType) {
    this.responseType = responseType;
  }

  public ApplicationUpdateStatus getApplicationUpdateStatus() {
    return applicationUpdateStatus;
  }

  public void setApplicationUpdateStatus(
      ApplicationUpdateStatus applicationUpdateStatus) {
    this.applicationUpdateStatus = applicationUpdateStatus;
  }

  public ApplicationVersion getResponseApplicationVersion() {
    return responseApplicationVersion;
  }

  public void setResponseApplicationVersion(
      ApplicationVersion responseApplicationVersion) {
    this.responseApplicationVersion = responseApplicationVersion;
  }
}
