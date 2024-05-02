package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import java.util.Map;
import uk.co.fivium.digitaldocumentlibrary.document.PdfRenderResult;

public record PdfRenderResultWithGenerationData(
    PdfRenderResult pdfRenderResult,
    Map<String, String> mailMergeResolvedValuesByMnemonic
) {

}
