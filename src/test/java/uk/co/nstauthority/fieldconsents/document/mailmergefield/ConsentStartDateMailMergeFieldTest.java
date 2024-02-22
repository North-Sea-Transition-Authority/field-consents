package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataRepository;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class ConsentStartDateMailMergeFieldTest {

  private static final String MNEMONIC = "CONSENT_START_DATE";

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private ConsentDataRepository repository;

  @InjectMocks
  private ConsentStartDateMailMergeField consentStartDateMailMergeField;

  private Application application;

  @BeforeEach
  void setUp() {
    application = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT).getApplication();
  }

  @Test
  void getMnemonic() {
    assertThat(consentStartDateMailMergeField.getMnemonic()).isEqualTo(MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(consentStartDateMailMergeField.getDescription()).isEqualTo("The Consent start date for this application");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(consentStartDateMailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void resolve_whenConsentDataExists() {
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    when(documentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto)).thenReturn(application);
    when(repository.findByApplication(application)).thenReturn(Optional.of(consentData));

    assertThat(consentStartDateMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DateUtils.format(consentData.getConsentStartDate(), DateUtils.LONG_DATE));
  }

  @Test
  void resolve_whenConsentDataDoesNotExist() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    when(documentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto)).thenReturn(application);
    when(repository.findByApplication(application)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consentStartDateMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class)
        .hasMessage("%s does not exist on DocumentInstance [%s]".formatted(MNEMONIC, documentInstanceDto.id()));
  }
}
