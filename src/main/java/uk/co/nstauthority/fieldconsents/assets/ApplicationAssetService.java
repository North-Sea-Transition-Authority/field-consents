package uk.co.nstauthority.fieldconsents.assets;

import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Service
public class ApplicationAssetService {

  private final FieldService fieldService;

  private final TerminalService terminalService;

  private final ApplicationAssetRepository applicationAssetRepository;

  @Autowired
  public ApplicationAssetService(FieldService fieldService,
                                 TerminalService terminalService,
                                 ApplicationAssetRepository applicationAssetRepository) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationAssetRepository = applicationAssetRepository;
  }

  public void createAssetRecordForField(ApplicationVersion applicationVersion, Integer fieldId) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setFieldId(fieldId);
    applicationAsset.setCachedFieldName(fieldService.getFieldOrError(fieldId, null).fieldName());
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    applicationAssetRepository.save(applicationAsset);
  }

  public void createAssetRecordForTerminal(ApplicationVersion applicationVersion, Integer terminalId) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setTerminalId(terminalId);
    applicationAsset.setCachedTerminalName(terminalService.getTerminalOrError(terminalId, null).terminalName());
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    applicationAssetRepository.save(applicationAsset);
  }

  public ApplicationAsset getPrimaryApplicationAsset(ApplicationVersion applicationVersion) {
    return applicationAssetRepository.findByApplicationVersionAndAssetRole(applicationVersion, AssetRole.PRIMARY)
        .orElseThrow(() -> new EntityNotFoundException("Primary application asset not found for application version id %s"
            .formatted(applicationVersion.getId())));
  }

  public AssetJson getAssetJsonForApplicationAsset(ApplicationAsset applicationAsset) {
    if (applicationAsset.getFieldId() != null) {
      return fieldService.getField(applicationAsset.getFieldId(),
              "Field lookup for application asset")
          .map(AssetJson::from)
          .orElseGet(() -> AssetJson.fromCachedInformation(applicationAsset.getFieldId(),
              applicationAsset.getCachedFieldName(),
              AssetType.FIELD));
    } else if (applicationAsset.getTerminalId() != null) {
      return terminalService.getTerminal(applicationAsset.getTerminalId(),
              "Terminal lookup for application asset")
          .map(AssetJson::from)
          .orElseGet(() -> AssetJson.fromCachedInformation(applicationAsset.getTerminalId(),
              applicationAsset.getCachedTerminalName(),
              AssetType.TERMINAL));
    } else {
      throw new RuntimeException("Field and terminal ids not found for application asset id %s"
          .formatted(applicationAsset.getId()));
    }
  }

}
