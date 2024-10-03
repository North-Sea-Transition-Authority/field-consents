package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

public class VentView {

  private Integer displayOrder;

  private Integer ventNo;

  private String editUrl;

  private String deleteUrl;

  private String ventType;

  private String description;

  private String meteredFlag;

  private String comments;

  public VentView(
      Integer displayOrder,
      Integer ventNo,
      String editUrl,
      String deleteUrl,
      String ventType,
      String description,
      String meteredFlag,
      String comments
  ) {
    this.displayOrder = displayOrder;
    this.ventNo = ventNo;
    this.editUrl = editUrl;
    this.deleteUrl = deleteUrl;
    this.ventType = ventType;
    this.description = description;
    this.meteredFlag = meteredFlag;
    this.comments = comments;
  }

  public static VentView from(Vent vent, Integer displayOrder) {

    String editUrl = ReverseRouter.route(on(VentController.class).editVent(
        vent.getApplicationVersion().getApplication().getId(),
        vent.getVentNo()));

    String deleteUrl = ReverseRouter.route(on(VentController.class).deleteVentConfirm(
        vent.getApplicationVersion().getApplication().getId(),
        vent.getVentNo()
    ));

    return new VentView(
        displayOrder,
        vent.getVentNo(),
        editUrl,
        deleteUrl,
        vent.getVentType().getDisplayName(),
        vent.getDescription() == null ? "" : vent.getDescription(),
        BooleanUtil.yesNoFromBoolean(vent.getMeteredFlag()),
        vent.getComments() == null ? "" : vent.getComments());
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public Integer getVentNo() {
    return ventNo;
  }

  public void setVentNo(Integer ventNo) {
    this.ventNo = ventNo;
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

  public String getVentType() {
    return ventType;
  }

  public void setVentType(String ventType) {
    this.ventType = ventType;
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
