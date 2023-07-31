package uk.co.nstauthority.fieldconsents.summary;

import uk.co.fivium.fileuploadlibrary.core.UploadedFile;

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

}
