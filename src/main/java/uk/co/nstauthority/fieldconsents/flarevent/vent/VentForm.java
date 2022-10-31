package uk.co.nstauthority.fieldconsents.flarevent.vent;

import uk.co.fivium.formlibrary.input.StringInput;

public class VentForm {

  private VentType ventType;

  private StringInput description = new StringInput("description", "Description");

  private Boolean meteredFlag;

  private StringInput commentsMeteredYes = new StringInput("commentsMeteredYes", "Comments");

  private StringInput commentsMeteredNo = new StringInput("commentsMeteredNo", "Comments");

  public VentForm() {
  }

  public VentForm(VentType ventType, StringInput description, Boolean meteredFlag, StringInput commentsMeteredYes,
                  StringInput commentsMeteredNo) {
    this.ventType = ventType;
    this.description = description;
    this.meteredFlag = meteredFlag;
    this.commentsMeteredYes = commentsMeteredYes;
    this.commentsMeteredNo = commentsMeteredNo;
  }

  static VentForm from(Vent vent) {
    Boolean meteredFlag = vent.getMeteredFlag();
    VentForm ventForm = new VentForm();
    ventForm.setVentType(vent.getVentType());
    ventForm.getDescription().setInputValue(vent.getDescription());
    ventForm.setMeteredFlag(meteredFlag);
    if (Boolean.TRUE.equals(meteredFlag)) {
      ventForm.getCommentsMeteredYes().setInputValue(vent.getComments());
    } else {
      ventForm.getCommentsMeteredNo().setInputValue(vent.getComments());
    }
    return ventForm;
  }

  public VentType getVentType() {
    return ventType;
  }

  public void setVentType(VentType ventType) {
    this.ventType = ventType;
  }

  public StringInput getDescription() {
    return description;
  }

  public void setDescription(StringInput description) {
    this.description = description;
  }

  public Boolean getMeteredFlag() {
    return meteredFlag;
  }

  public void setMeteredFlag(Boolean meteredFlag) {
    this.meteredFlag = meteredFlag;
  }

  public StringInput getCommentsMeteredYes() {
    return commentsMeteredYes;
  }

  public void setCommentsMeteredYes(StringInput commentsMeteredYes) {
    this.commentsMeteredYes = commentsMeteredYes;
  }

  public StringInput getCommentsMeteredNo() {
    return commentsMeteredNo;
  }

  public void setCommentsMeteredNo(StringInput commentsMeteredNo) {
    this.commentsMeteredNo = commentsMeteredNo;
  }
}
