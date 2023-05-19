package uk.co.nstauthority.fieldconsents.workarea;

import java.io.Serial;
import java.io.Serializable;
import org.springframework.web.bind.annotation.SessionAttributes;

@SessionAttributes({"workAreaFilter"})
public class WorkAreaFilter extends WorkAreaForm implements Serializable {

  @Serial
  private static final long serialVersionUID = 8791625085927579692L;

  public void clearFilter() {
    referenceNumber = null;
    statuses = null;
    applicationTypes = null;
    durationTypes = null;
    assetKey = null;
    operatorId = null;
    geographicAreas = null;
  }

  public void update(WorkAreaForm form) {
    referenceNumber = form.getReferenceNumber();
    statuses = form.getStatuses();
    applicationTypes = form.getApplicationTypes();
    durationTypes = form.getDurationTypes();
    assetKey = form.getAssetKey();
    operatorId = form.getOperatorId();
    geographicAreas = form.getGeographicAreas();
  }
}
