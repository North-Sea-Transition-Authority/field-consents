package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Table(name = "flares")
public class Flare {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Integer flareNo;

  @Enumerated(EnumType.STRING)
  private FlareType flareType;

  private String description;

  private Boolean meteredFlag;

  private String comments;

  public Flare() {
  }

  public Flare(ApplicationVersion applicationVersion, Integer flareNo, FlareType flareType, String description,
               Boolean meteredFlag, String comments) {
    this.flareNo = flareNo;
    this.applicationVersion = applicationVersion;
    this.flareType = flareType;
    this.description = description;
    this.meteredFlag = meteredFlag;
    this.comments = comments;
  }

  static Flare newFromForm(ApplicationVersion applicationVersion, Integer flareNo, FlareForm flareForm) {
    return new Flare(applicationVersion,
        flareNo,
        flareForm.getFlareType(),
        flareForm.getDescription().getInputValue(),
        flareForm.getMeteredFlag(),
        Boolean.TRUE.equals(flareForm.getMeteredFlag()) ? flareForm.getCommentsMeteredYes().getInputValue() :
            flareForm.getCommentsMeteredNo().getInputValue()
    );
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Integer getFlareNo() {
    return flareNo;
  }

  public void setFlareNo(Integer flareNo) {
    this.flareNo = flareNo;
  }

  public FlareType getFlareType() {
    return flareType;
  }

  public void setFlareType(FlareType flareType) {
    this.flareType = flareType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getMeteredFlag() {
    return meteredFlag;
  }

  public void setMeteredFlag(Boolean meteredFlag) {
    this.meteredFlag = meteredFlag;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  void updateFromForm(FlareForm flareForm) {
    this.flareType = flareForm.getFlareType();
    this.description = flareForm.getDescription().getInputValue();
    this.meteredFlag = flareForm.getMeteredFlag();
    this.comments = Boolean.TRUE.equals(flareForm.getMeteredFlag()) ? flareForm.getCommentsMeteredYes().getInputValue() :
        flareForm.getCommentsMeteredNo().getInputValue();
  }

}
