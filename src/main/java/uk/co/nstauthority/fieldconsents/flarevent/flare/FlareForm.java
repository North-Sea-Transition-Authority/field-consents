package uk.co.nstauthority.fieldconsents.flarevent.flare;

import uk.co.fivium.formlibrary.input.StringInput;

public class FlareForm {

  private FlareType flareType;

  private StringInput description = new StringInput("description", "Description");

  private Boolean meteredFlag;

  private StringInput commentsMeteredYes = new StringInput("commentsMeteredYes", "Comments");

  private StringInput commentsMeteredNo = new StringInput("commentsMeteredNo", "Comments");

  public FlareForm() {
  }

  public FlareForm(FlareType flareType,
                   StringInput description,
                   Boolean meteredFlag,
                   StringInput commentsMeteredYes,
                   StringInput commentsMeteredNo) {
    this.flareType = flareType;
    this.description = description;
    this.meteredFlag = meteredFlag;
    this.commentsMeteredYes = commentsMeteredYes;
    this.commentsMeteredNo = commentsMeteredNo;
  }

  static FlareForm from(Flare flare) {
    Boolean meteredFlag = flare.getMeteredFlag();
    FlareForm flareForm = new FlareForm();
    flareForm.setFlareType(flare.getFlareType());
    flareForm.getDescription().setInputValue(flare.getDescription());
    flareForm.setMeteredFlag(meteredFlag);
    if (meteredFlag) {
      flareForm.getCommentsMeteredYes().setInputValue(flare.getComments());
    } else {
      flareForm.getCommentsMeteredNo().setInputValue(flare.getComments());
    }
    return flareForm;
  }

  public FlareType getFlareType() {
    return flareType;
  }

  public void setFlareType(FlareType flareType) {
    this.flareType = flareType;
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
