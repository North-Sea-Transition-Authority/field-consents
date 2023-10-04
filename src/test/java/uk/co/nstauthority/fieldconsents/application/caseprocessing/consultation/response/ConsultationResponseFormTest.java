package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.EiaRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.HabitatsRegsResponseType;

class ConsultationResponseFormTest {

  private static final String DESCRIPTION = "description";
  private Consultation consultation;

  @BeforeEach
  void setUp() {
    consultation = new Consultation();
  }

  @Test
  void from_habitats_agree() {
    consultation.setHabitatsRegsResponseType(HabitatsRegsResponseType.AGREE);
    consultation.setHabitatsRegsResponseDescription(DESCRIPTION);

    assertThat(ConsultationResponseForm.from(consultation))
        .extracting(
            form -> unwrap(form.habitatsRegsAgreeDescription()),
            form -> unwrap(form.habitatsRegsDoNotAgreeDescription()),
            form -> unwrap(form.habitatsRegsDoesNotApplyDescription())
        ).containsExactly(DESCRIPTION, null, null);
  }

  @Test
  void from_habitats_doNotAgree() {
    consultation.setHabitatsRegsResponseType(HabitatsRegsResponseType.DO_NOT_AGREE);
    consultation.setHabitatsRegsResponseDescription(DESCRIPTION);

    assertThat(ConsultationResponseForm.from(consultation))
        .extracting(
            form -> unwrap(form.habitatsRegsAgreeDescription()),
            form -> unwrap(form.habitatsRegsDoNotAgreeDescription()),
            form -> unwrap(form.habitatsRegsDoesNotApplyDescription())
        ).containsExactly(null, DESCRIPTION, null);
  }

  @Test
  void from_habitats_doesNotApply() {
    consultation.setHabitatsRegsResponseType(HabitatsRegsResponseType.DOES_NOT_APPLY);
    consultation.setHabitatsRegsResponseDescription(DESCRIPTION);

    assertThat(ConsultationResponseForm.from(consultation))
        .extracting(
            form -> unwrap(form.habitatsRegsAgreeDescription()),
            form -> unwrap(form.habitatsRegsDoNotAgreeDescription()),
            form -> unwrap(form.habitatsRegsDoesNotApplyDescription())
        ).containsExactly(null, null, DESCRIPTION);
  }

  @Test
  void from_eia_agree() {
    consultation.setEiaRegsResponseType(EiaRegsResponseType.AGREE);
    consultation.setEiaRegsResponseDescription(DESCRIPTION);

    assertThat(ConsultationResponseForm.from(consultation))
        .extracting(
            form -> unwrap(form.eiaRegsAgreeDescription()),
            form -> unwrap(form.eiaRegsDoNotAgreeDescription()),
            form -> unwrap(form.eiaRegsDoesNotApplyDescription())
        ).containsExactly(DESCRIPTION, null, null);
  }

  @Test
  void from_eia_doNotAgree() {
    consultation.setEiaRegsResponseType(EiaRegsResponseType.DO_NOT_AGREE);
    consultation.setEiaRegsResponseDescription(DESCRIPTION);

    assertThat(ConsultationResponseForm.from(consultation))
        .extracting(
            form -> unwrap(form.eiaRegsAgreeDescription()),
            form -> unwrap(form.eiaRegsDoNotAgreeDescription()),
            form -> unwrap(form.eiaRegsDoesNotApplyDescription())
        ).containsExactly(null, DESCRIPTION, null);
  }

  @Test
  void from_eia_doesNotApply() {
    consultation.setEiaRegsResponseType(EiaRegsResponseType.DOES_NOT_APPLY);
    consultation.setEiaRegsResponseDescription(DESCRIPTION);

    assertThat(ConsultationResponseForm.from(consultation))
        .extracting(
            form -> unwrap(form.eiaRegsAgreeDescription()),
            form -> unwrap(form.eiaRegsDoNotAgreeDescription()),
            form -> unwrap(form.eiaRegsDoesNotApplyDescription())
        ).containsExactly(null, null, DESCRIPTION);
  }

  private String unwrap(StringInput stringInput) {
    return stringInput.getInputValue();
  }

  @ParameterizedTest
  @EnumSource(HabitatsRegsResponseType.class)
  void getHabitatsRegsDescription(HabitatsRegsResponseType radioOption) {
    consultation.setHabitatsRegsResponseType(radioOption);
    consultation.setHabitatsRegsResponseDescription(DESCRIPTION);

    var form = ConsultationResponseForm.from(consultation);
    assertThat(form.getHabitatsRegsDescription())
        .map(StringInput::getInputValue)
        .contains(DESCRIPTION);
  }

  @Test
  void getHabitatsRegsDescription_radioNull() {
    consultation.setHabitatsRegsResponseDescription(DESCRIPTION);

    var form = ConsultationResponseForm.from(consultation);
    assertThat(form.getHabitatsRegsDescription()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(EiaRegsResponseType.class)
  void getEiaRegsDescription(EiaRegsResponseType radioOption) {
    consultation.setEiaRegsResponseType(radioOption);
    consultation.setEiaRegsResponseDescription(DESCRIPTION);

    var form = ConsultationResponseForm.from(consultation);
    assertThat(form.getEiaRegsDescription())
        .map(StringInput::getInputValue)
        .contains(DESCRIPTION);
  }

  @Test
  void getEiaRegsDescription_radioNull() {
    consultation.setEiaRegsResponseDescription(DESCRIPTION);

    var form = ConsultationResponseForm.from(consultation);
    assertThat(form.getEiaRegsDescription()).isEmpty();
  }
}
