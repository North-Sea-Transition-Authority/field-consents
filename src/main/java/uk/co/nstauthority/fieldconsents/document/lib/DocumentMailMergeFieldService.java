package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@Service
public class DocumentMailMergeFieldService {

  // This is the same regex as GOV.UK Notify uses:
  // https://github.com/alphagov/notifications-utils/blob/main/notifications_utils/field.py#L64
  // This must only match the inner most brackets, e.g. (((TEST))) should match ((TEST)).
  private static final Pattern MAIL_MERGE_FIELD_PATTERN = Pattern.compile("\\({2}([^()]+)\\){2}");

  static final String SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE = "Mail merge field %s is not valid";
  static final String MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE = "Mail merge fields %s are not valid";

  private final List<DocumentMailMergeField> documentMailMergeFields;

  @Autowired
  DocumentMailMergeFieldService(List<DocumentMailMergeField> documentMailMergeFields) {
    this.documentMailMergeFields = documentMailMergeFields;
  }

  public List<DocumentMailMergeField> getApplicableDocumentMailMergeFields(DocumentTemplateDto documentTemplateDto) {
    return documentMailMergeFields.stream()
        .filter(documentMailMergeField -> documentMailMergeField.isApplicable(documentTemplateDto))
        .toList();
  }

  DocumentMailMergeValidationResult validateMailMergeFields(
      DocumentTemplateDto documentTemplateDto,
      String text
  ) {
    var textMailMergeFieldMnemonics = MAIL_MERGE_FIELD_PATTERN.matcher(text).results()
        .map(matchResult -> getMnemonicFromMailMergeFieldText(matchResult.group()))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    var invalidMnemonics = textMailMergeFieldMnemonics.stream()
        .filter(mnemonic -> getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic).isEmpty())
        .toList();

    if (invalidMnemonics.isEmpty()) {
      return DocumentMailMergeValidationResult.valid();
    }

    var errorMessage = invalidMnemonics.size() == 1
        ? SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted(invalidMnemonics.get(0))
        : MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE.formatted(StringUtil.formatStringList(invalidMnemonics));

    return DocumentMailMergeValidationResult.invalid(errorMessage);
  }

  String resolveMailMergeFields(DocumentInstanceSectionDto documentInstanceSectionDto) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    return MAIL_MERGE_FIELD_PATTERN.matcher(documentInstanceSectionDto.content()).replaceAll(matcher -> {
      var matchText = matcher.group();
      var mnemonic = getMnemonicFromMailMergeFieldText(matchText);

      return getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic)
          .map(documentMailMergeField -> documentMailMergeField.resolve(documentInstanceDto))
          .orElse(matchText);
    });
  }

  private String getMnemonicFromMailMergeFieldText(String mailMergeFieldText) {
    return mailMergeFieldText.substring(2, mailMergeFieldText.length() - 2);
  }

  Optional<DocumentMailMergeField> getApplicableDocumentMailMergeField(
      DocumentTemplateDto documentTemplateDto,
      String mnemonic
  ) {
    return documentMailMergeFields.stream()
        .filter(documentMailMergeField -> documentMailMergeField.getMnemonic().equals(mnemonic))
        .filter(documentMailMergeField -> documentMailMergeField.isApplicable(documentTemplateDto))
        .findFirst();
  }
}
