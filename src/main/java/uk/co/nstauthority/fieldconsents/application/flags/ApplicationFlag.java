package uk.co.nstauthority.fieldconsents.application.flags;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Table(name = "application_flags")
public class ApplicationFlag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Enumerated(EnumType.STRING)
  private ApplicationFlagType flagType;

  private Boolean flagValue;


  @VisibleForTesting
  public ApplicationFlag(Integer id, ApplicationVersion applicationVersion,
                         ApplicationFlagType flagType, Boolean flagValue) {
    this.id = id;
    this.applicationVersion = applicationVersion;
    this.flagType = flagType;
    this.flagValue = flagValue;
  }

  public ApplicationFlag() {
  }

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public ApplicationFlagType getFlagType() {
    return flagType;
  }

  public void setFlagType(ApplicationFlagType flag) {
    this.flagType = flag;
  }

  public Boolean getFlagValue() {
    return flagValue;
  }

  public void setFlagValue(Boolean flagValue) {
    this.flagValue = flagValue;
  }
}
