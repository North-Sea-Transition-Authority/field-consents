package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class CaseProcessingActionViewTest {

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void from_withCaseOfficerTakeOwnerShip() {
    var actionItem = CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
    assertThat(CaseProcessingActionView.from(actionItem, applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(CaseProcessingActionView.from(actionItem, applicationVersion));
  }

  @Test
  void from_withCaseOfficerReleaseOwnerShip() {
    var actionItem = CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
    assertThat(CaseProcessingActionView.from(actionItem, applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(CaseProcessingActionView.from(actionItem, applicationVersion));
  }
}
