package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.validation.FileValidationUtil;

@Component
class ConsultationResponseFormValidator {

  private static final String HABITATS_REGS_RESPONSE_TYPE = "habitatsRegsResponseType";
  private static final String EIA_REGS_RESPONSE_TYPE = "eiaRegsResponseType";
  private static final String DOCUMENTS = "documents";

  private final ConsultationService consultationService;

  ConsultationResponseFormValidator(ConsultationService consultationService) {
    this.consultationService = consultationService;
  }

  void validate(ConsultationResponseForm form, Errors errors, ApplicationVersion applicationVersion) {
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

    var habitatsRegsOptionRequiresDocumentUpload = Optional.ofNullable(form.habitatsRegsResponseType())
        .map(ConsultationResponseType::isSecretaryOfStateDecisionRequired)
        .orElse(false);

    var eiaRegsOptionRequiresDocumentUpload = Optional.ofNullable(form.eiaRegsResponseType())
        .map(ConsultationResponseType::isSecretaryOfStateDecisionRequired)
        .orElse(false);

    var documentUploadRequired = habitatsRegsOptionRequiresDocumentUpload || eiaRegsOptionRequiresDocumentUpload;
    if (documentUploadRequired && form.documents().isEmpty()) {
      errors.rejectValue(DOCUMENTS, "required", "Upload a copy of the Secretary of State's decision");
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
