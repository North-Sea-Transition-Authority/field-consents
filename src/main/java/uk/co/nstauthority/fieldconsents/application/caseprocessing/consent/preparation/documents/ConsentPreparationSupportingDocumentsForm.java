package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

public class ConsentPreparationSupportingDocumentsForm {

  private final List<UploadedFileForm> documents;

  public static ConsentPreparationSupportingDocumentsForm from(List<UploadedFile> files) {
    var fileForms = files.stream().map(FileUploadLibraryUtils::asForm).toList();
    return new ConsentPreparationSupportingDocumentsForm(fileForms);
  }

  public ConsentPreparationSupportingDocumentsForm() {
    this.documents = new ArrayList<>();
  }

  public ConsentPreparationSupportingDocumentsForm(List<UploadedFileForm> documents) {
    this.documents = documents;
  }

  public List<UploadedFileForm> getDocuments() {
    return documents;
  }

}
