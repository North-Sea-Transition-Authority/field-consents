package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

public class FlareView {

  private Integer displayOrder;

  private Integer flareNo;

  private String editUrl;

  private String deleteUrl;

  private String flareType;

  private String description;

  private String meteredFlag;

  private String comments;

  public FlareView(
      Integer displayOrder,
      Integer flareNo,
      String editUrl,
      String deleteUrl,
      String flareType,
      String description,
      String meteredFlag,
      String comments
  ) {
    this.displayOrder = displayOrder;
    this.flareNo = flareNo;
    this.editUrl = editUrl;
    this.deleteUrl = deleteUrl;
    this.flareType = flareType;
    this.description = description;
    this.meteredFlag = meteredFlag;
    this.comments = comments;
  }

  public static FlareView from(Flare flare, Integer displayOrder) {

    String editUrl = ReverseRouter.route(on(FlareController.class).editFlare(
        flare.getApplicationVersion().getApplication().getId(),
        flare.getFlareNo()));

    String deleteUrl = ReverseRouter.route(on(FlareController.class).deleteFlareConfirm(
        flare.getApplicationVersion().getApplication().getId(),
        flare.getFlareNo()
    ));

    return new FlareView(
        displayOrder,
        flare.getFlareNo(),
        editUrl,
        deleteUrl,
        flare.getFlareType().getDisplayName(),
        flare.getDescription() == null ? "" : flare.getDescription(),
        BooleanUtil.yesNoFromBoolean(flare.getMeteredFlag()),
        flare.getComments() == null ? "" : flare.getComments());
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public Integer getFlareNo() {
    return flareNo;
  }

  public void setFlareNo(Integer flareNo) {
    this.flareNo = flareNo;
  }

  public String getEditUrl() {
    return editUrl;
  }

  public void setEditUrl(String editUrl) {
    this.editUrl = editUrl;
  }

  public String getDeleteUrl() {
    return deleteUrl;
  }

  public void setDeleteUrl(String deleteUrl) {
    this.deleteUrl = deleteUrl;
  }

  public String getFlareType() {
    return flareType;
  }

  public void setFlareType(String flareType) {
    this.flareType = flareType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMeteredFlag() {
    return meteredFlag;
  }

  public void setMeteredFlag(String meteredFlag) {
    this.meteredFlag = meteredFlag;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }
}
