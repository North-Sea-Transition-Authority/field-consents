package uk.co.nstauthority.fieldconsents.application;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "applications")
public class Application {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  private ApplicationType type;

  private Instant createdDate;

  private Long createdByWuaId;

  private Integer variationNo;

  private Integer applicationNo;

  @VisibleForTesting
  public Application(Integer id, ApplicationType type, Instant createdDate, Long createdByWuaId,
                     Integer variationNo, Integer applicationNo) {
    this.id = id;
    this.type = type;
    this.createdDate = createdDate;
    this.createdByWuaId = createdByWuaId;
    this.variationNo = variationNo;
    this.applicationNo = applicationNo;
  }

  public Application() {
  }

  public Integer getId() {
    return id;
  }

  public ApplicationType getType() {
    return type;
  }

  public void setType(ApplicationType type) {
    this.type = type;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Instant createdDate) {
    this.createdDate = createdDate;
  }

  public LocalDate getCreatedLocalDate() {
    return LocalDate.ofInstant(this.createdDate, ZoneId.systemDefault());
  }

  public Long getCreatedByWuaId() {
    return createdByWuaId;
  }

  public void setCreatedByWuaId(Long createdByWuaId) {
    this.createdByWuaId = createdByWuaId;
  }

  public Integer getVariationNo() {
    return variationNo;
  }

  public void setVariationNo(Integer variationNumber) {
    this.variationNo = variationNumber;
  }

  public Integer getApplicationNo() {
    return applicationNo;
  }

  public void setApplicationNo(Integer applicationNo) {
    this.applicationNo = applicationNo;
  }
}
