package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;

@Entity
@Table(name = "application_consultation_further_information_requests")
public class FurtherInformationRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "consultation_id")
  private Consultation consultation;

  private Instant requestedAtDatetime;

  private Long requestedByWuaId;

  private String requestText;

  @Enumerated(EnumType.STRING)
  private FurtherInformationRequestStatus status;

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

  public FurtherInformationRequestStatus getStatus() {
    return status;
  }

  public void setStatus(FurtherInformationRequestStatus status) {
    this.status = status;
  }
}
