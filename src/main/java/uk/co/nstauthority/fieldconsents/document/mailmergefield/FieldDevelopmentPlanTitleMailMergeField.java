package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@Order(DocumentMailMergeFieldDisplayOrders.FIELD_DEVELOPMENT_PLAN_TITLE)
@Component
public class FieldDevelopmentPlanTitleMailMergeField implements DocumentMailMergeField {

  static final String MNEMONIC = "FIELD_DEVELOPMENT_PLAN_TITLE";
  static final String DESCRIPTION = "The Field Development Plan title";
  static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("Mail merging field development plan title");
  static final FieldProjectionRoot QUERY = new FieldProjectionRoot().fieldName().fieldDevelopmentPlan().title().root();

  private final ApplicationAssetService applicationAssetService;
  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final FieldApi fieldApi;

  FieldDevelopmentPlanTitleMailMergeField(
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
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return documentTemplateType.isApplicableToFieldApplications();
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

    var fieldOptional = fieldApi.findFieldById(primaryAsset.getAssetId(), QUERY, REQUEST_PURPOSE);
    if (fieldOptional.isEmpty()) {
      throw new MailMergeFieldFailedToResolveException("Field not found for primary application asset [%s]"
          .formatted(primaryAsset.getId()));
    }

    var field = fieldOptional.get();

    try {
      var title = FieldDevelopmentPlanMailMergeUtil.getTitle(field);
      return DocumentMailMergeFieldResolveResult.success(title);
    } catch (Exception e) {
      return DocumentMailMergeFieldResolveResult.error("Mail merge field %s is not valid. %s"
          .formatted(MNEMONIC, e.getMessage()));
    }
  }
}
