package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import java.util.Map;
import org.springframework.core.io.ByteArrayResource;

public record FieldConsentsPdfRenderResult(
    ByteArrayResource pdfContent,
    String pdfHtml,
    Map<String, String> mailMergeResolvedValuesByMnemonic
) {

}
