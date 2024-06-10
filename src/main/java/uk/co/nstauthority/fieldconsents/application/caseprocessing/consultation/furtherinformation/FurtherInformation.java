package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;

@Entity
@Audited
@Table(name = "application_consultation_further_information")
public class FurtherInformation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "consultation_id")
  private Consultation consultation;

  private Instant requestedAtDatetime;

  private Long requestedByWuaId;

  private String requestText;

  private Instant respondedAtDatetime;

  private Long respondedByWuaId;

  private String responseText;

  @Enumerated(EnumType.STRING)
  private FurtherInformationStatus status;

  public Integer getId() {
    return id;
  }

  @VisibleForTesting
  public void setId(Integer id) {
    this.id = id;
  }

  public Consultation getConsultation() {
    return consultation;
  }

  public void setConsultation(Consultation consultation) {
    this.consultation = consultation;
  }

  public Instant getRequestedAtDatetime() {
    return requestedAtDatetime;
  }

  public void setRequestedAtDatetime(Instant requestedAtDatetime) {
    this.requestedAtDatetime = requestedAtDatetime;
  }

  public Long getRequestedByWuaId() {
    return requestedByWuaId;
  }

  public void setRequestedByWuaId(Long requestedByWuaId) {
    this.requestedByWuaId = requestedByWuaId;
  }

  public String getRequestText() {
    return requestText;
  }

  public void setRequestText(String requestText) {
    this.requestText = requestText;
  }

  public Instant getRespondedAtDatetime() {
    return respondedAtDatetime;
  }

  public void setRespondedAtDatetime(Instant respondedAtDatetime) {
    this.respondedAtDatetime = respondedAtDatetime;
  }

  public Long getRespondedByWuaId() {
    return respondedByWuaId;
  }

  public void setRespondedByWuaId(Long respondedByWuaId) {
    this.respondedByWuaId = respondedByWuaId;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  public FurtherInformationStatus getStatus() {
    return status;
  }

  public void setStatus(FurtherInformationStatus status) {
    this.status = status;
  }

}
