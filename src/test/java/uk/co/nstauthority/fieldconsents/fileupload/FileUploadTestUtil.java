package uk.co.nstauthority.fieldconsents.fileupload;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

public class FileUploadTestUtil {

  public static final UUID FILE_ID = UUID.randomUUID();
  public static final String FILE_NAME_1 = "file_name_1.pdf";
  public static final String FILE_NAME_2 = "file_name_2.doc";
  public static final String FILE_DESCRIPTION_1 = "This is a description of the file_name_1";
  public static final String FILE_DESCRIPTION_2 = "This is a description of the file_name_2";
  public static final String DOCUMENT_TYPE = "document-type";
  public static final String CONTENT_TYPE = "application/pdf";
  public static final String APPLICATION_VERSION_USAGE_TYPE = "ApplicationVersion";
  public static final String CASE_NOTE_USAGE_TYPE = "CaseNote";

  public static final List<UploadedFileForm> validDocumentForms =
      List.of(
          getUploadedFileFormWithDescription(FILE_NAME_1, FILE_DESCRIPTION_1),
          getUploadedFileFormWithDescription(FILE_NAME_2, FILE_DESCRIPTION_2)
      );

  public static final List<UploadedFileForm> documentFormsWithMissingDescription =
      List.of(
          getUploadedFileFormWithDescription(FILE_NAME_1, FILE_DESCRIPTION_1),
          getUploadedFileWithFileName(FILE_NAME_2)
      );

  public static UploadedFileForm getUploadedFileWithFileName(String fileName) {
    var fileForm = new UploadedFileForm();
    fileForm.setFileId(FILE_ID);
    fileForm.setFileName(fileName);
    return fileForm;
  }

  public static UploadedFileForm getUploadedFileFormWithDescription(String fileName, String fileDescription) {
    var fileFormWithDescription = getUploadedFileWithFileName(fileName);
    fileFormWithDescription.setFileDescription(fileDescription);
    return fileFormWithDescription;
  }

  public static FileUploadComponentAttributes getFileUploadComponentAttributesWithPath(String bindingPath) {
    return FileUploadComponentAttributes.newBuilder()
        .withPath(bindingPath)
        .withMaximumSize(DataSize.ofMegabytes(50))
        .withUploadUrl("/upload")
        .withDownloadUrl("/download")
        .withDeleteUrl("/delete")
        .withAllowedExtensions(Set.of("csv", "pdf"))
        .withExistingFiles(Collections.emptyList())
        .build();
  }


}
