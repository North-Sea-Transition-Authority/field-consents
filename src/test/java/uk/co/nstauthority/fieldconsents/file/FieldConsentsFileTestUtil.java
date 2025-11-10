package uk.co.nstauthority.fieldconsents.file;

import java.util.UUID;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;

public class FieldConsentsFileTestUtil {

  static final String GENERIC_FILE_DESCRIPTION = "file description";

  public static UploadedFile createUploadedFile() {
    var uploadedFile = new UploadedFile(UUID.randomUUID());
    uploadedFile.setDescription(GENERIC_FILE_DESCRIPTION);
    return uploadedFile;
  }

  public static UploadedFile createUploadedFile(FieldConsentsFileUsage fileUsage) {
    var uploadedFile = createUploadedFile();
    uploadedFile.setUsageId(fileUsage.usageId());
    uploadedFile.setUsageType(fileUsage.usageType());
    uploadedFile.setDocumentType(fileUsage.documentType());
    return uploadedFile;
  }
}
