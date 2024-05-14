package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document;

import java.util.Comparator;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;

public class ConsentDocumentComparators {

  public static Comparator<DocumentInstanceDto> documentInstanceDto() {
    return Comparator.comparing(dto -> dto.documentTemplateDto().displayOrder());
  }

  public static Comparator<UploadedFile> supportingUploadedFile() {
    return Comparator.comparing(uploadedFile -> uploadedFile.getName().toLowerCase());
  }

  private ConsentDocumentComparators() {
    throw new IllegalStateException("ConsentDocumentComparators is a util class and should not be instantiated");
  }

}
