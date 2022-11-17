package uk.co.nstauthority.fieldconsents.application;

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

  private Integer createdByWuaId;

  public Application(Integer id, ApplicationType type,
                     Instant createdDate, Integer createdByWuaId) {
    this.id = id;
    this.type = type;
    this.createdDate = createdDate;
    this.createdByWuaId = createdByWuaId;
  }

  public Application() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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

  public Integer getCreatedByWuaId() {
    return createdByWuaId;
  }

  public void setCreatedByWuaId(Integer createdByWuaId) {
    this.createdByWuaId = createdByWuaId;
  }
}
