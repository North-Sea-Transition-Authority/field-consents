package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;

public class TechnicalReviewAssignmentForm {

  private WebUserAccountId technicalReviewerWuaId;

  public WebUserAccountId getTechnicalReviewerWuaId() {
    return technicalReviewerWuaId;
  }

  public void setTechnicalReviewerWuaId(WebUserAccountId technicalReviewerWuaId) {
    this.technicalReviewerWuaId = technicalReviewerWuaId;
  }
}
