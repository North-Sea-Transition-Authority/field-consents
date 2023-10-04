package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.validation.FileValidationUtil;

@Component
class ConsultationResponseFormValidator implements SmartValidator {

  private static final String HABITATS_REGS_RESPONSE_TYPE = "habitatsRegsResponseType";
  private static final String EIA_REGS_RESPONSE_TYPE = "eiaRegsResponseType";
  private static final String DOCUMENTS = "documents";

  private final ConsultationService consultationService;

  ConsultationResponseFormValidator(ConsultationService consultationService) {
    this.consultationService = consultationService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ConsultationResponseForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new IllegalStateException("You must pass an ApplicationVersion as a validation hint");
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (ConsultationResponseForm) target;
    var applicationVersion = (ApplicationVersion) validationHints[0];

    validateRadioOption(
        form.habitatsRegsResponseType(),
        form.getHabitatsRegsDescription().orElse(null),
        HABITATS_REGS_RESPONSE_TYPE,
        "Select a response under the Habitats regulations",
        errors
    );

    if (consultationService.requiresEiaRegsResponse(applicationVersion)) {
      validateRadioOption(
          form.eiaRegsResponseType(),
          form.getEiaRegsDescription().orElse(null),
          EIA_REGS_RESPONSE_TYPE,
          "Select a response under the EIA regulations",
          errors
      );
    }

    if (errors.hasFieldErrors(HABITATS_REGS_RESPONSE_TYPE) || errors.hasFieldErrors(EIA_REGS_RESPONSE_TYPE)) {
      return;
    }

    var habitatsRegsOptionRequiresDocumentUpload = Optional.ofNullable(form.habitatsRegsResponseType())
        .map(ConsultationResponseType::isSecretaryOfStateDecisionRequired)
        .orElse(false);

    var eiaRegsOptionRequiresDocumentUpload = Optional.ofNullable(form.eiaRegsResponseType())
        .map(ConsultationResponseType::isSecretaryOfStateDecisionRequired)
        .orElse(false);

    var documentUploadRequired = habitatsRegsOptionRequiresDocumentUpload || eiaRegsOptionRequiresDocumentUpload;

    if (!documentUploadRequired) {
      return;
    }

    if (form.documents().isEmpty()) {
      errors.rejectValue(DOCUMENTS, "required", "Upload a copy of the Secretary of State's decision");
    }

    if (errors.hasFieldErrors(DOCUMENTS)) {
      return;
    }

    FileValidationUtil.validateFilesHaveDescriptions(form.documents(), DOCUMENTS, errors);
  }

  private void validateRadioOption(
      ConsultationResponseType radioOption,
      StringInput description,
      String field,
      String messageIfMissing,
      Errors errors
  ) {
    ValidationUtils.rejectIfEmpty(errors, field, "required", messageIfMissing);

    if (Objects.isNull(description)) {
      return;
    }

    if (!errors.hasFieldErrors(field) && radioOption.isTextAreaInputRequired()) {
      StringInputValidator.builder().validate(description, errors);
    }
  }

}
