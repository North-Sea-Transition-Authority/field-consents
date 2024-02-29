package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@Order(8)
@Component
public class FieldDevelopmentPlanTitleMailMergeField implements DocumentMailMergeField {

  static final String MNEMONIC = "FIELD_DEVELOPMENT_PLAN_TITLE";
  static final String DESCRIPTION = "The Field Development Plan title";
  static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("Mail merging field development plan title");
  static final FieldProjectionRoot QUERY = new FieldProjectionRoot().fieldDevelopmentPlan().title().root();

  private final ApplicationAssetService applicationAssetService;
  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final FieldApi fieldApi;

  FieldDevelopmentPlanTitleMailMergeField(
      ApplicationAssetService applicationAssetService,
      DocumentInstanceLinkingService documentInstanceLinkingService,
      FieldApi fieldApi
  ) {
    this.applicationAssetService = applicationAssetService;
    this.documentInstanceLinkingService = documentInstanceLinkingService;
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
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

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

    return fieldApi
        .findFieldById(primaryAsset.getAssetId(), QUERY, REQUEST_PURPOSE)
        .map(field -> field.getFieldDevelopmentPlan().getTitle())
        .orElseThrow(() -> new MailMergeFieldFailedToResolveException(
            "Field development plan does not exist for field [%s]".formatted(primaryAsset.getAssetId())
        ));
  }
}
