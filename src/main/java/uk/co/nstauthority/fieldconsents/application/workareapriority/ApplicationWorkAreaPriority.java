package uk.co.nstauthority.fieldconsents.application.workareapriority;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Table(name = "application_work_area_priorities")
public class ApplicationWorkAreaPriority {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Enumerated(EnumType.STRING)
  private ApplicationWorkAreaPriorityGroup workAreaPriorityGroup;

  @Enumerated(EnumType.STRING)
  private ApplicationWorkAreaPriorityReason workAreaPriorityReason;

  private Instant workAreaPriorityDateTime;

  private Long workAreaPriorityByWuaId;

  public ApplicationWorkAreaPriority() {
  }

  public ApplicationWorkAreaPriority(ApplicationVersion applicationVersion,
                                     ApplicationWorkAreaPriorityGroup workAreaPriorityGroup,
                                     ApplicationWorkAreaPriorityReason workAreaPriorityReason,
                                     Instant workAreaPriorityDateTime,
                                     Long workAreaPriorityByWuaId) {
    this.applicationVersion = applicationVersion;
    this.workAreaPriorityGroup = workAreaPriorityGroup;
    this.workAreaPriorityReason = workAreaPriorityReason;
    this.workAreaPriorityDateTime = workAreaPriorityDateTime;
    this.workAreaPriorityByWuaId = workAreaPriorityByWuaId;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public ApplicationWorkAreaPriorityGroup getWorkAreaPriorityGroup() {
    return workAreaPriorityGroup;
  }

  public void setWorkAreaPriorityGroup(ApplicationWorkAreaPriorityGroup lastUpdatedType) {
    this.workAreaPriorityGroup = lastUpdatedType;
  }

  public ApplicationWorkAreaPriorityReason getWorkAreaPriorityReason() {
    return workAreaPriorityReason;
  }

  public void setWorkAreaPriorityReason(ApplicationWorkAreaPriorityReason workAreaPriorityReason) {
    this.workAreaPriorityReason = workAreaPriorityReason;
  }

  public Instant getWorkAreaPriorityDateTime() {
    return workAreaPriorityDateTime;
  }

  public void setWorkAreaPriorityDateTime(Instant lastUpdatedDateTime) {
    this.workAreaPriorityDateTime = lastUpdatedDateTime;
  }

  public Long getWorkAreaPriorityByWuaId() {
    return workAreaPriorityByWuaId;
  }

  public void setWorkAreaPriorityByWuaId(Long lastUpdatedByWuaId) {
    this.workAreaPriorityByWuaId = lastUpdatedByWuaId;
  }
}
