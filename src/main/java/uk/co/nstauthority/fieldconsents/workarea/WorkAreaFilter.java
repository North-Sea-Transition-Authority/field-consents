package uk.co.nstauthority.fieldconsents.workarea;

import java.io.Serial;
import java.io.Serializable;
import org.springframework.web.bind.annotation.SessionAttributes;

@SessionAttributes({"workAreaFilter"})
public class WorkAreaFilter extends WorkAreaFilterForm implements Serializable {

  @Serial
  private static final long serialVersionUID = 8791625085927579692L;

  public void clearSession() {
    super.clearFilter();
    assetKey = null;
    caseOfficerWuaId = null;
    technicalReviewerWuaId = null;
  }

  public void update(WorkAreaFilterForm form) {
    super.update(form);
    assetKey = form.getAssetKey();
    caseOfficerWuaId = form.getCaseOfficerWuaId();
    technicalReviewerWuaId = form.getTechnicalReviewerWuaId();
  }
}
