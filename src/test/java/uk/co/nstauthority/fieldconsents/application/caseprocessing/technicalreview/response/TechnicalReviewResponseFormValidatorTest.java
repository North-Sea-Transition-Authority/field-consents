package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil.extractErrorMessages;
import static uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil.getBindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewResponseFormValidatorTest {

  @InjectMocks
  private TechnicalReviewResponseFormValidator validator;

  @Test
  void validate_emptyForm() {
    var form = TechnicalReviewResponseForm.empty();
    var errorMessages = validateAndExtractErrorMessages(form);

    assertThat(errorMessages).containsOnly(
        entry("responseType", Collections.singleton("Select whether you approve or reject this application"))
    );
  }

  @Test
  void validate_rejectedWithoutReason() {
    var form = new TechnicalReviewResponseForm(TechnicalReviewResponseType.REJECT, null, null, null);
    var errorMessages = validateAndExtractErrorMessages(form);

    assertThat(errorMessages).containsOnly(
        entry("rejectionReason.inputValue", Collections.singleton("Enter rejection reason"))
    );
  }

  @Test
  void validate_containsFilesWithoutDescriptions() {
    var form = new TechnicalReviewResponseForm(TechnicalReviewResponseType.APPROVE, null, null, null);
    var uploadedFileForms = List.of(
        uploadedFileForm(null),
        uploadedFileForm("with description")
    );
    form.documents().clear();
    form.documents().addAll(uploadedFileForms);

    var errorMessages = validateAndExtractErrorMessages(form);

    assertThat(errorMessages).containsOnly(
        entry("documents[0].uploadedFileDescription", Collections.singleton("Enter a file description"))
    );
  }

  private UploadedFileForm uploadedFileForm(String description) {
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileDescription(description);
    return uploadedFileForm;
  }

  private Map<String, Set<String>> validateAndExtractErrorMessages(Object form) {
    var bindingResult = getBindingResult(form);
    validator.validate(form, bindingResult);
    return extractErrorMessages(bindingResult);
  }

}
