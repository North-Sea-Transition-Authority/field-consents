package uk.co.nstauthority.fieldconsents.integrationtest;

import org.springframework.context.annotation.Import;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.core.FileService;

@Import({FileService.class, FileUploadProperties.class})
public @interface AutoConfigureFileUploadLibrary {
}
