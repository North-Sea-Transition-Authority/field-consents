package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@Order(2)
@Component
class PrimaryAndAdditionalFieldNamesMailMergeField implements DocumentMailMergeField {

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ApplicationAssetService applicationAssetService;
  private final FieldService fieldService;

  @Autowired
  PrimaryAndAdditionalFieldNamesMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      ApplicationAssetService applicationAssetService,
      FieldService fieldService
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.applicationAssetService = applicationAssetService;
    this.fieldService = fieldService;
  }

  @Override
  public String getMnemonic() {
    return "PRIMARY_AND_ADDITIONAL_FIELD_NAMES";
  }

  @Override
  public String getDescription() {
    return "The names of the primary and additional fields on the application";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return documentTemplateType == DocumentTemplateType.FIELD_FLARE_CONSENT
        || documentTemplateType == DocumentTemplateType.FIELD_VENT_CONSENT;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var fieldNames = getFieldNames(documentInstanceDto);
    return DocumentMailMergeFieldResolveResult.success(StringUtil.formatStringList(fieldNames));
  }

  private List<String> getFieldNames(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var applicationAssets = applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
        applicationVersion,
        AssetType.FIELD,
        Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
    );

    var primaryFieldId = applicationAssets.stream()
        .filter(applicationAsset -> applicationAsset.getAssetRole() == AssetRole.PRIMARY)
        .map(ApplicationAsset::getAssetId)
        .findFirst()
        .orElseThrow(() -> new MailMergeFieldFailedToResolveException("Unable to find primary field"));

    var fieldIds = applicationAssets.stream().map(ApplicationAsset::getAssetId).toList();

    var fieldJsons = fieldService.findFieldsByIds(fieldIds, "Fields lookup for %s mail merge field".formatted(getMnemonic()));
    var fieldJsonsById = fieldJsons.stream()
        .collect(Collectors.toMap(FieldJson::getId, Function.identity()));

    var primaryFieldName = Optional.ofNullable(fieldJsonsById.get(primaryFieldId))
        .map(FieldJson::getName)
        .orElseThrow(() ->
            new MailMergeFieldFailedToResolveException(
                "Unable to find field json for primary field id: %d".formatted(primaryFieldId)
            )
        );

    var secondaryFieldNames = applicationAssets.stream()
        .filter(applicationAsset -> applicationAsset.getAssetRole() == AssetRole.SECONDARY)
        .map(ApplicationAsset::getAssetId)
        .map(secondaryFieldId ->
            Optional.ofNullable(fieldJsonsById.get(secondaryFieldId))
                .orElseThrow(() ->
                    new MailMergeFieldFailedToResolveException(
                        "Unable to find field json for secondary field id: %d".formatted(secondaryFieldId)
                    )
                )
        )
        .map(FieldJson::getName)
        .sorted()
        .toList();

    return Stream.concat(Stream.of(primaryFieldName), secondaryFieldNames.stream()).toList();
  }

}
