package uk.co.nstauthority.fieldconsents.document.signing;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

/*
  Temporary test controller for FTSS, remove after prod deployment has been tested.
 */
@Component
@RestControllerEndpoint(id = "digital-signing")
public class DigitalSigningTestActuatorController {

  private final DocumentSigningService documentSigningService;

  public DigitalSigningTestActuatorController(DocumentSigningService documentSigningService) {
    this.documentSigningService = documentSigningService;
  }

  @GetMapping("/test-preview")
  public ResponseEntity<?> testPreview() throws IOException {

    var res = new ClassPathResource("test-digital-signature-document.pdf");

    var previewPdf = documentSigningService.previewPdfSignature(new ByteArrayResource(res.getContentAsByteArray()));

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(previewPdf.contentLength())
        .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"%s\"".formatted("preview-test.pdf"))
        .body(previewPdf);
  }

  @GetMapping("/test-sign")
  public ResponseEntity<?> testSign() throws IOException {

    var res = new ClassPathResource("test-digital-signature-document.pdf");

    var user = new ServiceUserDetail(1L, 1L, "Signer", "Test", "test@example.com");

    var previewPdf = documentSigningService.signPdf(new ByteArrayResource(res.getContentAsByteArray()), user);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(previewPdf.contentLength())
        .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"%s\"".formatted("sign-test.pdf"))
        .body(previewPdf);
  }

}
