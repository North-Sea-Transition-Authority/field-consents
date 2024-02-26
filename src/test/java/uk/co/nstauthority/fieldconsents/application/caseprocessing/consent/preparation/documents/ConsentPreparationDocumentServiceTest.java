package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivum.fileuploadlibrary.core.UploadedFileTestUtil;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceSummaryViewTestUtil;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceController;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceControllerHelperService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@ExtendWith(MockitoExtension.class)
class ConsentPreparationDocumentServiceTest {

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService;

  @InjectMocks
  private ConsentPreparationDocumentService consentDocumentService;

  private Application application;

  private FieldConsentsFileUsage fileUsage;

  @BeforeEach
  void setUp() {
    application = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE).getApplication();
    fileUsage = ApplicationFileUsage.supportingConsentDocumentFrom(application);
  }

  @Test
  void saveSupportingConsentDocuments() {
    var uploadedFileForms = List.of(new UploadedFileForm(), new UploadedFileForm());

    consentDocumentService.saveSupportingConsentDocuments(application, uploadedFileForms);

    verify(fieldConsentsFileService).saveDocuments(
        fileUsage,
        uploadedFileForms
    );
  }

  @Test
  void getConsentDocumentsSummaryCard() {
    var documentInstanceSummaryView = DocumentInstanceSummaryViewTestUtil.newBuilder().build();
    var supportingConsentUploadedFile = UploadedFileTestUtil.newBuilder().build();
    var summaryFileViews = List.of(
        new SummaryFileView(
            documentInstanceSummaryView.title(),
            documentInstanceSummaryView.description(),
            ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
                .getPreviewDocumentInstance(documentInstanceSummaryView.documentInstanceId()))),
        new SummaryFileView(
            supportingConsentUploadedFile.getName(),
            supportingConsentUploadedFile.getDescription(),
            ReverseRouter.route(on(ConsentPreparationDocumentsController.class).download(APPLICATION_ID, supportingConsentUploadedFile.getId()))
        )
    );

    when(fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceSummaryViews(application))
        .thenReturn(List.of(documentInstanceSummaryView));
    when(fieldConsentsFileService.getUploadedFiles(fileUsage)).thenReturn(List.of(supportingConsentUploadedFile));

    var summaryCard = consentDocumentService.getConsentDocumentsSummaryCard(application);

    assertThat(summaryCard)
        .extracting(SummaryCard::displayName, SummaryCard::summaryCardType, SummaryCard::summaryData)
        .containsExactly("Consent documents", SummaryCardType.FILES_SUMMARY, summaryFileViews);
  }

  @Test
  void getConsentSupportingDocumentsForm() {
    var uploadedFile = UploadedFileTestUtil.newBuilder().build();

    when(fieldConsentsFileService.getUploadedFiles(fileUsage)).thenReturn(List.of(uploadedFile));

    var form = consentDocumentService.getConsentSupportingDocumentsForm(application);

    assertThat(form.getDocuments()).hasSize(1);
    assertThat(form.getDocuments()).first()
        .extracting(UploadedFileForm::getFileId)
        .isEqualTo(uploadedFile.getId());
  }
}
