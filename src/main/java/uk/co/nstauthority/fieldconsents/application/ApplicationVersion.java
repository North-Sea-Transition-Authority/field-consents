package uk.co.nstauthority.fieldconsents.application;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
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
import java.util.Objects;
import org.hibernate.envers.Audited;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Entity
@Audited
@Table(name = "application_versions")
public class ApplicationVersion implements DomainReference {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Audited(targetAuditMode = NOT_AUDITED)
  @ManyToOne
  @JoinColumn(name = "application_id")
  private Application application;

  @Column(name = "version_no")
  private Integer version;

  private Integer primaryOperatorOuId;

  private String cachedPrimaryOperatorName;

  private Instant createdDateTime;

  private Long createdByWuaId;

  private Instant submittedDateTime;

  private Long submittedByWuaId;

  @Enumerated(EnumType.STRING)
  private ApplicationVersionStatus status;

  private Long caseOfficerWuaId;

  private Long camWuaId;

  @Enumerated(EnumType.STRING)
  private RegulatorTeamRole currentCaseOwner;

  private Boolean migrated;

  public ApplicationVersion() {
  }

  @VisibleForTesting
  public ApplicationVersion(Integer id, Application application, Integer version, Integer primaryOperatorOuId,
                            String cachedPrimaryOperatorName, Instant createdDateTime, Long createdByWuaId,
                            Instant submittedDateTime, Long submittedByWuaId, ApplicationVersionStatus status,
                            Long caseOfficerWuaId, Boolean migrated) {
    this.id = id;
    this.application = application;
    this.version = version;
    this.primaryOperatorOuId = primaryOperatorOuId;
    this.cachedPrimaryOperatorName = cachedPrimaryOperatorName;
    this.createdDateTime = createdDateTime;
    this.createdByWuaId = createdByWuaId;
    this.submittedDateTime = submittedDateTime;
    this.submittedByWuaId = submittedByWuaId;
    this.status = status;
    this.caseOfficerWuaId = caseOfficerWuaId;
    this.migrated = migrated;
  }

  public Integer getId() {
    return id;
  }

  @VisibleForTesting
  public void setId(Integer id) {
    this.id = id;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public boolean isFirstVersion() {
    return this.version == 1;
  }

  public boolean isUpdateVersion() {
    return this.version > 1;
  }

  public Integer getPrimaryOperatorOuId() {
    return primaryOperatorOuId;
  }

  public void setPrimaryOperatorOuId(Integer primaryOperatorOuId) {
    this.primaryOperatorOuId = primaryOperatorOuId;
  }

  public String getCachedPrimaryOperatorName() {
    return cachedPrimaryOperatorName;
  }

  public void setCachedPrimaryOperatorName(String cachedPrimaryOperatorName) {
    this.cachedPrimaryOperatorName = cachedPrimaryOperatorName;
  }

  public Instant getCreatedDateTime() {
    return createdDateTime;
  }

  public void setCreatedDateTime(Instant createdDateTime) {
    this.createdDateTime = createdDateTime;
  }

  public Long getCreatedByWuaId() {
    return createdByWuaId;
  }

  public void setCreatedByWuaId(Long createdByWuaId) {
    this.createdByWuaId = createdByWuaId;
  }

  public Instant getSubmittedDateTime() {
    return submittedDateTime;
  }

  public void setSubmittedDateTime(Instant submittedDate) {
    this.submittedDateTime = submittedDate;
  }

  public Long getSubmittedByWuaId() {
    return submittedByWuaId;
  }

  public void setSubmittedByWuaId(Long submittedByWuaId) {
    this.submittedByWuaId = submittedByWuaId;
  }

  public ApplicationVersionStatus getStatus() {
    return status;
  }

  public void setStatus(ApplicationVersionStatus status) {
    this.status = status;
  }

  public Long getCaseOfficerWuaId() {
    return caseOfficerWuaId;
  }

  public void setCaseOfficerWuaId(Long caseOfficerWuaId) {
    this.caseOfficerWuaId = caseOfficerWuaId;
  }

  public Long getCamWuaId() {
    return camWuaId;
  }

  public void setCamWuaId(Long camWuaId) {
    this.camWuaId = camWuaId;
  }

  public RegulatorTeamRole getCurrentCaseOwner() {
    return currentCaseOwner;
  }

  public void setCurrentCaseOwner(
      RegulatorTeamRole currentCaseOwner) {
    this.currentCaseOwner = currentCaseOwner;
  }

  public Boolean getMigrated() {
    return migrated;
  }

  public void setMigrated(Boolean migrated) {
    this.migrated = migrated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplicationVersion that)) {
      return false;
    }
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String getDomainId() {
    return String.valueOf(id);
  }

  @Override
  public String getDomainType() {
    return "APPLICATION_VERSION";
  }
}
