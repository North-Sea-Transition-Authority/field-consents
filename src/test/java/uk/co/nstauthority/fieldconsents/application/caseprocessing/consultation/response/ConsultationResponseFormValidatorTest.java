package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.EiaRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.HabitatsRegsResponseType;

@ExtendWith(MockitoExtension.class)
class ConsultationResponseFormValidatorTest {

  private static final String HABITATS_REGS_RESPONSE_TYPE = "habitatsRegsResponseType";
  private static final String EIA_REGS_RESPONSE_TYPE = "eiaRegsResponseType";
  private static final String DOCUMENTS = "documents";

  private static final String FILE_UPLOAD_ERROR_MESSAGE = "Upload a copy of the Secretary of State's decision";

  private static final String DESCRIPTION_TEXT = "description text";
  private static final UploadedFileForm UPLOADED_FILE_FORM = new UploadedFileForm();
  static {
    UPLOADED_FILE_FORM.setFileId(UUID.randomUUID());
    UPLOADED_FILE_FORM.setFileName("decision-letter.pdf");
    UPLOADED_FILE_FORM.setFileDescription("This is the decision letter");
  }

  @Mock
  private ConsultationService consultationService;

  @InjectMocks
  private ConsultationResponseFormValidator validator;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void supports() {
    assertThat(validator.supports(ConsultationResponseForm.class)).isTrue();
  }

  @ParameterizedTest
  @MethodSource("validate_eia_required")
  void validate_eiaRequired(ConsultationResponseForm form, List<Tuple> expectedErrors) {
    when(consultationService.requiresEiaRegsResponse(applicationVersion)).thenReturn(true);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, bindingResult, applicationVersion);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getDefaultMessage)
        .containsExactlyElementsOf(expectedErrors);
  }

  private static Stream<Arguments> validate_eia_required() {
    return Stream.of(
        arguments(
            formWithValues(null, null, null, null, List.of(UPLOADED_FILE_FORM)),
            errors(
                tuple(HABITATS_REGS_RESPONSE_TYPE, "Select a response under the Habitats regulations"),
                tuple(EIA_REGS_RESPONSE_TYPE, "Select a response under the EIA regulations")
            )
        ),
        // check habitat regs radio and description in the form
        arguments(
            formWithValues(null, null, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors(tuple(HABITATS_REGS_RESPONSE_TYPE, "Select a response under the Habitats regulations"))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, null, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, null, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors(tuple("habitatsRegsDoNotAgreeDescription.inputValue", "Enter description"))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, null, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        // check EIA regs radio and description in the form
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, null, null, List.of(UPLOADED_FILE_FORM)),
            errors(tuple(EIA_REGS_RESPONSE_TYPE, "Select a response under the EIA regulations"))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, null, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DO_NOT_AGREE, null, List.of(UPLOADED_FILE_FORM)),
            errors(tuple("eiaRegsDoNotAgreeDescription.inputValue", "Enter description"))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DOES_NOT_APPLY, null, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        // validate document upload
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, EiaRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, EiaRegsResponseType.AGREE, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, EiaRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, Collections.emptyList()),
            errors(tuple(DOCUMENTS, FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, EiaRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, Collections.emptyList()),
            errors()
        )
    );
  }

  @ParameterizedTest
  @MethodSource("validate_eia_not_required")
  void validate_eiaNotRequired(ConsultationResponseForm form, List<Tuple> expectedErrors) {
    when(consultationService.requiresEiaRegsResponse(applicationVersion)).thenReturn(false);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, bindingResult, applicationVersion);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getDefaultMessage)
        .containsExactlyElementsOf(expectedErrors);
  }

  private static Stream<Arguments> validate_eia_not_required() {
    return Stream.of(
        // check habitat regs radio and description in the form
        arguments(
            formWithValues(null, null, null, null, List.of(UPLOADED_FILE_FORM)),
            errors(tuple(HABITATS_REGS_RESPONSE_TYPE, "Select a response under the Habitats regulations"))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, null, null, null, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, null, null, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, null, null, null, List.of(UPLOADED_FILE_FORM)),
            errors(tuple("habitatsRegsDoNotAgreeDescription.inputValue", "Enter description"))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, null, null, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, null, null, null, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, null, null, List.of(UPLOADED_FILE_FORM)),
            errors()
        ),
        // validate document upload
        arguments(
            formWithValues(HabitatsRegsResponseType.AGREE, DESCRIPTION_TEXT, null, null, Collections.emptyList()),
            errors(tuple("documents", FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DO_NOT_AGREE, DESCRIPTION_TEXT, null, null, Collections.emptyList()),
            errors(tuple("documents", FILE_UPLOAD_ERROR_MESSAGE))
        ),
        arguments(
            formWithValues(HabitatsRegsResponseType.DOES_NOT_APPLY, DESCRIPTION_TEXT, null, null, Collections.emptyList()),
            errors()
        )
    );
  }

  private static ConsultationResponseForm formWithValues(
      HabitatsRegsResponseType habitatsRegsResponseType,
      String habitatsRegsDescription,
      EiaRegsResponseType eiaRegsResponseType,
      String eiaRegsDescription,
      List<UploadedFileForm> documents
  ) {
    var form = new ConsultationResponseForm(
        habitatsRegsResponseType,
        null,
        null,
        null,
        eiaRegsResponseType,
        null,
        null,
        null,
        null
    );

    if (Objects.nonNull(habitatsRegsResponseType)) {
      switch (habitatsRegsResponseType) {
        case AGREE -> form.habitatsRegsAgreeDescription().setInputValue(habitatsRegsDescription);
        case DO_NOT_AGREE -> form.habitatsRegsDoNotAgreeDescription().setInputValue(habitatsRegsDescription);
        case DOES_NOT_APPLY -> form.habitatsRegsDoesNotApplyDescription().setInputValue(habitatsRegsDescription);
      }
    }

    if (Objects.nonNull(eiaRegsResponseType)) {
      switch (eiaRegsResponseType) {
        case AGREE -> form.eiaRegsAgreeDescription().setInputValue(eiaRegsDescription);
        case DO_NOT_AGREE -> form.eiaRegsDoNotAgreeDescription().setInputValue(eiaRegsDescription);
        case DOES_NOT_APPLY -> form.eiaRegsDoesNotApplyDescription().setInputValue(eiaRegsDescription);
      }
    }

    form.documents().addAll(documents);

    return form;
  }

  private static List<Tuple> errors(Tuple... tuples) {
    return List.of(tuples);
  }

}
