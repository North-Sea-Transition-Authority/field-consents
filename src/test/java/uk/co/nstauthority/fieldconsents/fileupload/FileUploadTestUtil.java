package uk.co.nstauthority.fieldconsents.fileupload;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.file.UnlinkedFileController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public class FileUploadTestUtil {

  public static final UUID FILE_ID = UUID.randomUUID();
  public static final Instant FILE_UPLOADED_AT = Instant.now();
  public static final String FILE_NAME_1 = "file_name_1.pdf";
  public static final String FILE_NAME_2 = "file_name_2.doc";
  public static final String FILE_DESCRIPTION_1 = "This is a description of the file_name_1";
  public static final String FILE_DESCRIPTION_2 = "This is a description of the file_name_2";
  public static final String CONTENT_TYPE = "application/pdf";
  public static final List<UploadedFileForm> EXISTING_DOCUMENTS = Collections.emptyList();
  public static final FileUploadComponentAttributes FILE_UPLOAD_COMPONENT_ATTRIBUTES = FileUploadComponentAttributes.newBuilder()
      .withPath("form.documents")
      .withMaximumSize(DataSize.ofMegabytes(50))
      .withAllowedExtensions(Set.of("pdf"))
      .withUploadUrl(ReverseRouter.route(on(UnlinkedFileController.class).upload(null, null)))
      .withDownloadUrl(ReverseRouter.route(on(UnlinkedFileController.class).download(null, null)))
      .withDeleteUrl(ReverseRouter.route(on(UnlinkedFileController.class).delete(null, null)))
      .withExistingFiles(EXISTING_DOCUMENTS)
      .build();

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
    fileFormWithDescription.setFileSize("1.3MB");
    fileFormWithDescription.setFileUploadedAt(FILE_UPLOADED_AT);
    return fileFormWithDescription;
  }

  public static FileUploadComponentAttributes.Builder getFileUploadComponentAttributesBuilder() {
    return FileUploadComponentAttributes.newBuilder()
        .withPath("form.documents")
        .withMaximumSize(DataSize.ofMegabytes(50))
        .withUploadUrl("/upload")
        .withDownloadUrl("/download")
        .withDeleteUrl("/delete")
        .withAllowedExtensions(Set.of("csv", "pdf"))
        .withExistingFiles(Collections.emptyList());
  }

}
