package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;

public class CaseAssignmentForm {

  private WebUserAccountId caseOfficerWuaId;

  public WebUserAccountId getCaseOfficerWuaId() {
    return caseOfficerWuaId;
  }

  public void setCaseOfficerWuaId(WebUserAccountId caseOfficerWuaId) {
    this.caseOfficerWuaId = caseOfficerWuaId;
  }
}
