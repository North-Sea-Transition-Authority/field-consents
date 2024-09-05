package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.FieldDevelopmentPlanAddendum;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@Order(DocumentMailMergeFieldDisplayOrders.FIELD_DEVELOPMENT_PLAN_ADDENDUM_LIST)
@Component
public class FieldDevelopmentPlanAddendumListMailMergeField implements DocumentMailMergeField {

  static final String MNEMONIC = "FIELD_DEVELOPMENT_PLAN_ADDENDUM_LIST";
  static final String DESCRIPTION = "The list of addendums for the Field Development Plan on the primary field.";
  static final FieldProjectionRoot QUERY = new FieldProjectionRoot()
      .fieldName()
      .fieldDevelopmentPlan()
        .addendums()
          .date()
          .title()
      .root();
  static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("Mail merging field development plan addendum list");

  private final ApplicationAssetService applicationAssetService;
  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final FieldApi fieldApi;

  FieldDevelopmentPlanAddendumListMailMergeField(
      ApplicationAssetService applicationAssetService,
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      FieldApi fieldApi
  ) {
    this.applicationAssetService = applicationAssetService;
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.fieldApi = fieldApi;
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
    return DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic()).isApplicableToFieldApplications();
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    if (!primaryAsset.isField()) {
      throw new MailMergeFieldFailedToResolveException(
          "Expected primary asset [%s] to be of type [%s], but was [%s], on application version [%s]".formatted(
              primaryAsset.getAssetId(),
              AssetType.FIELD,
              primaryAsset.getAssetType(),
              applicationVersion.getId()
          ));
    }

    var field = fieldApi.findFieldById(primaryAsset.getAssetId(), QUERY, REQUEST_PURPOSE)
        .orElseThrow(() -> new MailMergeFieldFailedToResolveException(
            "Field not found for primary application asset [%s]".formatted(primaryAsset.getId()))
        );

    var fieldDevelopmentPlan = field.getFieldDevelopmentPlan();
    if (fieldDevelopmentPlan == null) {
      return mailMergeFieldIsNotValidResultResult(
          "Field Development Plan does not exist for field %s".formatted(field.getFieldName())
      );
    }

    var addendums = fieldDevelopmentPlan.getAddendums();
    if (addendums == null || addendums.isEmpty()) {
      return DocumentMailMergeFieldResolveResult.success("");
    }

    try {
      var formattedAddendumList = addendums.stream().map(this::formatAddendum).toList();
      return DocumentMailMergeFieldResolveResult.success(StringUtil.formatStringList(formattedAddendumList));
    } catch (Exception e) {
      return mailMergeFieldIsNotValidResultResult(e.getMessage());
    }
  }

  private String formatAddendum(FieldDevelopmentPlanAddendum addendum) {
    if (StringUtils.isEmpty(addendum.getTitle()) || addendum.getDate() == null) {
      throw new IllegalArgumentException("Field Development Plan Addendum is missing a date or a title");
    }

    return "%s dated %s".formatted(addendum.getTitle(), DateUtils.format(addendum.getDate(), DateUtils.LONG_DATE));
  }

  private DocumentMailMergeFieldResolveResult mailMergeFieldIsNotValidResultResult(String reason) {
    return DocumentMailMergeFieldResolveResult.error("Mail merge field %s is not valid. %s".formatted(MNEMONIC, reason));
  }

}
