package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.validation.FileValidationUtil;

@Service
public class ConsentPreparationSupportingDocumentsFormValidator implements Validator {


  @Override
  public boolean supports(Class<?> clazz) {
    return ConsentPreparationSupportingDocumentsForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (ConsentPreparationSupportingDocumentsForm) target;
    FileValidationUtil.validateFilesHaveDescriptions(form.getDocuments(), "documents", errors);
  }
}
