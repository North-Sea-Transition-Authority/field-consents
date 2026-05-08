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
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Team;

@Entity
@Audited
@Table(name = "application_consultations")
@NamedEntityGraph(
    name = "consultation",
    attributeNodes = {
        @NamedAttributeNode("requestApplicationVersion"),
        @NamedAttributeNode("responseApplicationVersion"),
        @NamedAttributeNode("consultationTeam")
    }
)
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

  public static Builder newBuilder() {
    return new Builder();
  }

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

  public static class Builder {

    private Integer id;
    private ApplicationVersion requestApplicationVersion;
    private ApplicationVersion responseApplicationVersion;
    private Team consultationTeam;
    private ConsultationStatus status;
    private Instant requestDeadline;
    private Instant requestedAtDatetime;
    private Long requestedByWuaId;
    private Long responderWuaId;
    private Instant respondedAtDatetime;
    private Long respondedByWuaId;
    private HabitatsRegsResponseType habitatsRegsResponseType;
    private String habitatsRegsResponseDescription;
    private EiaRegsResponseType eiaRegsResponseType;
    private String eiaRegsResponseDescription;

    public Builder withId(int id) {
      this.id = id;
      return this;
    }

    public Builder withRequestApplicationVersion(ApplicationVersion applicationVersion) {
      this.requestApplicationVersion = applicationVersion;
      return this;
    }

    public Builder withResponseApplicationVersion(ApplicationVersion applicationVersion) {
      this.responseApplicationVersion = applicationVersion;
      return this;
    }

    public Builder withConsultationTeam(Team team) {
      this.consultationTeam = team;
      return this;
    }

    public Builder withStatus(ConsultationStatus status) {
      this.status = status;
      return this;
    }

    public Builder withRequestDeadline(Instant deadline) {
      this.requestDeadline = deadline;
      return this;
    }

    public Builder withRequestedAt(Instant requestedAt) {
      this.requestedAtDatetime = requestedAt;
      return this;
    }

    public Builder withRequestedBy(WebUserAccountId wuaId) {
      this.requestedByWuaId = wuaId.id();
      return this;
    }

    public Builder withResponder(WebUserAccountId wuaId) {
      this.responderWuaId = wuaId.id();
      return this;
    }

    public Builder withRespondedAt(Instant respondedAt) {
      this.respondedAtDatetime = respondedAt;
      return this;
    }

    public Builder withRespondedBy(WebUserAccountId wuaId) {
      this.respondedByWuaId = wuaId.id();
      return this;
    }

    public Builder withHabitatsRegsResponseType(HabitatsRegsResponseType responseType) {
      this.habitatsRegsResponseType = responseType;
      return this;
    }

    public Builder withHabitatsRegsResponseDescription(String description) {
      this.habitatsRegsResponseDescription = description;
      return this;
    }

    public Builder withEiaRegsResponseType(EiaRegsResponseType responseType) {
      this.eiaRegsResponseType = responseType;
      return this;
    }

    public Builder withEiaRegsResponseDescription(String description) {
      this.eiaRegsResponseDescription = description;
      return this;
    }

    public Consultation build() {
      var consultation = new Consultation();

      consultation.setId(id);
      consultation.setRequestApplicationVersion(requestApplicationVersion);
      consultation.setResponseApplicationVersion(responseApplicationVersion);
      consultation.setConsultationTeam(consultationTeam);
      consultation.setStatus(status);
      consultation.setRequestDeadline(requestDeadline);
      consultation.setRequestedAtDatetime(requestedAtDatetime);
      consultation.setRequestedByWuaId(requestedByWuaId);
      consultation.setResponderWuaId(responderWuaId);
      consultation.setRespondedAtDatetime(respondedAtDatetime);
      consultation.setRespondedByWuaId(respondedByWuaId);
      consultation.setHabitatsRegsResponseType(habitatsRegsResponseType);
      consultation.setHabitatsRegsResponseDescription(habitatsRegsResponseDescription);
      consultation.setEiaRegsResponseType(eiaRegsResponseType);
      consultation.setEiaRegsResponseDescription(eiaRegsResponseDescription);

      return consultation;
    }

    private Builder() {

    }

  }

}
