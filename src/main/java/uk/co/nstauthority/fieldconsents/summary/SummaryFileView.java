package uk.co.nstauthority.fieldconsents.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSummaryView;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record SummaryFileView(
    String filename,
    String description,
    String downloadUrl
) {

  public static SummaryFileView from(UploadedFile uploadedFile, String downloadUrl) {
    return new SummaryFileView(
        uploadedFile.getName(),
        uploadedFile.getDescription(),
        downloadUrl
    );
  }

  public static SummaryFileView previewSummaryFrom(
      Application application,
      DocumentInstanceSummaryView documentInstanceSummaryView
  ) {
    return new SummaryFileView(
        documentInstanceSummaryView.title(),
        documentInstanceSummaryView.description(),
        ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getPreviewDocumentInstance(application.getId(), documentInstanceSummaryView.documentInstanceId()))
    );
  }
}
