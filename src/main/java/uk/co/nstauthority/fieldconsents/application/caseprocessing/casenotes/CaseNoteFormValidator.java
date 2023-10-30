package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.validation.FileValidationUtil;

@Service
public class CaseNoteFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return CaseNoteForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (CaseNoteForm) target;

    StringInputValidator.builder()
        .validate(form.getCaseNoteText(), errors);

    if (!form.getDocuments().isEmpty()) {
      FileValidationUtil.validateFilesHaveDescriptions(form.getDocuments(), "documents", errors);
    }
  }
}
