package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.teams.Team;

@Entity
@Table(name = "application_consultations")
public class Consultation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "request_application_version_id")
  private ApplicationVersion requestApplicationVersion;

  @ManyToOne
  @JoinColumn(name = "consultation_team_id")
  private Team consultationTeam;

  private Instant requestDeadline;

  private Instant requestedAtDateTime;

  private Long requestedByWuaId;

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

  public Team getConsultationTeam() {
    return consultationTeam;
  }

  public void setConsultationTeam(Team consultationTeam) {
    this.consultationTeam = consultationTeam;
  }

  public Instant getRequestDeadline() {
    return requestDeadline;
  }

  public void setRequestDeadline(Instant requestDeadline) {
    this.requestDeadline = requestDeadline;
  }

  public Instant getRequestedAtDateTime() {
    return requestedAtDateTime;
  }

  public void setRequestedAtDateTime(Instant requestedAtTimestamp) {
    this.requestedAtDateTime = requestedAtTimestamp;
  }

  public Long getRequestedByWuaId() {
    return requestedByWuaId;
  }

  public void setRequestedByWuaId(Long requestedByWuaId) {
    this.requestedByWuaId = requestedByWuaId;
  }
}
