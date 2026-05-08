package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import com.google.common.annotations.VisibleForTesting;
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

@Audited
@Entity
@Table(name = "application_technical_reviews")
@NamedEntityGraph(
    name = "technicalReview",
    attributeNodes = {@NamedAttributeNode("requestApplicationVersion"), @NamedAttributeNode("responseApplicationVersion")}
)
public class TechnicalReview {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "request_application_version_id")
  private ApplicationVersion requestApplicationVersion;

  @OneToOne
  @JoinColumn(name = "response_application_version_id")
  private ApplicationVersion responseApplicationVersion;

  private Long requestedByWuaId;

  private Instant requestedDateTime;

  private String requestText;

  private Instant deadlineDateTime;

  private Long technicalReviewerWuaId;

  private Long respondedByWuaId;

  private Instant respondedDateTime;

  private String responseText;

  @Enumerated(EnumType.STRING)
  private TechnicalReviewResponseType responseType;

  @Enumerated(EnumType.STRING)
  private TechnicalReviewStatus technicalReviewStatus;

  @VisibleForTesting
  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getRequestApplicationVersion() {
    return requestApplicationVersion;
  }

  public void setRequestApplicationVersion(ApplicationVersion applicationVersion) {
    this.requestApplicationVersion = applicationVersion;
  }

  public ApplicationVersion getResponseApplicationVersion() {
    return responseApplicationVersion;
  }

  public void setResponseApplicationVersion(ApplicationVersion responseApplicationVersion) {
    this.responseApplicationVersion = responseApplicationVersion;
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

  public void setDeadlineDateTime(Instant requestDeadlineDateTime) {
    this.deadlineDateTime = requestDeadlineDateTime;
  }

  public Long getTechnicalReviewerWuaId() {
    return technicalReviewerWuaId;
  }

  public void setTechnicalReviewerWuaId(Long technicalReviewerWuaId) {
    this.technicalReviewerWuaId = technicalReviewerWuaId;
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

  public TechnicalReviewResponseType getResponseType() {
    return responseType;
  }

  public void setResponseType(
      TechnicalReviewResponseType responseType) {
    this.responseType = responseType;
  }

  public TechnicalReviewStatus getTechnicalReviewStatus() {
    return technicalReviewStatus;
  }

  public void setTechnicalReviewStatus(
      TechnicalReviewStatus technicalReviewStatus) {
    this.technicalReviewStatus = technicalReviewStatus;
  }
}
