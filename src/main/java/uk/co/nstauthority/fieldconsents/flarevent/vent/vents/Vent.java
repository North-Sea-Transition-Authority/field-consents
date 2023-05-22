package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

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
@Table(name = "vents")
public class Vent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Integer ventNo;

  @Enumerated(EnumType.STRING)
  private VentType ventType;

  private String description;

  private Boolean meteredFlag;

  private String comments;

  public Vent() {
  }

  public Vent(ApplicationVersion applicationVersion, Integer ventNo, VentType ventType, String description,
              Boolean meteredFlag, String comments) {
    this.applicationVersion = applicationVersion;
    this.ventNo = ventNo;
    this.ventType = ventType;
    this.description = description;
    this.meteredFlag = meteredFlag;
    this.comments = comments;
  }

  static Vent newFromForm(ApplicationVersion applicationVersion, Integer ventNo, VentForm ventForm) {
    return new Vent(applicationVersion,
        ventNo,
        ventForm.getVentType(),
        ventForm.getDescription().getInputValue(),
        ventForm.getMeteredFlag(),
        Boolean.TRUE.equals(ventForm.getMeteredFlag()) ? ventForm.getCommentsMeteredYes().getInputValue() :
            ventForm.getCommentsMeteredNo().getInputValue()
    );
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Integer getVentNo() {
    return ventNo;
  }

  public void setVentNo(Integer ventNo) {
    this.ventNo = ventNo;
  }

  public VentType getVentType() {
    return ventType;
  }

  public void setVentType(VentType ventType) {
    this.ventType = ventType;
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

  void updateFromForm(VentForm ventForm) {
    this.ventType = ventForm.getVentType();
    this.description = ventForm.getDescription().getInputValue();
    this.meteredFlag = ventForm.getMeteredFlag();
    this.comments = Boolean.TRUE.equals(ventForm.getMeteredFlag()) ? ventForm.getCommentsMeteredYes().getInputValue() :
        ventForm.getCommentsMeteredNo().getInputValue();
  }

}
