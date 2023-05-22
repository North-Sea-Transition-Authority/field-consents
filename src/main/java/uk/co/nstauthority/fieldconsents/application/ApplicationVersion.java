package uk.co.nstauthority.fieldconsents.application;

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
import org.hibernate.envers.Audited;

@Entity
@Table(name = "application_versions")
public class ApplicationVersion {
  @Id
  @Audited
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

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

  @Audited
  @Enumerated(EnumType.STRING)
  private ApplicationVersionStatus status;

  public ApplicationVersion() {
  }

  @VisibleForTesting
  public ApplicationVersion(Integer id, Application application, Integer version, Integer primaryOperatorOuId,
                            String cachedPrimaryOperatorName, Instant createdDateTime, Long createdByWuaId,
                            ApplicationVersionStatus status) {
    this.id = id;
    this.application = application;
    this.version = version;
    this.primaryOperatorOuId = primaryOperatorOuId;
    this.cachedPrimaryOperatorName = cachedPrimaryOperatorName;
    this.createdDateTime = createdDateTime;
    this.createdByWuaId = createdByWuaId;
    this.status = status;
  }

  public Integer getId() {
    return id;
  }

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
}
