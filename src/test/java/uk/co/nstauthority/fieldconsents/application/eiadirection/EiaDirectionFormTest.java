package uk.co.nstauthority.fieldconsents.application.eiadirection;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_REF_1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class EiaDirectionFormTest {

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void from_withSat() {
    var eiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSat(applicationVersion, SAT_ID_1, SAT_REF_1);
    var expectedEiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSat(SAT_ID_1);
    assertThat(EiaDirectionForm.from(eiaDirection))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirectionForm);
  }

  @Test
  void from_withSatToSubmit() {
    var eiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSatToSubmit(applicationVersion);
    var expectedEiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSatToSubmit();
    assertThat(EiaDirectionForm.from(eiaDirection))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirectionForm);
  }

  @Test
  void from_withNoSatToSubmit() {
    var eiaDirection = EiaDirectionTestUtil.getEiaDirectionWithNoSatToSubmit(applicationVersion);
    var expectedEiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithNoSatToSubmit();
    assertThat(EiaDirectionForm.from(eiaDirection))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirectionForm);
  }

  @Test
  void from_withNoSatToSubmit_dbDataBloated() {
    var eiaDirection = EiaDirectionTestUtil.getEiaDirectionWithAllDataSet(applicationVersion, SAT_ID_1, SAT_REF_1);
    var expectedEiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithAllDataSet(SAT_ID_1);
    assertThat(EiaDirectionForm.from(eiaDirection))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirectionForm);
  }
}