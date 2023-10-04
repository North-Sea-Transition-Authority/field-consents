package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.EiaRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.HabitatsRegsResponseType;

record ConsultationResponseForm(
    HabitatsRegsResponseType habitatsRegsResponseType,
    StringInput habitatsRegsAgreeDescription,
    StringInput habitatsRegsDoNotAgreeDescription,
    StringInput habitatsRegsDoesNotApplyDescription,

    EiaRegsResponseType eiaRegsResponseType,
    StringInput eiaRegsAgreeDescription,
    StringInput eiaRegsDoNotAgreeDescription,
    StringInput eiaRegsDoesNotApplyDescription,

    List<UploadedFileForm> documents
) {

  private static final String DESCRIPTION = "description";

  ConsultationResponseForm {
    habitatsRegsAgreeDescription = new StringInput("habitatsRegsAgreeDescription", DESCRIPTION);
    habitatsRegsDoNotAgreeDescription = new StringInput("habitatsRegsDoNotAgreeDescription", DESCRIPTION);
    habitatsRegsDoesNotApplyDescription = new StringInput("habitatsRegsDoesNotApplyDescription", DESCRIPTION);

    eiaRegsAgreeDescription = new StringInput("eiaRegsAgreeDescription", DESCRIPTION);
    eiaRegsDoNotAgreeDescription = new StringInput("eiaRegsDoNotAgreeDescription", DESCRIPTION);
    eiaRegsDoesNotApplyDescription = new StringInput("eiaRegsDoesNotApplyDescription", DESCRIPTION);

    documents = new ArrayList<>();
  }

  static ConsultationResponseForm from(Consultation consultation) {
    var form = new ConsultationResponseForm(
        consultation.getHabitatsRegsResponseType(),
        null,
        null,
        null,
        consultation.getEiaRegsResponseType(),
        null,
        null,
        null,
        null
    );

    var habitatsRegsResponseType = consultation.getHabitatsRegsResponseType();
    if (!Objects.isNull(habitatsRegsResponseType)) {
      var description = consultation.getHabitatsRegsResponseDescription();
      switch (habitatsRegsResponseType) {
        case AGREE -> form.habitatsRegsAgreeDescription.setInputValue(description);
        case DO_NOT_AGREE -> form.habitatsRegsDoNotAgreeDescription.setInputValue(description);
        case DOES_NOT_APPLY -> form.habitatsRegsDoesNotApplyDescription.setInputValue(description);
        default -> {
        }
      }
    }

    var eiaRegsResponseType = consultation.getEiaRegsResponseType();
    if (Objects.nonNull(eiaRegsResponseType)) {
      var description = consultation.getEiaRegsResponseDescription();
      switch (eiaRegsResponseType) {
        case AGREE -> form.eiaRegsAgreeDescription.setInputValue(description);
        case DO_NOT_AGREE -> form.eiaRegsDoNotAgreeDescription.setInputValue(description);
        case DOES_NOT_APPLY -> form.eiaRegsDoesNotApplyDescription.setInputValue(description);
        default -> {
        }
      }
    }

    return form;
  }

  public Optional<StringInput> getHabitatsRegsDescription() {
    return getDescriptionFor(habitatsRegsResponseType);
  }

  public Optional<StringInput> getEiaRegsDescription() {
    return getDescriptionFor(eiaRegsResponseType);
  }

  private Optional<StringInput> getDescriptionFor(ConsultationResponseType consultationResponseType) {
    if (consultationResponseType instanceof EiaRegsResponseType responseType) {
      var description = switch (responseType) {
        case AGREE -> eiaRegsAgreeDescription;
        case DO_NOT_AGREE -> eiaRegsDoNotAgreeDescription;
        case DOES_NOT_APPLY -> eiaRegsDoesNotApplyDescription;
      };

      return Optional.of(description);
    }

    if (consultationResponseType instanceof HabitatsRegsResponseType responseType) {
      var description = switch (responseType) {
        case AGREE -> habitatsRegsAgreeDescription;
        case DO_NOT_AGREE -> habitatsRegsDoNotAgreeDescription;
        case DOES_NOT_APPLY -> habitatsRegsDoesNotApplyDescription;
      };

      return Optional.of(description);
    }

    return Optional.empty();
  }

}
