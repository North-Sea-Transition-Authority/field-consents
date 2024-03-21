package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceControllerHelperService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@Service
public class ConsentPreparationDocumentService {

  private final FieldConsentsFileService fieldConsentsFileService;
  private final FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService;

  ConsentPreparationDocumentService(
      FieldConsentsFileService fieldConsentsFileService,
      FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService
  ) {
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.fieldConsentsDocumentInstanceControllerHelperService = fieldConsentsDocumentInstanceControllerHelperService;
  }

  public void saveSupportingConsentDocuments(Application application, Collection<UploadedFileForm> fileForms) {
    fieldConsentsFileService.saveDocuments(getFileUsage(application), fileForms);
  }

  public SummaryCard getConsentDocumentsSummaryCard(Application application) {
    var filesSummary = new ArrayList<SummaryFileView>();

    fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceSummaryViews(application).stream()
        .map(documentInstanceSummaryView -> SummaryFileView.previewSummaryFrom(application, documentInstanceSummaryView))
        .forEach(filesSummary::add);

    fieldConsentsFileService.getUploadedFiles(getFileUsage(application)).stream()
        .map(uploadedFile -> SummaryFileView.from(
            uploadedFile,
            ReverseRouter.route(on(ConsentPreparationFileController.class)
                .download(application.getId(), uploadedFile.getId(), null)))
        )
        .forEach(filesSummary::add);

    return SummaryCard.filesSummaryCardWithHeading("Consent documents", filesSummary);
  }

  ConsentPreparationSupportingDocumentsForm getConsentSupportingDocumentsForm(Application application) {
    return ConsentPreparationSupportingDocumentsForm.from(fieldConsentsFileService.getUploadedFiles(getFileUsage(application)));
  }

  private FieldConsentsFileUsage getFileUsage(Application application) {
    return ApplicationFileUsage.supportingConsentDocumentFrom(application);
  }
}
