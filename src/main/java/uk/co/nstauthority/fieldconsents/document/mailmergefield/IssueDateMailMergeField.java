package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import java.time.Clock;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Order(DocumentMailMergeFieldDisplayOrders.ISSUE_DATE)
@Component
class IssueDateMailMergeField implements DocumentMailMergeField {

  static final String MNEMONIC = "ISSUE_DATE";
  static final String DESCRIPTION = "The date the document was issued";

  private final Clock clock;

  IssueDateMailMergeField(Clock clock) {
    this.clock = clock;
  }

  @Override
  public String getMnemonic() {
    return MNEMONIC;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    return DocumentMailMergeFieldResolveResult.success(DateUtils.format(clock.instant(), DateUtils.LONG_DATE));
  }
}
