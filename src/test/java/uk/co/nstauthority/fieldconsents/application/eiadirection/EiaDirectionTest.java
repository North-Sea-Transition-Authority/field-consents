package uk.co.nstauthority.fieldconsents.application.eiadirection;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_REF_1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class EiaDirectionTest {

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void from_withSat() {
    var eiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSat(SAT_ID_1);
    var expectedEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSat(applicationVersion, SAT_ID_1, SAT_REF_1);
    assertThat(EiaDirection.from(applicationVersion, eiaDirectionForm, SAT_REF_1))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirection);
  }

  @Test
  void from_withSatToSubmit() {
    var eiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSatToSubmit();
    var expectedEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSatToSubmit(applicationVersion);
    assertThat(EiaDirection.from(applicationVersion, eiaDirectionForm, null))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirection);
  }

  @Test
  void from_withNoSatToSubmit() {
    var eiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithNoSatToSubmit();
    var expectedEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithNoSatToSubmit(applicationVersion);
    assertThat(EiaDirection.from(applicationVersion, eiaDirectionForm, null))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirection);
  }

  @Test
  void from_withNoSatToSubmit_formDataBloated() {
    var eiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithAllDataSet(SAT_ID_1);
    var expectedEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithNoSatToSubmit(applicationVersion);
    assertThat(EiaDirection.from(applicationVersion, eiaDirectionForm, null))
        .usingRecursiveComparison()
        .isEqualTo(expectedEiaDirection);
  }
}