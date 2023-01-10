package uk.co.nstauthority.fieldconsents.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "application_versions")
public class ApplicationVersion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_id")
  private Application application;

  @Column(name = "version_no")
  private Integer version;

  private Integer primaryOperatorOuId;

  private String cachedPrimaryOperatorName;

  public ApplicationVersion() {
  }

  public ApplicationVersion(Integer id, Application application, Integer version, Integer primaryOperatorOuId,
                            String cachedPrimaryOperatorName) {
    this.id = id;
    this.application = application;
    this.version = version;
    this.primaryOperatorOuId = primaryOperatorOuId;
    this.cachedPrimaryOperatorName = cachedPrimaryOperatorName;
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
}
