package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.teams.Team;

@Entity
@Audited
@Table(name = "application_consultations")
public class Consultation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "request_application_version_id")
  private ApplicationVersion requestApplicationVersion;

  @OneToOne
  @JoinColumn(name = "response_application_version_id")
  private ApplicationVersion responseApplicationVersion;

  @ManyToOne
  @JoinColumn(name = "consultation_team_id")
  @NotAudited
  private Team consultationTeam;

  @Enumerated(EnumType.STRING)
  private ConsultationStatus status;

  private Instant requestDeadline;

  private Instant requestedAtDatetime;

  private Long requestedByWuaId;

  private Long responderWuaId;

  private Instant respondedAtDatetime;

  private Long respondedByWuaId;

  @Enumerated(EnumType.STRING)
  private HabitatsRegsResponseType habitatsRegsResponseType;

  private String habitatsRegsResponseDescription;

  @Enumerated(EnumType.STRING)
  private EiaRegsResponseType eiaRegsResponseType;

  private String eiaRegsResponseDescription;

  public Integer getId() {
    return id;
  }

  @VisibleForTesting
  public void setId(Integer id) {
    this.id = id;
  }

  public ApplicationVersion getRequestApplicationVersion() {
    return requestApplicationVersion;
  }

  public void setRequestApplicationVersion(ApplicationVersion requestApplicationVersion) {
    this.requestApplicationVersion = requestApplicationVersion;
  }

  public ApplicationVersion getResponseApplicationVersion() {
    return responseApplicationVersion;
  }

  public void setResponseApplicationVersion(ApplicationVersion responseApplicationVersion) {
    this.responseApplicationVersion = responseApplicationVersion;
  }

  public Team getConsultationTeam() {
    return consultationTeam;
  }

  public void setConsultationTeam(Team consultationTeam) {
    this.consultationTeam = consultationTeam;
  }

  public ConsultationStatus getStatus() {
    return status;
  }

  public void setStatus(ConsultationStatus status) {
    this.status = status;
  }

  public Instant getRequestDeadline() {
    return requestDeadline;
  }

  public void setRequestDeadline(Instant requestDeadline) {
    this.requestDeadline = requestDeadline;
  }

  public Instant getRequestedAtDatetime() {
    return requestedAtDatetime;
  }

  public void setRequestedAtDatetime(Instant requestedAtTimestamp) {
    this.requestedAtDatetime = requestedAtTimestamp;
  }

  public Long getRequestedByWuaId() {
    return requestedByWuaId;
  }

  public void setRequestedByWuaId(Long requestedByWuaId) {
    this.requestedByWuaId = requestedByWuaId;
  }

  public Long getResponderWuaId() {
    return responderWuaId;
  }

  public void setResponderWuaId(Long responderWuaId) {
    this.responderWuaId = responderWuaId;
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

  public HabitatsRegsResponseType getHabitatsRegsResponseType() {
    return habitatsRegsResponseType;
  }

  public void setHabitatsRegsResponseType(HabitatsRegsResponseType responseType) {
    this.habitatsRegsResponseType = responseType;
  }

  public String getHabitatsRegsResponseDescription() {
    return habitatsRegsResponseDescription;
  }

  public void setHabitatsRegsResponseDescription(String responseHabitatsRegulationsDescription) {
    this.habitatsRegsResponseDescription = responseHabitatsRegulationsDescription;
  }

  public EiaRegsResponseType getEiaRegsResponseType() {
    return eiaRegsResponseType;
  }

  public void setEiaRegsResponseType(EiaRegsResponseType responseType) {
    this.eiaRegsResponseType = responseType;
  }

  public String getEiaRegsResponseDescription() {
    return eiaRegsResponseDescription;
  }

  public void setEiaRegsResponseDescription(String responseEiaRegulationsDescription) {
    this.eiaRegsResponseDescription = responseEiaRegulationsDescription;
  }
}
